package com.example.marketlens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.marketlens.data.local.entity.NewsArticleEntity

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<NewsArticleEntity>)

    @Query("SELECT * FROM news_articles WHERE symbol = '' AND cachedAt > :since ORDER BY publishedAt DESC")
    suspend fun getGeneralNews(since: Long): List<NewsArticleEntity>

    @Query("SELECT * FROM news_articles WHERE symbol = :symbol AND cachedAt > :since ORDER BY publishedAt DESC")
    suspend fun getStockNews(symbol: String, since: Long): List<NewsArticleEntity>

    @Query("DELETE FROM news_articles WHERE cachedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}