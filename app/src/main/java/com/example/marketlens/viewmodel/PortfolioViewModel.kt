package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.analytics.AnalyticsEngine
import com.example.marketlens.analytics.PortfolioBlender
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.model.HoldingSnapshot
import com.example.marketlens.data.model.PortfolioHolding
import com.example.marketlens.data.model.PortfolioResult
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.PortfolioRepository
import com.example.marketlens.data.repository.WatchlistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class PortfolioViewModel(
    private val portfolioRepo: PortfolioRepository  = AppContainer.portfolioRepository,
    private val marketRepo:    MarketRepository      = AppContainer.repository,
    private val watchlistRepo: WatchlistRepository   = AppContainer.watchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioState(isLoading = true))
    val state: StateFlow<PortfolioState> = _state.asStateFlow()

    init { loadAll() }


    fun onDropdownExpand()   { _state.value = _state.value.copy(isDropdownExpanded = true) }
    fun onDropdownDismiss()  { _state.value = _state.value.copy(isDropdownExpanded = false) }

    fun onSymbolSelected(symbol: String) {
        _state.value = _state.value.copy(
            formSymbol          = symbol,
            isDropdownExpanded  = false,
            formError           = null
        )
    }


    fun onShowForm()  {
        _state.value = _state.value.copy(
            showForm       = true,
            formSymbol     = "",
            formShares     = "",
            formPurchasePrice = "",
            formError      = null
        )
    }
    fun onHideForm()  { _state.value = _state.value.copy(showForm = false, formError = null) }

    fun onFormSharesChanged(v: String) {
        _state.value = _state.value.copy(formShares = v.filter { it.isDigit() || it == '.' }, formError = null)
    }
    fun onFormPurchasePriceChanged(v: String) {
        _state.value = _state.value.copy(formPurchasePrice = v.filter { it.isDigit() || it == '.' }, formError = null)
    }

    fun onHorizonSelected(h: PortfolioHorizon) {
        _state.value = _state.value.copy(selectedHorizon = h, simulationError = null, result = _state.value.result?.copy(simulation = null))
    }



    fun onAddHolding() {
        val symbol        = _state.value.formSymbol
        val shares        = _state.value.formShares.toDoubleOrNull()
        val purchasePrice = _state.value.formPurchasePrice.toDoubleOrNull()

        if (symbol.isBlank()) {
            _state.value = _state.value.copy(formError = "Select a stock from your watchlist")
            return
        }
        if (shares == null || shares <= 0) {
            _state.value = _state.value.copy(formError = "Enter a valid number of shares")
            return
        }
        if (purchasePrice == null || purchasePrice <= 0) {
            _state.value = _state.value.copy(formError = "Enter a valid purchase price")
            return
        }

        _state.value = _state.value.copy(isSaving = true, formError = null)
        viewModelScope.launch {
            val holding = PortfolioHolding(symbol, shares, purchasePrice)
            when (portfolioRepo.saveHolding(holding)) {
                is ApiResult.Success -> { onHideForm(); loadAll() }
                is ApiResult.Error   -> _state.value = _state.value.copy(isSaving = false, formError = "Could not save. Try again.")
            }
            Firebase.analytics.logEvent("portfolio_holding_added") {
                param("symbol", holding.symbol)
            }
        }
    }



    fun onDeleteHolding(symbol: String) {
        viewModelScope.launch {
            portfolioRepo.deleteHolding(symbol)
            loadAll()
        }
    }



    fun onSimulate() {
        val holdings = _state.value.holdings
        if (holdings.isEmpty()) {
            _state.value = _state.value.copy(simulationError = "Add at least one holding first")
            return
        }

        _state.value = _state.value.copy(isSimulating = true, simulationError = null)

        viewModelScope.launch {
            val now         = Instant.now().epochSecond
            val twoYearsAgo = now - (2 * 365 * 86400L)

            val priceDeferred = holdings.associate { h ->
                h.symbol to async {
                    try { marketRepo.getCandles(h.symbol, "W", twoYearsAgo, now) }
                    catch (e: Exception) { ApiResult.Error("Failed: ${e.message}") }
                }
            }
            val quoteDeferred = holdings.associate { h ->
                h.symbol to async {
                    try { marketRepo.getQuote(h.symbol) }
                    catch (e: Exception) { ApiResult.Error("Failed: ${e.message}") }
                }
            }

            val priceSeries   = mutableMapOf<String, List<Double>>()
            val currentPrices = mutableMapOf<String, Double>()

            for ((symbol, d) in priceDeferred) {
                val r = d.await()
                if (r is ApiResult.Success && r.data.closePrices.size >= 10) {
                    priceSeries[symbol] = r.data.closePrices
                }
            }
            for ((symbol, d) in quoteDeferred) {
                val r = d.await()
                if (r is ApiResult.Success) currentPrices[symbol] = r.data.price
            }

            if (priceSeries.isEmpty()) {
                _state.value = _state.value.copy(
                    isSimulating    = false,
                    simulationError = "Could not load price history. Check your connection and try again."
                )
                return@launch
            }

            val sharesMap = holdings.associate { it.symbol to it.shares }

            val snapshots = holdings.mapNotNull { h ->
                val price = currentPrices[h.symbol] ?: return@mapNotNull null
                HoldingSnapshot(
                    symbol        = h.symbol,
                    shares        = h.shares,
                    currentPrice  = price,
                    purchasePrice = h.purchasePrice,
                    currentValue  = h.shares * price,
                    gainLoss      = (price - h.purchasePrice) * h.shares,
                    gainLossPct   = if (h.purchasePrice > 0)
                        ((price - h.purchasePrice) / h.purchasePrice) * 100.0 else 0.0
                )
            }

            val currentPortfolioValue = snapshots.sumOf { it.currentValue }
            val totalCostBasis        = holdings.sumOf { it.shares * it.purchasePrice }
            val totalGainLoss         = currentPortfolioValue - totalCostBasis
            val totalGainLossPct      = if (totalCostBasis > 0) (totalGainLoss / totalCostBasis) * 100.0 else 0.0

            val blended = PortfolioBlender.blend(priceSeries, sharesMap)

            if (blended.size < 20) {
                _state.value = _state.value.copy(
                    isSimulating    = false,
                    simulationError = "Not enough shared price history across your holdings."
                )
                return@launch
            }


            val twoYearGainPct = ((blended.last() / blended.first()) - 1.0) * 100.0
            val horizon        = _state.value.selectedHorizon
            val horizonRatio   = horizon.weeks.toDouble() / 104.0 // 104 weeks = 2 years
            val scaledTargetPct = (twoYearGainPct * horizonRatio).coerceAtLeast(1.0)

            val blendedCurrentValue = blended.last()
            val targetValue = blendedCurrentValue * (1.0 + scaledTargetPct / 100.0)

            if (targetValue <= blendedCurrentValue) {
                _state.value = _state.value.copy(
                    isSimulating    = false,
                    simulationError = "Historical data shows negative 2-year trend. Simulation not available."
                )
                return@launch
            }

            val simulation = AnalyticsEngine.compute(
                prices       = blended,
                currentPrice = blendedCurrentValue,
                targetPrice  = targetValue,
                horizonDays  = horizon.weeks,
                symbol       = "PORTFOLIO"
            )

            _state.value = _state.value.copy(
                isSimulating    = false,
                simulationError = if (simulation == null) "Not enough historical overlap. Try adding stocks with longer history." else null,
                result          = PortfolioResult(
                    currentValue      = currentPortfolioValue,
                    totalGainLoss     = totalGainLoss,
                    totalGainLossPct  = totalGainLossPct,
                    holdings          = snapshots,
                    simulation        = simulation,
                    historicalGainPct = twoYearGainPct,
                    scaledTargetPct   = scaledTargetPct
                )
            )
            Firebase.analytics.logEvent("portfolio_simulation_run") {
                param("holding_count", holdings.size.toLong())
                param("horizon", horizon.label)
                param("probability", simulation.probabilityPct.toLong())
            }
        }
    }


    private fun loadAll() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val holdingsDeferred  = async { portfolioRepo.getHoldings() }
            val watchlistDeferred = async { watchlistRepo.getWatchlistSymbols() }

            val holdings = when (val r = holdingsDeferred.await()) {
                is ApiResult.Success -> r.data
                is ApiResult.Error   -> {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = r.message)
                    return@launch
                }
            }

            val watchlist = when (val r = watchlistDeferred.await()) {
                is ApiResult.Success -> r.data
                is ApiResult.Error   -> emptyList()
            }

            _state.value = _state.value.copy(
                isLoading        = false,
                holdings         = holdings,
                watchlistSymbols = watchlist,
                result           = null,
                isSaving         = false
            )
        }
    }
}
