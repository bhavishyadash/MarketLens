package com.example.marketlens.data.network

import com.example.marketlens.data.network.dto.QuoteDto
import retrofit2.http.GET
import retrofit2.http.Query

/*
    Bug fixed: package was "com.example.marketlens.network"

    This is the Retrofit interface. Each function = one API endpoint.
    The @GET / @POST annotation is the URL path appended to the base URL.
    @Query turns a parameter into a URL query string: ?symbol=AAPL
    "suspend" means this runs on a coroutine — it won't block the UI thread.
*/
interface MarketApi {

    // Finnhub endpoint: GET /quote?symbol=AAPL&token=YOUR_KEY
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token")  apiKey: String
    ): QuoteDto
}
