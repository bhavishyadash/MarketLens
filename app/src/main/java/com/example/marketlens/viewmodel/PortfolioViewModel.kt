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

class PortfolioViewModel(
    private val portfolioRepo: PortfolioRepository  = AppContainer.portfolioRepository,
    private val marketRepo:    MarketRepository      = AppContainer.repository,
    private val watchlistRepo: WatchlistRepository   = AppContainer.watchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioState(isLoading = true))
    val state: StateFlow<PortfolioState> = _state.asStateFlow()

    init { loadAll() }

    // ── Dropdown ──────────────────────────────────────────────────────────────

    fun onDropdownExpand()   { _state.value = _state.value.copy(isDropdownExpanded = true) }
    fun onDropdownDismiss()  { _state.value = _state.value.copy(isDropdownExpanded = false) }

    fun onSymbolSelected(symbol: String) {
        _state.value = _state.value.copy(
            formSymbol          = symbol,
            isDropdownExpanded  = false,
            formError           = null
        )
    }

    // ── Form ──────────────────────────────────────────────────────────────────

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

    // ── Add holding ───────────────────────────────────────────────────────────

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
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun onDeleteHolding(symbol: String) {
        viewModelScope.launch {
            portfolioRepo.deleteHolding(symbol)
            loadAll()
        }
    }

    // ── Simulate ──────────────────────────────────────────────────────────────

    /*
        Auto-target approach:
        Instead of asking the user to type a gain %, we compute the portfolio's
        actual 2-year historical performance from the blended price series.
        The simulation then asks: given your horizon, what's the probability
        this portfolio achieves that same historical rate of gain?

        This is more meaningful than an arbitrary user-typed number.
    */
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

            // Fetch price history + quotes in parallel for all holdings
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

            // Build holding snapshots
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

            // Blend all price series into one portfolio value series
            val blended = PortfolioBlender.blend(priceSeries, sharesMap)

            if (blended.size < 20) {
                _state.value = _state.value.copy(
                    isSimulating    = false,
                    simulationError = "Not enough shared price history across your holdings."
                )
                return@launch
            }

            /*
                Auto-compute target from 2-year historical gain.
                If the blended portfolio grew 40% over 2 years historically,
                we ask: what's the probability it gains 40% in [horizon]?

                We scale by horizon ratio so targets are proportional:
                  2Y gain = 40%, 6M horizon ratio = 6/24 = 0.25
                  6M target = 40% * 0.25 = 10%
            */
            val twoYearGainPct = ((blended.last() / blended.first()) - 1.0) * 100.0
            val horizon        = _state.value.selectedHorizon
            val horizonRatio   = horizon.weeks.toDouble() / 104.0 // 104 weeks = 2 years
            val scaledTargetPct = (twoYearGainPct * horizonRatio).coerceAtLeast(1.0)

            val blendedCurrentValue = blended.last()
            val targetValue = blendedCurrentValue * (1.0 + scaledTargetPct / 100.0)

            // Guard: target must be above current for AnalyticsEngine
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
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private fun loadAll() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            // Load holdings and watchlist symbols in parallel
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
