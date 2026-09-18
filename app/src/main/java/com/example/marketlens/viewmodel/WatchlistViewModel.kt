package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.WatchlistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

        viewModelScope.launch {
            AppContainer.watchlistChanged.collect {
                refreshSymbolsSilently()
            }
        }
    }

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

    private fun loadWatchlist() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            fetchAndUpdate(showLoading = true)
        }
    }

    private suspend fun refreshSymbolsSilently() {
        fetchAndUpdate(showLoading = false)
    }

    private suspend fun fetchAndUpdate(showLoading: Boolean) = coroutineScope {
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
                return@coroutineScope
            }
        }

        if (symbols.isEmpty()) {
            _state.value = WatchlistState(isLoading = false)
            return@coroutineScope
        }

        val quoteResults = symbols
            .map { symbol -> symbol to async { repo.getQuote(symbol) } }
            .map { (symbol, deferred) -> symbol to deferred.await() }

        val freshItemsBySymbol = quoteResults.mapNotNull { (_, result) ->
            (result as? ApiResult.Success<StockQuote>)?.data?.let { quote ->
                quote.symbol to WatchlistRowUi(quote.symbol, quote.price, quote.percentChange)
            }
        }.toMap()

        if (freshItemsBySymbol.isEmpty()) {
            if (showLoading || _state.value.items.isEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Could not load watchlist prices. Check your connection and try again."
                )
            } else {
                // A silent refresh should never erase prices that were already visible.
                _state.value = _state.value.copy(isLoading = false)
            }
            return@coroutineScope
        }

        val existingItemsBySymbol = _state.value.items.associateBy { it.symbol }
        val items = symbols.mapNotNull { symbol ->
            freshItemsBySymbol[symbol]
                ?: if (showLoading) null else existingItemsBySymbol[symbol]
        }

        _state.value = WatchlistState(items = items, isLoading = false)
    }
}
