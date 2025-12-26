package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import com.example.marketlens.data.model.AlertItem
import com.example.marketlens.data.model.MarketIndex
import com.example.marketlens.data.model.MarketMover
import com.example.marketlens.data.model.WatchlistItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Dashboard screen.
 * Provides DashboardState to the UI.
 */
class DashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(DashboardState(isLoading = true))
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadFakeDashboardData()
    }

    private fun loadFakeDashboardData() {
        _state.value = DashboardState(
            indices = listOf(
                MarketIndex("NASDAQ", 15234.22, 1.25, isUp = true),
                MarketIndex("S&P 500", 4982.10, -0.42, isUp = false),
                MarketIndex("Dow Jones", 38912.44, 0.63, isUp = true)
            ),
            topGainer = MarketMover(
                symbol = "NVDA",
                price = 890.22,
                percentChange = 4.18
            ),
            topLoser = MarketMover(
                symbol = "TSLA",
                price = 242.88,
                percentChange = -3.72
            ),
            watchlistPreview = listOf(
                WatchlistItem("AAPL", 187.23, 1.12),
                WatchlistItem("MSFT", 412.10, -0.38),
                WatchlistItem("GOOGL", 141.88, 0.54)
            ),
            recentAlerts = listOf(
                AlertItem(
                    message = "NASDAQ moved over 500 points in the last 30 minutes",
                    timestamp = "10 min ago"
                )
            ),
            isLoading = false,
            errorMessage = null
        )
    }
}