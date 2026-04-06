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
    @Json(name = "meta")       val meta: YahooChartMetaDto,
    @Json(name = "timestamp")  val timestamps: List<Long>?,
    @Json(name = "indicators") val indicators: YahooIndicatorsDto
)

data class YahooChartMetaDto(
    @Json(name = "symbol")           val symbol: String?,
    @Json(name = "shortName")        val shortName: String?,
    @Json(name = "longName")         val longName: String?,
    @Json(name = "exchangeName")     val exchangeName: String?,
    @Json(name = "currency")         val currency: String?,
    @Json(name = "fiftyTwoWeekHigh") val fiftyTwoWeekHigh: Double?,
    @Json(name = "fiftyTwoWeekLow")  val fiftyTwoWeekLow: Double?,
    @Json(name = "regularMarketPrice") val regularMarketPrice: Double?,
    @Json(name = "chartPreviousClose") val previousClose: Double?
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

data class YahooQuoteSummaryResponseDto(
    @Json(name = "quoteSummary") val quoteSummary: YahooQuoteSummaryWrapper
)

data class YahooQuoteSummaryWrapper(
    @Json(name = "result") val result: List<YahooQuoteSummaryResultDto>?,
    @Json(name = "error")  val error: Any?
)

data class YahooQuoteSummaryResultDto(
    @Json(name = "summaryDetail")        val summaryDetail: YahooSummaryDetailDto?,
    @Json(name = "defaultKeyStatistics") val defaultKeyStatistics: YahooDefaultKeyStatisticsDto?,
    @Json(name = "assetProfile")         val assetProfile: YahooAssetProfileDto?,
    @Json(name = "price")                val price: YahooPriceDto?
)

data class YahooSummaryDetailDto(
    @Json(name = "marketCap")        val marketCap: YahooValueDto?,
    @Json(name = "trailingPE")       val trailingPE: YahooValueDto?,
    @Json(name = "fiftyTwoWeekHigh") val fiftyTwoWeekHigh: YahooValueDto?,
    @Json(name = "fiftyTwoWeekLow")  val fiftyTwoWeekLow: YahooValueDto?
)

data class YahooDefaultKeyStatisticsDto(
    @Json(name = "beta") val beta: YahooValueDto?
)

data class YahooAssetProfileDto(
    @Json(name = "industry") val industry: String?,
    @Json(name = "sector")   val sector: String?
)

data class YahooPriceDto(
    @Json(name = "exchangeName") val exchangeName: String?
)

data class YahooValueDto(
    @Json(name = "raw") val raw: Double?,
    @Json(name = "fmt") val fmt: String?
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
data class YahooSummaryProfileDto(
    @Json(name = "industry") val industry: String?,
    @Json(name = "sector")   val sector: String?
)

data class YahooKeyStatsDto(
    @Json(name = "beta")           val beta: YahooRawValue?,
    @Json(name = "trailingEps")    val trailingEps: YahooRawValue?,
    @Json(name = "52WeekChange")   val weekChange52: YahooRawValue?
)

data class YahooFinancialDataDto(
    @Json(name = "currentPrice")   val currentPrice: YahooRawValue?,
    @Json(name = "targetMeanPrice") val targetMeanPrice: YahooRawValue?
)

data class YahooRawValue(
    @Json(name = "raw") val raw: Double?
)
