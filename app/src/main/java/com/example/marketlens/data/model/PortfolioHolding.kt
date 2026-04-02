package com.example.marketlens.data.model

data class PortfolioHolding(
    val symbol:        String,
    val shares:        Double,
    val purchasePrice: Double,
    val addedAt:       Long = System.currentTimeMillis()
) {
    fun currentValue(currentPrice: Double): Double = shares * currentPrice
    fun gainLoss(currentPrice: Double): Double = (currentPrice - purchasePrice) * shares
    fun gainLossPct(currentPrice: Double): Double =
        if (purchasePrice > 0) ((currentPrice - purchasePrice) / purchasePrice) * 100.0 else 0.0
}
