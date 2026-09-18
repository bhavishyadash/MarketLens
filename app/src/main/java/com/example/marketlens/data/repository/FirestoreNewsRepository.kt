package com.example.marketlens.data.repository

import com.example.marketlens.data.firebase.FirebaseModule
import com.example.marketlens.data.model.NewsArticle
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.network.YahooFinanceApi
import com.example.marketlens.data.network.dto.YahooNewsItemDto
import com.example.marketlens.util.SectorMapper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.tasks.await
import java.nio.charset.StandardCharsets
import java.util.UUID

class FirestoreNewsRepository(
    private val yahoo: YahooFinanceApi,
    private val db: FirebaseFirestore = FirebaseModule.firestore
) : NewsRepository {

    companion object {
        private const val CACHE_DURATION_MS = 60 * 60 * 1000L
        private const val NEWS_CACHE        = "news_cache"
        private const val ARTICLES          = "articles"
    }

    override suspend fun getMarketNews(): ApiResult<List<NewsArticle>> {
        return try {
            val collectionRef = db.collection(NEWS_CACHE)
                .document("general")
                .collection(ARTICLES)

            val cached = collectionRef
                .whereGreaterThan("cachedAt", System.currentTimeMillis() - CACHE_DURATION_MS)
                .get()
                .await()

            if (!cached.isEmpty) {
                val articles = cached.documents
                    .mapNotNull { it.toNewsArticle() }
                    .sortedByDescending { it.publishedAt }
                return ApiResult.Success(articles)
            }

            val dtos = yahoo.search(
                query       = "stock market today",
                quotesCount = 0,
                newsCount   = 25
            ).news.orEmpty()
            if (dtos.isEmpty()) return ApiResult.Error("No news articles available right now")

            val articles = dtos.map { it.toDomain(symbol = "") }
            saveToFirestore(collectionRef, articles)

            ApiResult.Success(articles.sortedByDescending { it.publishedAt })

        } catch (e: Exception) {
            ApiResult.Error("Could not load news: ${e.message}", e)
        }
    }

    override suspend fun getStockNews(symbol: String): ApiResult<List<NewsArticle>> {
        return try {
            val collectionRef = db.collection(NEWS_CACHE)
                .document("stock_$symbol")
                .collection(ARTICLES)

            val cached = collectionRef
                .whereGreaterThan("cachedAt", System.currentTimeMillis() - CACHE_DURATION_MS)
                .get()
                .await()

            if (!cached.isEmpty) {
                val articles = cached.documents
                    .mapNotNull { it.toNewsArticle() }
                    .sortedByDescending { it.publishedAt }
                return ApiResult.Success(articles)
            }

            val dtos = yahoo.search(
                query       = symbol,
                quotesCount = 0,
                newsCount   = 20
            ).news.orEmpty()
            if (dtos.isEmpty()) return ApiResult.Error("No recent news found for $symbol")

            val articles = dtos.map { it.toDomain(symbol = symbol) }
            saveToFirestore(collectionRef, articles)

            ApiResult.Success(articles.sortedByDescending { it.publishedAt })

        } catch (e: Exception) {
            ApiResult.Error("Could not load news for $symbol: ${e.message}", e)
        }
    }

    private suspend fun saveToFirestore(
        collectionRef: CollectionReference,
        articles: List<NewsArticle>
    ) {
        val batch = db.batch()
        articles.forEach { article ->
            val docRef = collectionRef.document(article.id.toString())
            batch.set(docRef, article.toFirestoreMap())
        }
        batch.commit().await()
    }

    private fun NewsArticle.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id"          to id,
        "headline"    to headline,
        "source"      to source,
        "summary"     to summary,
        "url"         to url,
        "imageUrl"    to imageUrl,
        "publishedAt" to publishedAt,
        "symbol"      to symbol,
        "sector"      to sector,
        "cachedAt"    to System.currentTimeMillis()
    )

    private fun DocumentSnapshot.toNewsArticle(): NewsArticle? {
        return try {
            NewsArticle(
                id          = getLong("id") ?: return null,
                headline    = getString("headline") ?: return null,
                source      = getString("source") ?: "",
                summary     = getString("summary") ?: "",
                url         = getString("url") ?: "",
                imageUrl    = getString("imageUrl") ?: "",
                publishedAt = getLong("publishedAt") ?: 0L,
                symbol      = getString("symbol") ?: "",
                sector      = getString("sector")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun YahooNewsItemDto.toDomain(symbol: String) = NewsArticle(
        id          = stableId(),
        headline    = title.orEmpty(),
        source      = publisher?.takeIf { it.isNotBlank() } ?: "Yahoo Finance",
        summary     = summary.orEmpty(),
        url         = link.orEmpty(),
        imageUrl    = "",
        publishedAt = publishedAt ?: 0L,
        symbol      = symbol,
        sector      = SectorMapper.map(title.orEmpty())
    )

    private fun YahooNewsItemDto.stableId(): Long {
        val seed = uuid.orEmpty().ifBlank {
            link.orEmpty().ifBlank { "${title.orEmpty()}|${publishedAt ?: 0L}" }
        }
        val stableUuid = runCatching { UUID.fromString(seed) }
            .getOrElse {
                UUID.nameUUIDFromBytes(seed.toByteArray(StandardCharsets.UTF_8))
            }
        return stableUuid.mostSignificantBits xor stableUuid.leastSignificantBits
    }
}
