package com.example.marketlens.data.network.dto

import com.squareup.moshi.Json

data class NewsArticleDto(
    @Json(name = "category") val category: String,
    @Json(name = "datetime") val datetime: Long,
    @Json(name = "headline") val headline: String,
    @Json(name = "id")       val id: Long,
    @Json(name = "image")    val image: String,
    @Json(name = "related")  val related: String,
    @Json(name = "source")   val source: String,
    @Json(name = "summary")  val summary: String,
    @Json(name = "url")      val url: String
)