package com.example.marketlens.data.model

data class StockQuote(
    val symbol: String,
    val name: String,
    val price: Double,
    val percentChange: Double
)