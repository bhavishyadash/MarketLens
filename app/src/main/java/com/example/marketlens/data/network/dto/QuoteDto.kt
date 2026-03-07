package com.example.marketlens.data.network.dto

import com.squareup.moshi.Json

data class QuoteDto(
    @Json(name = "c")  val currentPrice: Double,
    @Json(name = "d")  val change: Double,
    @Json(name = "dp") val percentChange: Double,
    @Json(name = "h")  val highOfDay: Double,
    @Json(name = "l")  val lowOfDay: Double,
    @Json(name = "o")  val openPrice: Double,
    @Json(name = "pc") val previousClose: Double,
    @Json(name = "t")  val timestamp: Long
)