package com.example.marketlens.analytics

import com.example.marketlens.data.model.AnalyticsResult


object AnalyticsEngine {

    fun compute(
        prices:       List<Double>,
        currentPrice: Double,
        targetPrice:  Double,
        horizonDays:  Int,
        symbol:       String
    ): AnalyticsResult? {

        if (prices.size < horizonDays + 10) return null
        if (targetPrice <= 0 || currentPrice <= 0) return null

        val gainNeededPct          = ((targetPrice - currentPrice) / currentPrice) * 100.0
        val scaledTargetMultiplier = targetPrice / currentPrice

        val daysToTarget = mutableListOf<Int>()
        val drawdowns    = mutableListOf<Double>()

        val windowCount = prices.size - horizonDays
        for (startIdx in 0 until windowCount) {
            val startPrice   = prices[startIdx]
            val scaledTarget = startPrice * scaledTargetMultiplier
            val window       = prices.subList(startIdx, startIdx + horizonDays)

            var targetReachedDay: Int? = null
            var lowestPrice = startPrice

            for ((dayIndex, price) in window.withIndex()) {
                if (price < lowestPrice) lowestPrice = price
                if (targetReachedDay == null && price >= scaledTarget) {
                    targetReachedDay = dayIndex + 1
                }
            }

            if (targetReachedDay != null) {
                daysToTarget.add(targetReachedDay)
            } else {
                drawdowns.add(((lowestPrice - startPrice) / startPrice) * 100.0)
            }
        }

        val probabilityPct = if (windowCount > 0)
            (daysToTarget.size.toDouble() / windowCount.toDouble()) * 100.0
        else 0.0

        val medianDays = if (daysToTarget.isNotEmpty())
            daysToTarget.sorted()[daysToTarget.size / 2]
        else null

        val maxDrawdownPct = if (drawdowns.isNotEmpty()) drawdowns.min() else null

        return AnalyticsResult(
            symbol         = symbol,
            targetPrice    = targetPrice,
            currentPrice   = currentPrice,
            gainNeededPct  = gainNeededPct,
            probabilityPct = probabilityPct,
            medianDays     = medianDays,
            maxDrawdownPct = maxDrawdownPct,
            horizonDays    = horizonDays,
            dataPointsUsed = prices.size
        )
    }
}