package com.example.marketlens.data.network.dto

import com.squareup.moshi.Json

data class CandleDto(
    @Json(name = "c") val closePrices: List<Double>?,
    @Json(name = "h") val highPrices: List<Double>?,
    @Json(name = "l") val lowPrices: List<Double>?,
    @Json(name = "o") val openPrices: List<Double>?,
    @Json(name = "t") val timestamps: List<Long>?,
    @Json(name = "v") val volumes: List<Long>?,
    @Json(name = "s") val status: String
)