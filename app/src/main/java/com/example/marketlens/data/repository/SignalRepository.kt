package com.example.marketlens.data.repository

import com.example.marketlens.data.model.NewsSignal
import com.example.marketlens.data.network.ApiResult

interface SignalRepository {
    suspend fun getSignals(): ApiResult<List<NewsSignal>>
    suspend fun saveSignals(signals: List<NewsSignal>): ApiResult<Unit>
    suspend fun markRead(signalId: String): ApiResult<Unit>
    suspend fun getSeenArticleIds(): Set<Long>
    suspend fun addSeenArticleId(articleId: Long)
    suspend fun addSeenArticleIds(articleIds: List<Long>)
    suspend fun deleteSignal(signalId: String): ApiResult<Unit>
}
