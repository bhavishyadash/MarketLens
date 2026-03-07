package com.example.marketlens.data.network.dto

import com.squareup.moshi.Json

data class StockProfileDto(
    @Json(name = "name")                 val name: String,
    @Json(name = "ticker")               val ticker: String,
    @Json(name = "exchange")             val exchange: String,
    @Json(name = "finnhubIndustry")      val industry: String,
    @Json(name = "marketCapitalization") val marketCapMillions: Double,
    @Json(name = "logo")                 val logoUrl: String,
    @Json(name = "weburl")               val website: String,
    @Json(name = "currency")             val currency: String,
    @Json(name = "country")              val country: String
)