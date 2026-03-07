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

class WatchlistViewModel(
    private val repo: MarketRepository = AppContainer.repository
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistState(isLoading = true))
    val state: StateFlow<WatchlistState> = _state.asStateFlow()

    private val watchlistSymbols = listOf("AAPL", "NVDA", "GOOGL", "MSFT", "TSLA")

    init { loadWatchlist() }

    fun refresh() { loadWatchlist() }

    private fun loadWatchlist() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val items = watchlistSymbols
                .map { async { repo.getQuote(it) } }
                .mapNotNull { (it.await() as? ApiResult.Success)?.data }
                .map { WatchlistRowUi(it.symbol, it.price, it.percentChange) }

            _state.value = WatchlistState(
                items        = items,
                isLoading    = false,
                errorMessage = if (items.isEmpty()) "Could not load watchlist data." else null
            )
        }
    }
}