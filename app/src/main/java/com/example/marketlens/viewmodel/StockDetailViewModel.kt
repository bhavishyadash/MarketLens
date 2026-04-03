package com.example.marketlens.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.marketlens.analytics.AnalyticsEngine
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.model.AlertDirection
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.AlertRepository
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.NewsRepository
import com.example.marketlens.data.repository.WatchlistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class StockDetailViewModel(
    savedStateHandle:  SavedStateHandle,
    private val repo:          MarketRepository   = AppContainer.repository,
    private val newsRepo:      NewsRepository      = AppContainer.newsRepository,
    private val watchlistRepo: WatchlistRepository = AppContainer.watchlistRepository,
    private val alertRepo:     AlertRepository     = AppContainer.alertRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StockDetailState())
    val state: StateFlow<StockDetailState> = _state.asStateFlow()

    private val symbol: String = savedStateHandle["symbol"] ?: "UNKNOWN"

    init { loadAll(symbol, Timeframe.ONE_MONTH) }

    fun onToggleWatchlist() {
        viewModelScope.launch {
            val current = _state.value.isInWatchlist
            if (current) watchlistRepo.removeSymbol(symbol) else watchlistRepo.addSymbol(symbol)
            _state.value = _state.value.copy(isInWatchlist = !current)
        }
    }

    fun onAlertPriceChanged(input: String) {
        val filtered = input.filter { it.isDigit() || it == '.' }
        _state.value = _state.value.copy(alertPriceInput = filtered, alertSetSuccess = false, alertError = null)
    }

    fun onSetAlert(direction: AlertDirection) {
        val targetPrice = _state.value.alertPriceInput.toDoubleOrNull()
        if (targetPrice == null || targetPrice <= 0) {
            _state.value = _state.value.copy(alertError = "Please enter a valid price")
            return
        }
        viewModelScope.launch {
            when (val result = alertRepo.addAlert(symbol, targetPrice, direction)) {
                is ApiResult.Success -> _state.value = _state.value.copy(alertSetSuccess = true, alertError = null, alertPriceInput = "")
                is ApiResult.Error   -> _state.value = _state.value.copy(alertError = result.message)
            }
        }
    }

    fun onTimeframeSelected(timeframe: Timeframe) {
        _state.value = _state.value.copy(selectedTimeframe = timeframe, isCandleLoading = true, candleError = null)
        loadCandle(symbol, timeframe)
    }

    fun onTargetPriceChanged(input: String) {
        val filtered = input.filter { it.isDigit() || it == '.' }
        _state.value = _state.value.copy(targetPriceInput = filtered, analyticsResult = null, analyticsError = null)
    }

    fun onHorizonSelected(horizon: Horizon) {
        _state.value = _state.value.copy(selectedHorizon = horizon, analyticsResult = null, analyticsError = null)
    }

    fun onCalculate() {
        val targetPrice  = _state.value.targetPriceInput.toDoubleOrNull()
        val currentPrice = _state.value.price

        if (targetPrice == null || targetPrice <= 0) {
            _state.value = _state.value.copy(analyticsError = "Please enter a valid target price")
            return
        }
        if (currentPrice <= 0) {
            _state.value = _state.value.copy(analyticsError = "Current price not loaded yet")
            return
        }
        if (targetPrice <= currentPrice) {
            _state.value = _state.value.copy(
                analyticsError = "Target must be above current price (${"%.2f".format(currentPrice)})"
            )
            return
        }

        _state.value = _state.value.copy(isAnalyticsLoading = true, analyticsError = null)

        viewModelScope.launch {
            val now         = Instant.now().epochSecond
            val twoYearsAgo = now - (2 * 365 * 86400L)
            val candleResult = repo.getCandles(symbol, "W", twoYearsAgo, now)

            val prices = when (candleResult) {
                is ApiResult.Success -> candleResult.data.closePrices
                is ApiResult.Error   -> {
                    _state.value = _state.value.copy(
                        isAnalyticsLoading = false,
                        analyticsError     = "Could not load price history"
                    )
                    return@launch
                }
            }

            val horizon      = _state.value.selectedHorizon
            val horizonWeeks = (horizon.days / 7).coerceAtLeast(4)

            val result = AnalyticsEngine.compute(
                prices       = prices,
                currentPrice = currentPrice,
                targetPrice  = targetPrice,
                horizonDays  = horizonWeeks,
                symbol       = symbol
            )

            _state.value = if (result == null) {
                _state.value.copy(
                    isAnalyticsLoading = false,
                    analyticsError     = "Not enough historical data for this horizon"
                )
            } else {
                _state.value.copy(
                    analyticsResult    = result,
                    isAnalyticsLoading = false,
                    analyticsError     = null
                )
            }
        }
    }

    private fun loadAll(symbol: String, timeframe: Timeframe) {
        _state.value = StockDetailState(isLoading = true)
        viewModelScope.launch {
            val quoteDeferred     = async { repo.getQuote(symbol) }
            val candleDeferred    = async {
                val now = Instant.now().epochSecond
                repo.getCandles(symbol, timeframe.resolution, now - (timeframe.daysBack * 86400L), now)
            }
            val profileDeferred   = async { repo.getStockProfile(symbol) }
            val newsDeferred      = async { newsRepo.getStockNews(symbol) }
            val watchlistDeferred = async { watchlistRepo.isInWatchlist(symbol) }

            val quoteResult   = quoteDeferred.await()
            val candleResult  = candleDeferred.await()
            val profileResult = profileDeferred.await()
            val newsResult    = newsDeferred.await()
            val inWatchlist   = watchlistDeferred.await()

            when (quoteResult) {
                is ApiResult.Error -> _state.value = StockDetailState(symbol = symbol, isLoading = false, errorMessage = quoteResult.message)
                is ApiResult.Success -> {
                    val q = quoteResult.data
                    _state.value = StockDetailState(
                        symbol            = q.symbol,
                        name              = (profileResult as? ApiResult.Success)?.data?.name ?: q.symbol,
                        price             = q.price,
                        percentChange     = q.percentChange,
                        isInWatchlist     = inWatchlist,
                        candle            = (candleResult as? ApiResult.Success)?.data,
                        candleError       = (candleResult as? ApiResult.Error)?.message,
                        selectedTimeframe = timeframe,
                        isCandleLoading   = false,
                        profile           = (profileResult as? ApiResult.Success)?.data,
                        isProfileLoading  = false,
                        news              = (newsResult as? ApiResult.Success)?.data ?: emptyList(),
                        newsError         = (newsResult as? ApiResult.Error)?.message,
                        isNewsLoading     = false,
                        isLoading         = false
                    )
                }
            }
        }
    }

    private fun loadCandle(symbol: String, timeframe: Timeframe) {
        viewModelScope.launch {
            val now = Instant.now().epochSecond
            val result = repo.getCandles(symbol, timeframe.resolution, now - (timeframe.daysBack * 86400L), now)
            _state.value = _state.value.copy(
                candle          = (result as? ApiResult.Success)?.data,
                candleError     = (result as? ApiResult.Error)?.message,
                isCandleLoading = false
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StockDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repo             = AppContainer.repository,
                    newsRepo         = AppContainer.newsRepository,
                    watchlistRepo    = AppContainer.watchlistRepository,
                    alertRepo        = AppContainer.alertRepository
                )
            }
        }
    }
}
