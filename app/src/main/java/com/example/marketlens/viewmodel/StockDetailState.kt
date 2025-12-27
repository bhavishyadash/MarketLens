package com.example.marketlens.viewmodel

data class StockDetailState(
    val symbol: String = "",
    val price: Double = 0.0,
    val percentChange: Double = 0.0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)