package com.example.marketlens.data.repository

import com.example.marketlens.data.model.PortfolioHolding
import com.example.marketlens.data.network.ApiResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestorePortfolioRepository(
    private val db:   FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : PortfolioRepository {

    private fun portfolioCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("portfolio")
    }

    override suspend fun getHoldings(): ApiResult<List<PortfolioHolding>> {
        return try {
            val collection = portfolioCollection() ?: return ApiResult.Error("Not signed in")
            val snapshot = collection.get().await()
            val holdings = snapshot.documents.mapNotNull { it.toHolding() }
            ApiResult.Success(holdings)
        } catch (e: Exception) {
            ApiResult.Error("Could not load portfolio: ${e.message}", e)
        }
    }

    override suspend fun saveHolding(holding: PortfolioHolding): ApiResult<Unit> {
        return try {
            val collection = portfolioCollection() ?: return ApiResult.Error("Not signed in")
            collection.document(holding.symbol).set(
                mapOf(
                    "symbol"        to holding.symbol,
                    "shares"        to holding.shares,
                    "purchasePrice" to holding.purchasePrice,
                    "addedAt"       to holding.addedAt
                )
            ).await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not save holding: ${e.message}", e)
        }
    }

    override suspend fun deleteHolding(symbol: String): ApiResult<Unit> {
        return try {
            val collection = portfolioCollection() ?: return ApiResult.Error("Not signed in")
            collection.document(symbol).delete().await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not delete holding: ${e.message}", e)
        }
    }

    private fun DocumentSnapshot.toHolding(): PortfolioHolding? {
        return try {
            PortfolioHolding(
                symbol        = getString("symbol") ?: return null,
                shares        = getDouble("shares") ?: return null,
                purchasePrice = getDouble("purchasePrice") ?: return null,
                addedAt       = getLong("addedAt") ?: 0L
            )
        } catch (e: Exception) { null }
    }
}
