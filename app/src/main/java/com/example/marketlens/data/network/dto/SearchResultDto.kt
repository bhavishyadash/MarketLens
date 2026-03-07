package com.example.marketlens.data.network.dto

import com.squareup.moshi.Json

data class SearchResponseDto(
    @Json(name = "count")  val count: Int,
    @Json(name = "result") val result: List<SearchItemDto>
)

data class SearchItemDto(
    @Json(name = "description")   val description: String,
    @Json(name = "displaySymbol") val displaySymbol: String,
    @Json(name = "symbol")        val symbol: String,
    @Json(name = "type")          val type: String
)