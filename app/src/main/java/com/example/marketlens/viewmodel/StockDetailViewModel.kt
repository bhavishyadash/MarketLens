package com.example.marketlens.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StockDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(StockDetailState())
    val state: StateFlow<StockDetailState> = _state.asStateFlow()

    private val symbol: String = savedStateHandle["symbol"] ?: "UNKNOWN"

    init {
        loadFake(symbol)
    }

    private fun loadFake(symbol: String) {
        // Same fake universe as Markets for now
        val fake = listOf(
            StockRowUi("AAPL", "Apple Inc.", 187.23, 1.12),
            StockRowUi("MSFT", "Microsoft", 412.10, -0.38),
            StockRowUi("NVDA", "NVIDIA", 890.22, 2.41),
            StockRowUi("TSLA", "Tesla", 242.88, -3.72),
            StockRowUi("AMZN", "Amazon", 156.44, 0.65),
            StockRowUi("GOOGL", "Alphabet", 141.88, 0.54),
            StockRowUi("META", "Meta", 355.70, -0.92)
        ).associateBy { it.symbol }

        val item = fake[symbol]

        _state.value = if (item != null) {
            StockDetailState(
                symbol = item.symbol,
                price = item.price,
                percentChange = item.percentChange,
                isLoading = false,
                errorMessage = null
            )
        } else {
            StockDetailState(
                symbol = symbol,
                isLoading = false,
                errorMessage = "No data for $symbol"
            )
        }
    }
}