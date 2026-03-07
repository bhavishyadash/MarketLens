package com.example.marketlens.data.model

data class NewsArticle(
    val id:          Long,
    val headline:    String,
    val source:      String,
    val summary:     String,
    val url:         String,
    val imageUrl:    String,
    val publishedAt: Long,
    val symbol:      String,
    val sector:      String?
)