package com.example.marketlens.data.model

data class NewsSignal(
    val id:           String,
    val headline:     String,
    val sector:       String,
    val strength:     SignalStrength,
    val affectedSymbols: List<String>,
    val reason:       String,
    val articleUrl:   String,
    val detectedAt:   Long,
    val isRead:       Boolean = false
)

enum class SignalStrength { LOW, MEDIUM, HIGH }
