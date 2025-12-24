package com.example.marketlens.data.model

data class MarketIndex(
    val name: String,
    val currentValue: Double,
    val percentChange: Double,
    val isUp: Boolean
)