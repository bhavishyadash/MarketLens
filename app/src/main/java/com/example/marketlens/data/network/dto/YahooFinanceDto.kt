package com.example.marketlens.data.network.dto

import com.squareup.moshi.Json


data class YahooChartResponseDto(
    @Json(name = "chart") val chart: YahooChartDto
)

data class YahooChartDto(
    @Json(name = "result") val result: List<YahooChartResultDto>?,
    @Json(name = "error")  val error: Any?
)

data class YahooChartResultDto(
    @Json(name = "timestamp")  val timestamps: List<Long>?,
    @Json(name = "indicators") val indicators: YahooIndicatorsDto
)

data class YahooIndicatorsDto(
    @Json(name = "quote") val quote: List<YahooQuoteDto>
)

data class YahooQuoteDto(
    @Json(name = "close") val close: List<Double?>
)