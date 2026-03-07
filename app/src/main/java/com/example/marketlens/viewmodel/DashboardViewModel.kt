package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.model.AlertItem
import com.example.marketlens.data.model.MarketIndex
import com.example.marketlens.data.model.MarketMover
import com.example.marketlens.data.model.WatchlistItem
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.MarketRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repo: MarketRepository = AppContainer.repository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState(isLoading = true))
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    // SPY = S&P 500, QQQ = NASDAQ, DIA = Dow Jones (ETF proxies — Finnhub free tier)
    private val indexSymbols     = mapOf("SPY" to "S&P 500", "QQQ" to "NASDAQ", "DIA" to "Dow Jones")
    private val watchlistSymbols = listOf("AAPL", "MSFT", "GOOGL")
    private val marketsSymbols   = listOf("AAPL", "MSFT", "NVDA", "TSLA", "AMZN", "GOOGL", "META")

    init { loadDashboard() }

    fun refresh() { loadDashboard() }

    private fun loadDashboard() {
        _state.value = DashboardState(isLoading = true)
        viewModelScope.launch {
            val indexDeferreds     = indexSymbols.keys.map { it to async { repo.getQuote(it) } }
            val watchlistDeferreds = watchlistSymbols.map { it to async { repo.getQuote(it) } }
            val marketsDeferreds   = marketsSymbols.map { it to async { repo.getQuote(it) } }

            val indices = indexDeferreds.mapNotNull { (symbol, d) ->
                (d.await() as? ApiResult.Success)?.data?.let {
                    MarketIndex(indexSymbols[symbol] ?: symbol, it.price, it.percentChange, it.percentChange >= 0)
                }
            }
            val watchlistPreview = watchlistDeferreds.mapNotNull { (_, d) ->
                (d.await() as? ApiResult.Success)?.data?.let {
                    WatchlistItem(it.symbol, it.price, it.percentChange)
                }
            }
            val allQuotes = marketsDeferreds.mapNotNull { (_, d) -> (d.await() as? ApiResult.Success)?.data }
            val topGainer = allQuotes.maxByOrNull { it.percentChange }?.let { MarketMover(it.symbol, it.price, it.percentChange) }
            val topLoser  = allQuotes.minByOrNull { it.percentChange }?.let { MarketMover(it.symbol, it.price, it.percentChange) }

            if (indices.isEmpty() && watchlistPreview.isEmpty()) {
                _state.value = DashboardState(isLoading = false, errorMessage = "Could not load market data. Check your connection.")
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