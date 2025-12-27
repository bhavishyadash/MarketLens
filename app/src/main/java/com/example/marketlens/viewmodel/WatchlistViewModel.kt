package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WatchlistViewModel : ViewModel() {

    private val _state = MutableStateFlow(WatchlistState(isLoading = true))
    val state: StateFlow<WatchlistState> = _state.asStateFlow()

    init {
        loadFakeWatchlist()
    }

    private fun loadFakeWatchlist() {
        val items = listOf(
            WatchlistRowUi("AAPL", 187.23, 1.12),
            WatchlistRowUi("NVDA", 890.22, 2.41),
            WatchlistRowUi("GOOGL", 141.88, 0.54)
        )

        _state.value = WatchlistState(
            isLoading = false,
            errorMessage = null,
            items = items
        )
    }
}