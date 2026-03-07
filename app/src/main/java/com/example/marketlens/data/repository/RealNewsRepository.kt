package com.example.marketlens.data.repository

import com.example.marketlens.data.local.dao.NewsDao
import com.example.marketlens.data.local.entity.NewsArticleEntity
import com.example.marketlens.data.model.NewsArticle
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.network.MarketApi
import com.example.marketlens.data.network.dto.NewsArticleDto
import com.example.marketlens.util.SectorMapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RealNewsRepository(
    private val api: MarketApi,
    private val dao: NewsDao
) : NewsRepository {

    companion object {
        private const val CACHE_DURATION_MS = 60 * 60 * 1000L // 1 hour
    }

    override suspend fun getMarketNews(): ApiResult<List<NewsArticle>> {
        return try {
            val since  = System.currentTimeMillis() - CACHE_DURATION_MS
            val cached = dao.getGeneralNews(since)
            if (cached.isNotEmpty()) return ApiResult.Success(cached.map { it.toDomain() })

            val dtos = api.getMarketNews("general")
            if (dtos.isEmpty()) return ApiResult.Error("No news articles available right now")

            val entities = dtos.map { it.toEntity(symbol = "") }
            dao.insertAll(entities)
            dao.deleteOlderThan(System.currentTimeMillis() - CACHE_DURATION_MS * 24)
            ApiResult.Success(entities.map { it.toDomain() })

        } catch (e: Exception) {
            val staleCache = try { dao.getGeneralNews(0L) } catch (_: Exception) { emptyList() }
            if (staleCache.isNotEmpty()) ApiResult.Success(staleCache.map { it.toDomain() })
            else ApiResult.Error("Could not load news: ${e.message}", e)
        }
    }

    override suspend fun getStockNews(symbol: String): ApiResult<List<NewsArticle>> {
        return try {
            val since  = System.currentTimeMillis() - CACHE_DURATION_MS
            val cached = dao.getStockNews(symbol, since)
            if (cached.isNotEmpty()) return ApiResult.Success(cached.map { it.toDomain() })

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val toDate    = formatter.format(Date())
            val fromDate  = formatter.format(Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L))

            val dtos = api.getCompanyNews(symbol, fromDate, toDate)
            if (dtos.isEmpty()) return ApiResult.Error("No recent news found for $symbol")

            val entities = dtos.take(20).map { it.toEntity(symbol = symbol) }
            dao.insertAll(entities)
            ApiResult.Success(entities.map { it.toDomain() })

        } catch (e: Exception) {
            val staleCache = try { dao.getStockNews(symbol, 0L) } catch (_: Exception) { emptyList() }
            if (staleCache.isNotEmpty()) ApiResult.Success(staleCache.map { it.toDomain() })
            else ApiResult.Error("Could not load news for $symbol: ${e.message}", e)
        }
    }

    private fun NewsArticleDto.toEntity(symbol: String) = NewsArticleEntity(
        id          = id,
        headline    = headline,
        source      = source,
        summary     = summary,
        url         = url,
        imageUrl    = image,
        publishedAt = datetime,
        symbol      = symbol,
        sector      = SectorMapper.map(headline),
        cachedAt    = System.currentTimeMillis()
    )

    private fun NewsArticleEntity.toDomain() = NewsArticle(
        id          = id,
        headline    = headline,
        source      = source,
        summary     = summary,
        url         = url,
        imageUrl    = imageUrl,
        publishedAt = publishedAt,
        symbol      = symbol,
        sector      = sector
    )
}