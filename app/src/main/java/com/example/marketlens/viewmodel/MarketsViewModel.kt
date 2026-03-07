package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.MarketRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketsViewModel(
    private val repo: MarketRepository = AppContainer.repository
) : ViewModel() {

    private val _state = MutableStateFlow(MarketsState(isLoading = true))
    val state: StateFlow<MarketsState> = _state.asStateFlow()

    private val defaultSymbols = listOf("AAPL", "MSFT", "NVDA", "TSLA", "AMZN", "GOOGL", "META", "JPM", "V", "UNH", "WMT", "XOM")

    init { loadMarkets() }

    fun refresh() { loadMarkets() }

    private fun loadMarkets() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val stocks = defaultSymbols
                .map { async { repo.getQuote(it) } }
                .mapNotNull { (it.await() as? ApiResult.Success)?.data }
                .map { StockRowUi(it.symbol, it.name, it.price, it.percentChange) }

            if (stocks.isEmpty()) {
                _state.value = MarketsState(isLoading = false, errorMessage = "Could not load market data. Check your connection.")
            } else {
                _state.value = MarketsState(allStocks = stocks, filteredStocks = stocks, isLoading = false)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        val q = newQuery.trim()
        val filtered = if (q.isEmpty()) _state.value.allStocks
        else _state.value.allStocks.filter {
            it.symbol.contains(q, ignoreCase = true) || it.name.contains(q, ignoreCase = true)
        }
        _state.value = _state.value.copy(query = newQuery, filteredStocks = filtered)
    }
}