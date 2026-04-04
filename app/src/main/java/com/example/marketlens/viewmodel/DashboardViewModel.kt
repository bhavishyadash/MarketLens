package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.model.MarketIndex
import com.example.marketlens.data.model.MarketMover
import com.example.marketlens.data.model.WatchlistItem
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.WatchlistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repo:          MarketRepository   = AppContainer.repository,
    private val watchlistRepo: WatchlistRepository = AppContainer.watchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState(isLoading = true))
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val indexSymbols = mapOf("SPY" to "S&P 500", "QQQ" to "NASDAQ", "DIA" to "Dow Jones")
    private val moverSymbols = listOf("AAPL", "NVDA", "TSLA", "MSFT")

    init {
        loadDashboard()

        // When watchlist changes, silently refresh just the watchlist preview
        // without reloading the full dashboard (no spinner, no flicker)
        viewModelScope.launch {
            AppContainer.watchlistChanged.collect {
                refreshWatchlistPreviewSilently()
            }
        }
    }

    fun refresh() { loadDashboard() }

    // Silently updates only the watchlist preview section of the dashboard
    private suspend fun refreshWatchlistPreviewSilently() {
        val symbols = when (val r = watchlistRepo.getWatchlistSymbols()) {
            is ApiResult.Success -> r.data.take(5)
            is ApiResult.Error   -> return
        }

        val watchlistPreview = symbols
            .map { symbol -> async { repo.getQuote(symbol) } }
            .mapNotNull { (it.await() as? ApiResult.Success)?.data }
            .map { WatchlistItem(it.symbol, it.price, it.percentChange) }

        // Only update the watchlist preview — everything else stays the same
        _state.value = _state.value.copy(watchlistPreview = watchlistPreview)
    }

    private fun loadDashboard() {
        _state.value = DashboardState(isLoading = true)
        viewModelScope.launch {
            val indexDeferreds = indexSymbols.keys.map { it to async { repo.getQuote(it) } }
            val moverDeferreds = moverSymbols.map { async { repo.getQuote(it) } }

            val watchlistSymbols = when (val r = watchlistRepo.getWatchlistSymbols()) {
                is ApiResult.Success -> r.data.take(5)
                is ApiResult.Error   -> emptyList()
            }
            val watchlistDeferreds = watchlistSymbols.map { it to async { repo.getQuote(it) } }

            val indices = indexDeferreds.mapNotNull { (symbol, d) ->
                (d.await() as? ApiResult.Success)?.data?.let {
                    MarketIndex(indexSymbols[symbol] ?: symbol, it.price, it.percentChange, it.percentChange >= 0)
                }
            }

            val allQuotes = moverDeferreds.mapNotNull { (it.await() as? ApiResult.Success)?.data }
            val topGainer = allQuotes.maxByOrNull { it.percentChange }?.let { MarketMover(it.symbol, it.price, it.percentChange) }
            val topLoser  = allQuotes.minByOrNull { it.percentChange }?.let { MarketMover(it.symbol, it.price, it.percentChange) }

            val watchlistPreview = watchlistDeferreds.mapNotNull { (_, d) ->
                (d.await() as? ApiResult.Success)?.data?.let {
                    WatchlistItem(it.symbol, it.price, it.percentChange)
                }
            }

            if (indices.isEmpty()) {
                _state.value = DashboardState(isLoading = false, errorMessage = "Could not load market data.")
            } else {
                _state.value = DashboardState(
                    indices          = indices,
                    topGainer        = topGainer,
                    topLoser         = topLoser,
                    watchlistPreview = watchlistPreview,
                    recentAlerts     = emptyList(),
                    isLoading        = false
                )
            }
        }
    }
}
