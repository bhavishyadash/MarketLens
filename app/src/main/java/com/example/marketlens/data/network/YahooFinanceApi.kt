package com.example.marketlens.data.network

import com.example.marketlens.data.network.dto.YahooChartResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface YahooFinanceApi {

    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol")   symbol: String,
        @Query("interval") interval: String,
        @Query("range")    range: String
    ): YahooChartResponseDto
}