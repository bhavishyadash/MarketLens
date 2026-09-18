package com.example.marketlens.data.network.dto

import com.squareup.moshi.Json

data class YahooChartResponseDto(
    @Json(name = "chart") val chart: YahooChartDto? = null
)

data class YahooChartDto(
    @Json(name = "result") val result: List<YahooChartResultDto>? = null,
    @Json(name = "error") val error: YahooErrorDto? = null
)

data class YahooChartResultDto(
    @Json(name = "meta") val meta: YahooChartMetaDto? = null,
    @Json(name = "timestamp") val timestamps: List<Long>? = null,
    @Json(name = "indicators") val indicators: YahooIndicatorsDto? = null
)

data class YahooChartMetaDto(
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "exchangeName") val exchangeName: String? = null,
    @Json(name = "fullExchangeName") val fullExchangeName: String? = null,
    @Json(name = "instrumentType") val instrumentType: String? = null,
    @Json(name = "regularMarketTime") val regularMarketTime: Long? = null,
    @Json(name = "regularMarketPrice") val regularMarketPrice: Double? = null,
    @Json(name = "regularMarketChangePercent") val regularMarketChangePercent: Double? = null,
    @Json(name = "chartPreviousClose") val chartPreviousClose: Double? = null,
    @Json(name = "previousClose") val previousClose: Double? = null,
    @Json(name = "fiftyTwoWeekHigh") val fiftyTwoWeekHigh: Double? = null,
    @Json(name = "fiftyTwoWeekLow") val fiftyTwoWeekLow: Double? = null,
    @Json(name = "longName") val longName: String? = null,
    @Json(name = "shortName") val shortName: String? = null
)

data class YahooIndicatorsDto(
    @Json(name = "quote") val quote: List<YahooQuoteDataDto>? = null
)

data class YahooQuoteDataDto(
    @Json(name = "close") val close: List<Double?>? = null
)

data class YahooSearchResponseDto(
    @Json(name = "quotes") val quotes: List<YahooSearchQuoteDto>? = null,
    @Json(name = "news") val news: List<YahooNewsItemDto>? = null
)

data class YahooSearchQuoteDto(
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "shortname") val shortName: String? = null,
    @Json(name = "longname") val longName: String? = null,
    @Json(name = "quoteType") val quoteType: String? = null,
    @Json(name = "typeDisp") val typeDisplay: String? = null,
    @Json(name = "exchange") val exchange: String? = null,
    @Json(name = "exchDisp") val exchangeDisplay: String? = null,
    @Json(name = "industry") val industry: String? = null,
    @Json(name = "sector") val sector: String? = null,
    @Json(name = "isYahooFinance") val isYahooFinance: Boolean? = null
)

data class YahooNewsItemDto(
    @Json(name = "uuid") val uuid: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "publisher") val publisher: String? = null,
    @Json(name = "link") val link: String? = null,
    @Json(name = "providerPublishTime") val publishedAt: Long? = null,
    @Json(name = "summary") val summary: String? = null
)

data class YahooQuoteSummaryResponseDto(
    @Json(name = "quoteSummary") val quoteSummary: YahooQuoteSummaryDto? = null
)

data class YahooQuoteSummaryDto(
    @Json(name = "result") val result: List<YahooQuoteSummaryResultDto>? = null,
    @Json(name = "error") val error: YahooErrorDto? = null
)

data class YahooQuoteSummaryResultDto(
    @Json(name = "assetProfile") val assetProfile: YahooAssetProfileDto? = null,
    @Json(name = "summaryDetail") val summaryDetail: YahooSummaryDetailDto? = null,
    @Json(name = "defaultKeyStatistics") val defaultKeyStatistics: YahooDefaultKeyStatisticsDto? = null,
    @Json(name = "price") val price: YahooPriceDto? = null
)

data class YahooAssetProfileDto(
    @Json(name = "industry") val industry: String? = null,
    @Json(name = "sector") val sector: String? = null
)

data class YahooSummaryDetailDto(
    @Json(name = "marketCap") val marketCap: YahooValueDto? = null,
    @Json(name = "trailingPE") val trailingPe: YahooValueDto? = null,
    @Json(name = "fiftyTwoWeekHigh") val fiftyTwoWeekHigh: YahooValueDto? = null,
    @Json(name = "fiftyTwoWeekLow") val fiftyTwoWeekLow: YahooValueDto? = null,
    @Json(name = "beta") val beta: YahooValueDto? = null
)

data class YahooDefaultKeyStatisticsDto(
    @Json(name = "beta") val beta: YahooValueDto? = null
)

data class YahooPriceDto(
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "shortName") val shortName: String? = null,
    @Json(name = "longName") val longName: String? = null,
    @Json(name = "exchange") val exchange: String? = null,
    @Json(name = "exchangeName") val exchangeName: String? = null,
    @Json(name = "marketCap") val marketCap: YahooValueDto? = null
)

data class YahooValueDto(
    @Json(name = "raw") val raw: Double? = null,
    @Json(name = "fmt") val formatted: String? = null
)

data class YahooErrorDto(
    @Json(name = "code") val code: String? = null,
    @Json(name = "description") val description: String? = null
)
