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
    @Json(name = "meta")       val meta: YahooChartMetaDto?,
    @Json(name = "timestamp")  val timestamps: List<Long>?,
    @Json(name = "indicators") val indicators: YahooIndicatorsDto
)

data class YahooChartMetaDto(
    @Json(name = "symbol")             val symbol: String,
    @Json(name = "chartPreviousClose") val chartPreviousClose: Double?,
    @Json(name = "regularMarketPrice") val regularMarketPrice: Double?,
    @Json(name = "shortName")          val shortName: String?,
    @Json(name = "longName")           val longName: String?
)

data class YahooIndicatorsDto(
    @Json(name = "quote") val quote: List<YahooQuoteDataDto>
)

data class YahooQuoteDataDto(
    @Json(name = "close") val close: List<Double?>
)

data class YahooQuoteResponseDto(
    @Json(name = "quoteResponse") val quoteResponse: YahooQuoteResponseWrapper
)

data class YahooQuoteResponseWrapper(
    @Json(name = "result") val result: List<YahooStockQuoteDto>?,
    @Json(name = "error")  val error: Any?
)

data class YahooStockQuoteDto(
    @Json(name = "symbol")                      val symbol: String,
    @Json(name = "shortName")                   val shortName: String?,
    @Json(name = "longName")                    val longName: String?,
    @Json(name = "regularMarketPrice")          val regularMarketPrice: Double?,
    @Json(name = "regularMarketChangePercent")  val regularMarketChangePercent: Double?,
    @Json(name = "marketCap")                   val marketCap: Long?,
    @Json(name = "fiftyTwoWeekHigh")            val fiftyTwoWeekHigh: Double?,
    @Json(name = "fiftyTwoWeekLow")             val fiftyTwoWeekLow: Double?,
    @Json(name = "trailingPE")                  val trailingPE: Double?,
    @Json(name = "beta")                        val beta: Double?,
    @Json(name = "fullExchangeName")            val fullExchangeName: String?,
    @Json(name = "industry")                    val industry: String?,
    @Json(name = "sector")                      val sector: String?
)

data class YahooSearchResponseDto(
    @Json(name = "quotes") val quotes: List<YahooSearchQuoteDto>?,
    @Json(name = "news")   val news: List<YahooNewsItemDto>?
)

data class YahooSearchQuoteDto(
    @Json(name = "symbol")    val symbol: String,
    @Json(name = "shortname") val shortname: String?,
    @Json(name = "longname")  val longname: String?,
    @Json(name = "exchDisp")  val exchange: String?,
    @Json(name = "typeDisp")  val type: String?
)

data class YahooNewsItemDto(
    @Json(name = "uuid")                val uuid: String,
    @Json(name = "title")               val title: String,
    @Json(name = "publisher")           val publisher: String?,
    @Json(name = "link")                val link: String?,
    @Json(name = "providerPublishTime") val publishedAt: Long?,
    @Json(name = "summary")             val summary: String?
)