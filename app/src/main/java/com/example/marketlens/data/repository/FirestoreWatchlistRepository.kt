package com.example.marketlens.data.repository

import com.example.marketlens.data.network.ApiResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class FirestoreWatchlistRepository(
    private val db:   FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : WatchlistRepository {


    private fun watchlistCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("watchlist")
    }

    override suspend fun getWatchlistSymbols(): ApiResult<List<String>> {
        return try {
            val collection = watchlistCollection()
                ?: return ApiResult.Error("Not signed in")

            val snapshot = collection.get().await()
            val symbols  = snapshot.documents.mapNotNull { it.getString("symbol") }
            ApiResult.Success(symbols)
        } catch (e: Exception) {
            ApiResult.Error("Could not load watchlist: ${e.message}", e)
        }
    }

    override suspend fun addSymbol(symbol: String): ApiResult<Unit> {
        return try {
            val collection = watchlistCollection()
                ?: return ApiResult.Error("Not signed in")

            // Document ID = symbol so we can query/delete by symbol directly
            collection.document(symbol).set(
                mapOf(
                    "symbol"  to symbol,
                    "addedAt" to System.currentTimeMillis()
                )
            ).await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not add to watchlist: ${e.message}", e)
        }
    }

    override suspend fun removeSymbol(symbol: String): ApiResult<Unit> {
        return try {
            val collection = watchlistCollection()
                ?: return ApiResult.Error("Not signed in")

            collection.document(symbol).delete().await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not remove from watchlist: ${e.message}", e)
        }
    }

    override suspend fun isInWatchlist(symbol: String): Boolean {
        return try {
            val collection = watchlistCollection() ?: return false
            val doc = collection.document(symbol).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}