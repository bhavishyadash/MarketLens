package com.example.marketlens.viewmodel

data class WatchlistState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<WatchlistRowUi> = emptyList()
)

data class WatchlistRowUi(
    val symbol: String,
    val price: Double,
    val percentChange: Double
) {
    val isUp: Boolean get() = percentChange >= 0.0
}