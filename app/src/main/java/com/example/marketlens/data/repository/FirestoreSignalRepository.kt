package com.example.marketlens.data.repository

import com.example.marketlens.data.model.NewsSignal
import com.example.marketlens.data.model.SignalStrength
import com.example.marketlens.data.network.ApiResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSignalRepository(
    private val db:   FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : SignalRepository {

    private fun signalsCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("signals")
    }

    private fun seenCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("seen_articles")
    }

    override suspend fun getSignals(): ApiResult<List<NewsSignal>> {
        return try {
            val collection = signalsCollection() ?: return ApiResult.Error("Not signed in")
            val snapshot = collection.get().await()
            val signals = snapshot.documents
                .mapNotNull { it.toNewsSignal() }
                .sortedByDescending { it.detectedAt }
                .take(50)
            ApiResult.Success(signals)
        } catch (e: Exception) {
            ApiResult.Error("Could not load signals: ${e.message}", e)
        }
    }

    override suspend fun saveSignals(signals: List<NewsSignal>): ApiResult<Unit> {
        return try {
            val collection = signalsCollection() ?: return ApiResult.Error("Not signed in")
            val batch = db.batch()
            signals.forEach { signal ->
                val docRef = collection.document(signal.id)
                batch.set(docRef, signal.toMap())
            }
            batch.commit().await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not save signals: ${e.message}", e)
        }
    }

    override suspend fun markRead(signalId: String): ApiResult<Unit> {
        return try {
            val collection = signalsCollection() ?: return ApiResult.Error("Not signed in")
            collection.document(signalId).update("isRead", true).await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not mark signal as read: ${e.message}", e)
        }
    }

    override suspend fun getSeenArticleIds(): Set<Long> {
        return try {
            val collection = seenCollection() ?: return emptySet()
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.getLong("articleId") }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    override suspend fun addSeenArticleId(articleId: Long) {
        try {
            val collection = seenCollection() ?: return
            collection.document(articleId.toString()).set(mapOf("articleId" to articleId)).await()
        } catch (e: Exception) {
        }
    }

    override suspend fun addSeenArticleIds(articleIds: List<Long>) {
        if (articleIds.isEmpty()) return
        try {
            val collection = seenCollection() ?: return
            val batch = db.batch()
            articleIds.forEach { id ->
                batch.set(collection.document(id.toString()), mapOf("articleId" to id))
            }
            batch.commit().await()
        } catch (e: Exception) {
        }
    }

    override suspend fun deleteSignal(signalId: String): ApiResult<Unit> {
        return try {
            val collection = signalsCollection() ?: return ApiResult.Error("Not signed in")
            collection.document(signalId).delete().await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not delete signal: ${e.message}", e)
        }
    }

    private fun NewsSignal.toMap(): Map<String, Any?> = mapOf(
        "id"              to id,
        "headline"        to headline,
        "sector"          to sector,
        "strength"        to strength.name,
        "affectedSymbols" to affectedSymbols,
        "reason"          to reason,
        "articleUrl"      to articleUrl,
        "detectedAt"      to detectedAt,
        "isRead"          to isRead
    )

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toNewsSignal(): NewsSignal? {
        return try {
            NewsSignal(
                id              = getString("id") ?: return null,
                headline        = getString("headline") ?: return null,
                sector          = getString("sector") ?: return null,
                strength        = SignalStrength.valueOf(getString("strength") ?: return null),
                affectedSymbols = get("affectedSymbols") as? List<String> ?: emptyList(),
                reason          = getString("reason") ?: "",
                articleUrl      = getString("articleUrl") ?: "",
                detectedAt      = getLong("detectedAt") ?: 0L,
                isRead          = getBoolean("isRead") ?: false
            )
        } catch (e: Exception) { null }
    }
}
