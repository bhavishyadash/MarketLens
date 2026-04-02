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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class PortfolioViewModel(
    private val portfolioRepo: PortfolioRepository = AppContainer.portfolioRepository,
    private val marketRepo:    MarketRepository     = AppContainer.repository
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioState(isLoading = true))
    val state: StateFlow<PortfolioState> = _state.asStateFlow()

    init { loadHoldings() }

    fun onShowForm()  { _state.value = _state.value.copy(showForm = true, formError = null) }
    fun onHideForm()  { _state.value = _state.value.copy(showForm = false, formSymbol = "", formShares = "", formPurchasePrice = "", formError = null) }

    fun onFormSymbolChanged(v: String)        { _state.value = _state.value.copy(formSymbol = v.uppercase().trim(), formError = null) }
    fun onFormSharesChanged(v: String)        { _state.value = _state.value.copy(formShares = v.filter { it.isDigit() || it == '.' }, formError = null) }
    fun onFormPurchasePriceChanged(v: String) { _state.value = _state.value.copy(formPurchasePrice = v.filter { it.isDigit() || it == '.' }, formError = null) }

    fun onTargetGainChanged(v: String)  { _state.value = _state.value.copy(targetGainPct = v.filter { it.isDigit() || it == '.' }, simulationError = null) }
    fun onHorizonSelected(h: PortfolioHorizon) { _state.value = _state.value.copy(selectedHorizon = h, simulationError = null) }

    fun onAddHolding() {
        val symbol        = _state.value.formSymbol
        val shares        = _state.value.formShares.toDoubleOrNull()
        val purchasePrice = _state.value.formPurchasePrice.toDoubleOrNull()

        if (symbol.isBlank()) {
            _state.value = _state.value.copy(formError = "Enter a stock symbol")
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
                is ApiResult.Success -> {
                    onHideForm()
                    loadHoldings()
                }
                is ApiResult.Error -> _state.value = _state.value.copy(isSaving = false, formError = "Could not save. Try again.")
            }
        }
    }

    fun onDeleteHolding(symbol: String) {
        viewModelScope.launch {
            portfolioRepo.deleteHolding(symbol)
            loadHoldings()
        }
    }

    fun onSimulate() {
        val holdings = _state.value.holdings
        if (holdings.isEmpty()) {
            _state.value = _state.value.copy(simulationError = "Add at least one holding first")
            return
        }

        val targetGainPct = _state.value.targetGainPct.toDoubleOrNull()
        if (targetGainPct == null || targetGainPct <= 0) {
            _state.value = _state.value.copy(simulationError = "Enter a valid target gain percentage")
            return
        }

        _state.value = _state.value.copy(isSimulating = true, simulationError = null)

        viewModelScope.launch {
            val now         = Instant.now().epochSecond
            val twoYearsAgo = now - (2 * 365 * 86400L)

            val priceSeriesDeferred = holdings.associate { holding ->
                holding.symbol to async {
                    marketRepo.getCandles(holding.symbol, "W", twoYearsAgo, now)
                }
            }

            val quoteDeferred = holdings.associate { holding ->
                holding.symbol to async { marketRepo.getQuote(holding.symbol) }
            }

            val priceSeries = mutableMapOf<String, List<Double>>()
            for ((symbol, deferred) in priceSeriesDeferred) {
                val result = deferred.await()
                if (result is ApiResult.Success && result.data.closePrices.isNotEmpty()) {
                    priceSeries[symbol] = result.data.closePrices
                }
            }

            val currentPrices = mutableMapOf<String, Double>()
            for ((symbol, deferred) in quoteDeferred) {
                val result = deferred.await()
                if (result is ApiResult.Success) {
                    currentPrices[symbol] = result.data.price
                }
            }

            if (priceSeries.isEmpty()) {
                _state.value = _state.value.copy(
                    isSimulating    = false,
                    simulationError = "Could not load price history. Check your connection."
                )
                return@launch
            }

            val sharesMap = holdings.associate { it.symbol to it.shares }
            val holdingSnapshots = holdings.mapNotNull { holding ->
                val currentPrice = currentPrices[holding.symbol] ?: return@mapNotNull null
                HoldingSnapshot(
                    symbol        = holding.symbol,
                    shares        = holding.shares,
                    currentPrice  = currentPrice,
                    purchasePrice = holding.purchasePrice,
                    currentValue  = holding.shares * currentPrice,
                    gainLoss      = (currentPrice - holding.purchasePrice) * holding.shares,
                    gainLossPct   = if (holding.purchasePrice > 0)
                        ((currentPrice - holding.purchasePrice) / holding.purchasePrice) * 100.0
                    else 0.0
                )
            }

            val currentPortfolioValue = holdingSnapshots.sumOf { it.currentValue }
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

            val blendedCurrentValue = blended.last()

            val targetValue = blendedCurrentValue * (1 + targetGainPct / 100.0)

            val horizon = _state.value.selectedHorizon

            val simulation = AnalyticsEngine.compute(
                prices       = blended,
                currentPrice = blendedCurrentValue,
                targetPrice  = targetValue,
                horizonDays  = horizon.weeks,
                symbol       = "PORTFOLIO"
            )

            _state.value = _state.value.copy(
                isSimulating = false,
                result       = PortfolioResult(
                    currentValue     = currentPortfolioValue,
                    totalGainLoss    = totalGainLoss,
                    totalGainLossPct = totalGainLossPct,
                    holdings         = holdingSnapshots,
                    simulation       = simulation
                ),
                simulationError = if (simulation == null) "Not enough historical overlap across your holdings." else null
            )
        }
    }

    private fun loadHoldings() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = portfolioRepo.getHoldings()) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    holdings  = result.data,
                    result    = null
                )
                is ApiResult.Error -> _state.value = _state.value.copy(
                    isLoading    = false,
                    errorMessage = result.message
                )
            }
        }
    }
}