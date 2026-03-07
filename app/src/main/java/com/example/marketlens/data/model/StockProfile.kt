package com.example.marketlens.data.model

data class StockProfile(
    val symbol:             String,
    val name:               String,
    val exchange:           String,
    val industry:           String,
    val marketCapFormatted: String,
    val week52High:         Double?,
    val week52Low:          Double?,
    val peRatio:            Double?,
    val beta:               Double?
)