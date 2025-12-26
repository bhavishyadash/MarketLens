package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MarketsViewModel : ViewModel() {

    private val _state = MutableStateFlow(MarketsState(isLoading = true))
    val state: StateFlow<MarketsState> = _state.asStateFlow()

    init {
        loadFakeMarkets()
    }

    private fun loadFakeMarkets() {
        val stocks = listOf(
            StockRowUi("AAPL", "Apple Inc.", 187.23, 1.12),
            StockRowUi("MSFT", "Microsoft", 412.10, -0.38),
            StockRowUi("NVDA", "NVIDIA", 890.22, 2.41),
            StockRowUi("TSLA", "Tesla", 242.88, -3.72),
            StockRowUi("AMZN", "Amazon", 156.44, 0.65),
            StockRowUi("GOOGL", "Alphabet", 141.88, 0.54),
            StockRowUi("META", "Meta", 355.70, -0.92)
        )

        _state.value = MarketsState(
            query = "",
            allStocks = stocks,
            filteredStocks = stocks,
            isLoading = false,
            errorMessage = null
        )
    }

    fun onQueryChange(newQuery: String) {
        val q = newQuery.trim()

        val filtered = if (q.isEmpty()) {
            _state.value.allStocks
        } else {
            _state.value.allStocks.filter {
                it.symbol.contains(q, ignoreCase = true) ||
                        it.name.contains(q, ignoreCase = true)
            }
        }

        _state.value = _state.value.copy(
            query = newQuery,
            filteredStocks = filtered
        )
    }
}