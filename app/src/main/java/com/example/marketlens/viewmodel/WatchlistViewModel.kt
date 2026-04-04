package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.WatchlistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WatchlistViewModel(
    private val repo:          MarketRepository   = AppContainer.repository,
    private val watchlistRepo: WatchlistRepository = AppContainer.watchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistState(isLoading = true))
    val state: StateFlow<WatchlistState> = _state.asStateFlow()

    init {
        loadWatchlist()

        // Listen for any watchlist changes broadcast from any screen
        // When triggered, silently refresh the symbol list only — no loading spinner
        viewModelScope.launch {
            AppContainer.watchlistChanged.collect {
                refreshSymbolsSilently()
            }
        }
    }

    // Called when user navigates to the Watchlist tab — no spinner shown
    fun onScreenVisible() {
        viewModelScope.launch {
            refreshSymbolsSilently()
        }
    }

    fun removeSymbol(symbol: String) {
        viewModelScope.launch {
            watchlistRepo.removeSymbol(symbol)
            AppContainer.notifyWatchlistChanged()
        }
    }

    // Full load with spinner — only used on first open
    private fun loadWatchlist() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            fetchAndUpdate(showLoading = true)
        }
    }

    // Silent refresh — updates list without showing any loading indicator
    private suspend fun refreshSymbolsSilently() {
        fetchAndUpdate(showLoading = false)
    }

    private suspend fun fetchAndUpdate(showLoading: Boolean) {
        if (showLoading) {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        }

        val symbolsResult = watchlistRepo.getWatchlistSymbols()
        val symbols = when (symbolsResult) {
            is ApiResult.Success -> symbolsResult.data
            is ApiResult.Error   -> {
                if (showLoading) {
                    _state.value = WatchlistState(isLoading = false, errorMessage = symbolsResult.message)
                }
                return
            }
        }

        if (symbols.isEmpty()) {
            _state.value = WatchlistState(isLoading = false)
            return
        }

        // Fetch live prices for each symbol in parallel
        val items = symbols
            .map { symbol -> async { repo.getQuote(symbol) } }
            .mapNotNull { (it.await() as? ApiResult.Success)?.data }
            .map { WatchlistRowUi(it.symbol, it.price, it.percentChange) }

        _state.value = WatchlistState(
            items        = items,
            isLoading    = false,
            errorMessage = if (items.isEmpty() && showLoading) "Could not load price data." else null
        )
    }
}
