package com.example.marketlens.data.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/*
    Bug fixed: package was "com.example.marketlens.network.dto"
    Should be "com.example.marketlens.data.network.dto" to match folder.

    DTO = Data Transfer Object.
    This is what Retrofit/Moshi parse directly from the JSON response.
    It stays close to the raw API shape — the repository maps it to
    our domain model (StockQuote) before passing it upward.
*/
@JsonClass(generateAdapter = true)  // tells Moshi to auto-generate a JSON adapter
data class QuoteDto(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "price")  val price: Double,
    @Json(name = "dp")     val percentChange: Double  // "dp" = daily percent change (Finnhub field name)
)
