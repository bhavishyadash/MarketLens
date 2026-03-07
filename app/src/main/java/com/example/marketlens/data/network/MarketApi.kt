package com.example.marketlens.data.network

import com.example.marketlens.data.network.dto.CandleDto
import com.example.marketlens.data.network.dto.QuoteDto
import com.example.marketlens.data.network.dto.SearchResponseDto
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
}