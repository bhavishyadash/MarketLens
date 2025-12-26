package com.example.marketlens.viewmodel

data class MarketsState(
    val query: String = "",
    val allStocks: List<StockRowUi> = emptyList(),
    val filteredStocks: List<StockRowUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class StockRowUi(
    val symbol: String,
    val name: String,
    val price: Double,
    val percentChange: Double
) {
    val isUp: Boolean get() = percentChange >= 0.0
}