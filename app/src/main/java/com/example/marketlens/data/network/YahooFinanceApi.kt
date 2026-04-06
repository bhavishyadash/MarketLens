package com.example.marketlens.data.network

import com.example.marketlens.data.network.dto.YahooChartResponseDto
import com.example.marketlens.data.network.dto.YahooQuoteResponseDto
import com.example.marketlens.data.network.dto.YahooQuoteSummaryResponseDto
import com.example.marketlens.data.network.dto.YahooSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YahooFinanceApi {

    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol")    symbol: String,
        @Query("interval") interval: String,
        @Query("range")    range: String
    ): YahooChartResponseDto

    @GET("v7/finance/quote")
    suspend fun getQuotes(
        @Query("symbols") symbols: String,
        @Query("fields")  fields: String = "symbol,shortName,longName,regularMarketPrice,regularMarketChangePercent,marketCap,fiftyTwoWeekHigh,fiftyTwoWeekLow,trailingPE,beta,fullExchangeName,industry,sector"
    ): YahooQuoteResponseDto

    @GET("v10/finance/quoteSummary/{symbol}")
    suspend fun getQuoteSummary(
        @Path("symbol")  symbol: String,
        @Query("modules") modules: String = "summaryProfile,defaultKeyStatistics,financialData"
    ): YahooQuoteSummaryResponseDto


    @GET("v1/finance/search")
    suspend fun search(
        @Query("q")            query: String,
        @Query("quotesCount")  quotesCount: Int = 10,
        @Query("newsCount")    newsCount: Int = 0
    ): YahooSearchResponseDto
}