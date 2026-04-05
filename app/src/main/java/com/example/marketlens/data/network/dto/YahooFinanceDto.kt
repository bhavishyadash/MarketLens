package com.example.marketlens.data.network.dto

import com.squareup.moshi.Json

// ── Existing chart DTOs (unchanged) ──────────────────────────────────────────

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
    @Json(name = "quote") val quote: List<YahooQuoteDataDto>
)

data class YahooQuoteDataDto(
    @Json(name = "close") val close: List<Double?>
)

// ── v7/finance/quote — real-time quote + profile + metrics in one call ────────
/*
    This single endpoint replaces:
      - Finnhub /quote       (price, % change)
      - Finnhub /stock/profile2  (name, exchange, industry)
      - Finnhub /stock/metric    (52W high/low, P/E, beta, market cap)

    Pass comma-separated symbols: symbols=AAPL,MSFT,NVDA
    Returns all in one response — massive reduction in API calls.
*/

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

// ── v1/finance/search — symbol search + news ─────────────────────────────────
/*
    One endpoint handles both:
      1. Symbol search: set quotesCount=10, newsCount=0
      2. News fetch:    set quotesCount=0,  newsCount=20

    For general market news, search for "stock market"
    For stock-specific news, search for the symbol
*/

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
