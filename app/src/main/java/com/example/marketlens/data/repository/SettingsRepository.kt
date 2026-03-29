package com.example.marketlens.data.repository

import com.example.marketlens.data.network.ApiResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserSettings(
    val signalsEnabled:       Boolean = true,
    val highSignalsOnly:      Boolean = false,
    val notifyOnSignal:       Boolean = true,
    val watchlistSectorOnly:  Boolean = true
)

interface SettingsRepository {
    suspend fun getSettings(): ApiResult<UserSettings>
    suspend fun saveSettings(settings: UserSettings): ApiResult<Unit>
}

class FirestoreSettingsRepository(
    private val db:   FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : SettingsRepository {

    private fun settingsDoc() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("prefs").document("settings")
    }

    override suspend fun getSettings(): ApiResult<UserSettings> {
        return try {
            val doc = settingsDoc() ?: return ApiResult.Error("Not signed in")
            val snapshot = doc.get().await()
            if (!snapshot.exists()) return ApiResult.Success(UserSettings())
            ApiResult.Success(
                UserSettings(
                    signalsEnabled      = snapshot.getBoolean("signalsEnabled") ?: true,
                    highSignalsOnly     = snapshot.getBoolean("highSignalsOnly") ?: false,
                    notifyOnSignal      = snapshot.getBoolean("notifyOnSignal") ?: true,
                    watchlistSectorOnly = snapshot.getBoolean("watchlistSectorOnly") ?: true
                )
            )
        } catch (e: Exception) {
            ApiResult.Success(UserSettings())
        }
    }

    override suspend fun saveSettings(settings: UserSettings): ApiResult<Unit> {
        return try {
            val doc = settingsDoc() ?: return ApiResult.Error("Not signed in")
            doc.set(
                mapOf(
                    "signalsEnabled"      to settings.signalsEnabled,
                    "highSignalsOnly"     to settings.highSignalsOnly,
                    "notifyOnSignal"      to settings.notifyOnSignal,
                    "watchlistSectorOnly" to settings.watchlistSectorOnly
                )
            ).await()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Could not save settings: ${e.message}", e)
        }
    }
}
