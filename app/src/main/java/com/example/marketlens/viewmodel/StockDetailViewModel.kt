package com.example.marketlens.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
    private val repo: MarketRepository   = AppContainer.repository,
    private val newsRepo: NewsRepository = AppContainer.newsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StockDetailState())
    val state: StateFlow<StockDetailState> = _state.asStateFlow()

    private val symbol: String = savedStateHandle["symbol"] ?: "UNKNOWN"

    init { loadAll(symbol, Timeframe.ONE_MONTH) }

    fun onTimeframeSelected(timeframe: Timeframe) {
        _state.value = _state.value.copy(selectedTimeframe = timeframe, isCandleLoading = true, candleError = null)
        loadCandle(symbol, timeframe)
    }

    private fun loadAll(symbol: String, timeframe: Timeframe) {
        _state.value = StockDetailState(isLoading = true)
        viewModelScope.launch {
            val quoteDeferred  = async { repo.getQuote(symbol) }
            val candleDeferred = async {
                val now = Instant.now().epochSecond
                repo.getCandles(symbol, timeframe.resolution, now - (timeframe.daysBack * 86400L), now)
            }
            val newsDeferred = async { newsRepo.getStockNews(symbol) }

            val quoteResult  = quoteDeferred.await()
            val candleResult = candleDeferred.await()
            val newsResult   = newsDeferred.await()

            when (quoteResult) {
                is ApiResult.Error -> _state.value = StockDetailState(
                    symbol = symbol, isLoading = false, errorMessage = quoteResult.message
                )
                is ApiResult.Success -> {
                    val q = quoteResult.data
                    _state.value = StockDetailState(
                        symbol            = q.symbol,
                        name              = q.name,
                        price             = q.price,
                        percentChange     = q.percentChange,
                        candle            = (candleResult as? ApiResult.Success)?.data,
                        candleError       = (candleResult as? ApiResult.Error)?.message,
                        selectedTimeframe = timeframe,
                        isCandleLoading   = false,
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