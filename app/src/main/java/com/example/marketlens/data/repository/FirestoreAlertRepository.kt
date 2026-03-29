package com.example.marketlens.data.repository

import com.example.marketlens.data.model.AlertDirection
import com.example.marketlens.data.model.PriceAlert
import com.example.marketlens.data.network.ApiResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreAlertRepository(
    private val db:   FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AlertRepository {

    private fun alertsCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("alerts")
    }

    override suspend fun getAlerts(): ApiResult<List<PriceAlert>> {
        return try {
            val collection = alertsCollection() ?: return ApiResult.Error("Not signed in")
            val snapshot = collection.get().await()
            val alerts = snapshot.documents.mapNotNull { it.toPriceAlert() }.sortedByDescending { it.createdAt }
            ApiResult.Success(alerts)
        } catch (e: Exception) {
            ApiResult.Error("Could not load alerts: ${e.message}", e)
        }
    }

    override suspend fun getActiveAlerts(): ApiResult<List<PriceAlert>> {
        return try {
            val collection = alertsCollection() ?: return ApiResult.Error("Not signed in")
            val snapshot = collection.whereEqualTo("isTriggered", false).get().await()
            ApiResult.Success(snapshot.documents.mapNotNull { it.toPriceAlert() })
        } catch (e: Exception) {
            ApiResult.Error("Could not load active alerts: ${e.message}", e)
        }
    }

    override suspend fun addAlert(symbol: String, targetPrice: Double, direction: AlertDirection): ApiResult<Unit> {
        return try {
            val collection = alertsCollection() ?: return ApiResult.Error("Not signed in")
            val id = UUID.randomUUID().toString()
            collection.document(id).set(
                mapOf(
                    "id"          to id,
                    "symbol"      to symbol,
                    "targetPrice" to targetPrice,
                    "direction"   to direction.name,
                    "createdAt"   to System.currentTimeMillis(),
                    "isTriggered" to false
                )
            ).await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not add alert: ${e.message}", e)
        }
    }

    override suspend fun markTriggered(alertId: String): ApiResult<Unit> {
        return try {
            val collection = alertsCollection() ?: return ApiResult.Error("Not signed in")
            collection.document(alertId).update("isTriggered", true).await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not update alert: ${e.message}", e)
        }
    }

    override suspend fun deleteAlert(alertId: String): ApiResult<Unit> {
        return try {
            val collection = alertsCollection() ?: return ApiResult.Error("Not signed in")
            collection.document(alertId).delete().await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not delete alert: ${e.message}", e)
        }
    }

    private fun DocumentSnapshot.toPriceAlert(): PriceAlert? {
        return try {
            PriceAlert(
                id          = getString("id") ?: return null,
                symbol      = getString("symbol") ?: return null,
                targetPrice = getDouble("targetPrice") ?: return null,
                direction   = AlertDirection.valueOf(getString("direction") ?: return null),
                createdAt   = getLong("createdAt") ?: 0L,
                isTriggered = getBoolean("isTriggered") ?: false
            )
        } catch (e: Exception) { null }
    }
}
