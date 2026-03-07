package com.example.marketlens.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey val id:          Long,
    val headline:    String,
    val source:      String,
    val summary:     String,
    val url:         String,
    val imageUrl:    String,
    val publishedAt: Long,
    val symbol:      String,
    val sector:      String?,
    val cachedAt:    Long
)