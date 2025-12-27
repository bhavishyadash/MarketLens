package com.example.marketlens.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuoteDto(
    val symbol: String,
    val price: Double,
    val percentChange: Double
)