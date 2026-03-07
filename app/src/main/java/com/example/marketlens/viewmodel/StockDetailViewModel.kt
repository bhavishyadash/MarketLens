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
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.NewsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class StockDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repo:     MarketRepository = AppContainer.repository,
    private val newsRepo: NewsRepository   = AppContainer.newsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StockDetailState())
    val state: StateFlow<StockDetailState> = _state.asStateFlow()

    private val symbol: String = savedStateHandle["symbol"] ?: "UNKNOWN"

    init { loadAll(symbol, Timeframe.ONE_MONTH) }

    fun onTimeframeSelected(timeframe: Timeframe) {
        _state.value = _state.value.copy(
            selectedTimeframe = timeframe,
            isCandleLoading   = true,
            candleError       = null
        )
        loadCandle(symbol, timeframe)
    }

    fun onTargetPriceChanged(input: String) {
        val filtered = input.filter { it.isDigit() || it == '.' }
        _state.value = _state.value.copy(
            targetPriceInput = filtered,
            analyticsResult  = null,
            analyticsError   = null
        )
    }

    fun onHorizonSelected(horizon: Horizon) {
        _state.value = _state.value.copy(
            selectedHorizon = horizon,
            analyticsResult = null,
            analyticsError  = null
        )
    }


    fun onCalculate() {
        val targetPrice = _state.value.targetPriceInput.toDoubleOrNull()
        if (targetPrice == null || targetPrice <= 0) {
            _state.value = _state.value.copy(analyticsError = "Please enter a valid target price")
            return
        }

        val currentPrice = _state.value.price
        if (currentPrice <= 0) {
            _state.value = _state.value.copy(analyticsError = "Current price not loaded yet")
            return
        }

        _state.value = _state.value.copy(isAnalyticsLoading = true, analyticsError = null)

        viewModelScope.launch {
            // Reuse already-loaded candle data if available, otherwise fetch fresh
            val prices = if (_state.value.candle?.hasData == true) {
                _state.value.candle!!.closePrices
            } else {
                val now    = Instant.now().epochSecond
                val result = repo.getCandles(symbol, "D", now - (365 * 86400L), now)
                when (result) {
                    is ApiResult.Success -> result.data.closePrices
                    is ApiResult.Error   -> {
                        _state.value = _state.value.copy(
                            isAnalyticsLoading = false,
                            analyticsError     = "Could not load price history. Try selecting 1Y chart first."
                        )
                        return@launch
                    }
                }
            }

            val horizon = _state.value.selectedHorizon
            val result  = AnalyticsEngine.compute(
                prices       = prices,
                currentPrice = currentPrice,
                targetPrice  = targetPrice,
                horizonDays  = horizon.days,
                symbol       = symbol
            )

            _state.value = if (result == null) {
                _state.value.copy(
                    isAnalyticsLoading = false,
                    analyticsError     = "Not enough data. Tap 1Y on the chart first, then calculate."
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
            val quoteDeferred   = async { repo.getQuote(symbol) }
            val candleDeferred  = async {
                val now = Instant.now().epochSecond
                repo.getCandles(symbol, timeframe.resolution, now - (timeframe.daysBack * 86400L), now)
            }
            val profileDeferred = async { repo.getStockProfile(symbol) }
            val newsDeferred    = async { newsRepo.getStockNews(symbol) }

            val quoteResult   = quoteDeferred.await()
            val candleResult  = candleDeferred.await()
            val profileResult = profileDeferred.await()
            val newsResult    = newsDeferred.await()

            when (quoteResult) {
                is ApiResult.Error -> _state.value = StockDetailState(
                    symbol = symbol, isLoading = false, errorMessage = quoteResult.message
                )
                is ApiResult.Success -> {
                    val q = quoteResult.data
                    _state.value = StockDetailState(
                        symbol            = q.symbol,
                        name              = (profileResult as? ApiResult.Success)?.data?.name ?: q.symbol,
                        price             = q.price,
                        percentChange     = q.percentChange,
                        candle            = (candleResult as? ApiResult.Success)?.data,
                        candleError       = (candleResult as? ApiResult.Error)?.message,
                        selectedTimeframe = timeframe,
                        isCandleLoading   = false,
                        profile           = (profileResult as? ApiResult.Success)?.data,
                        profileError      = (profileResult as? ApiResult.Error)?.message,
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
                    newsRepo         = AppContainer.newsRepository
                )
            }
        }
    }
}