package com.example.marketlens.data.model

data class AnalyticsResult(
    val symbol:          String,
    val targetPrice:     Double,
    val currentPrice:    Double,
    val gainNeededPct:   Double,
    val probabilityPct:  Double,
    val medianDays:      Int?,
    val maxDrawdownPct:  Double?,
    val horizonDays:     Int,
    val dataPointsUsed:  Int
)