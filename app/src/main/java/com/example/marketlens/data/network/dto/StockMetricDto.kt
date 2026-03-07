package com.example.marketlens.data.network.dto

import com.squareup.moshi.Json

data class StockMetricResponseDto(
    @Json(name = "metric") val metric: StockMetricDto
)

data class StockMetricDto(
    @Json(name = "52WeekHigh")          val week52High: Double?,
    @Json(name = "52WeekLow")           val week52Low: Double?,
    @Json(name = "peBasicExclExtraTTM") val peRatio: Double?,
    @Json(name = "beta")                val beta: Double?,
    @Json(name = "marketCapitalization") val marketCap: Double?
)