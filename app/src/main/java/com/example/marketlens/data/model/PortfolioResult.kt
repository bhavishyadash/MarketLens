package com.example.marketlens.data.model

data class PortfolioResult(
    val currentValue:       Double,
    val totalGainLoss:      Double,
    val totalGainLossPct:   Double,
    val holdings:           List<HoldingSnapshot>,
    val simulation:         AnalyticsResult?,
    val historicalGainPct:  Double = 0.0,
    val scaledTargetPct:    Double = 0.0
)

data class HoldingSnapshot(
    val symbol:        String,
    val shares:        Double,
    val currentPrice:  Double,
    val purchasePrice: Double,
    val currentValue:  Double,
    val gainLoss:      Double,
    val gainLossPct:   Double
)