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
    private val repo:          MarketRepository  = AppContainer.repository,
    private val watchlistRepo: WatchlistRepository = AppContainer.watchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistState(isLoading = true))
    val state: StateFlow<WatchlistState> = _state.asStateFlow()

    init { loadWatchlist() }

    fun refresh() { loadWatchlist() }

    fun removeSymbol(symbol: String) {
        viewModelScope.launch {
            watchlistRepo.removeSymbol(symbol)
            // Refresh list after removal
            loadWatchlist()
        }
    }

    private fun loadWatchlist() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            // Step 1: get list of symbols from Firestore
            val symbolsResult = watchlistRepo.getWatchlistSymbols()
            val symbols = when (symbolsResult) {
                is ApiResult.Success -> symbolsResult.data
                is ApiResult.Error   -> {
                    _state.value = WatchlistState(
                        isLoading    = false,
                        errorMessage = symbolsResult.message
                    )
                    return@launch
                }
            }

            if (symbols.isEmpty()) {
                _state.value = WatchlistState(isLoading = false)
                return@launch
            }

            // Step 2: fetch live prices for each symbol in parallel
            val items = symbols
                .map { symbol -> async { repo.getQuote(symbol) } }
                .mapNotNull { (it.await() as? ApiResult.Success)?.data }
                .map { WatchlistRowUi(it.symbol, it.price, it.percentChange) }

            _state.value = WatchlistState(
                items        = items,
                isLoading    = false,
                errorMessage = if (items.isEmpty()) "Could not load price data." else null
            )
        }
    }
}