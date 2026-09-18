package com.example.marketlens.data.network

import com.example.marketlens.data.network.dto.YahooChartResponseDto
import com.example.marketlens.data.network.dto.YahooQuoteSummaryResponseDto
import com.example.marketlens.data.network.dto.YahooSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YahooFinanceApi {

    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("range") range: String,
        @Query("events") events: String = "div,splits",
        @Query("includePrePost") includePrePost: Boolean = false
    ): YahooChartResponseDto

    @GET("v1/finance/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("quotesCount") quotesCount: Int = 10,
        @Query("newsCount") newsCount: Int = 0,
        @Query("enableFuzzyQuery") enableFuzzyQuery: Boolean = false
    ): YahooSearchResponseDto

    @GET("v10/finance/quoteSummary/{symbol}")
    suspend fun getQuoteSummary(
        @Path("symbol") symbol: String,
        @Query("crumb") crumb: String,
        @Query("modules") modules: String = "price,assetProfile,summaryDetail,defaultKeyStatistics",
        @Query("formatted") formatted: Boolean = true
    ): YahooQuoteSummaryResponseDto
}
