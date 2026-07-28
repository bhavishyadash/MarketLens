package com.example.marketlens.viewmodel

import com.example.marketlens.data.model.MarketIndex
import com.example.marketlens.data.model.MarketMover
import com.example.marketlens.data.model.WatchlistItem

data class DashboardState(
    val indices: List<MarketIndex> = emptyList(),
    val topGainer: MarketMover? = null,
    val topLoser: MarketMover? = null,
    val watchlistPreview: List<WatchlistItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)