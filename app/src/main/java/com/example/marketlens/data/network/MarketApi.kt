package com.example.marketlens.data.network

import com.example.marketlens.data.network.dto.CandleDto
import com.example.marketlens.data.network.dto.NewsArticleDto
import com.example.marketlens.data.network.dto.QuoteDto
import com.example.marketlens.data.network.dto.SearchResponseDto
import com.example.marketlens.data.network.dto.StockMetricResponseDto
import com.example.marketlens.data.network.dto.StockProfileDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MarketApi {

    @GET("quote")
    suspend fun getQuote(@Query("symbol") symbol: String): QuoteDto

    @GET("search")
    suspend fun searchSymbols(@Query("q") query: String): SearchResponseDto

    @GET("stock/candle")
    suspend fun getCandles(
        @Query("symbol")     symbol: String,
        @Query("resolution") resolution: String,
        @Query("from")       from: Long,
        @Query("to")         to: Long
    ): CandleDto

    @GET("news")
    suspend fun getMarketNews(@Query("category") category: String = "general"): List<NewsArticleDto>

    @GET("company-news")
    suspend fun getCompanyNews(
        @Query("symbol") symbol: String,
        @Query("from")   from: String,
        @Query("to")     to: String
    ): List<NewsArticleDto>

    @GET("stock/profile2")
    suspend fun getStockProfile(@Query("symbol") symbol: String): StockProfileDto

    @GET("stock/metric")
    suspend fun getStockMetric(
        @Query("symbol") symbol: String,
        @Query("metric") metric: String = "all"
    ): StockMetricResponseDto
}