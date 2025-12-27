package com.example.marketlens.network

import com.example.marketlens.network.dto.QuoteDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MarketApi {

    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String
    ): QuoteDto
}