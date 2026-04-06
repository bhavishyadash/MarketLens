package com.example.marketlens.analytics

object PortfolioBlender {

    fun blend(
        priceSeries: Map<String, List<Double>>,
        shares:      Map<String, Double>
    ): List<Double> {
        if (priceSeries.isEmpty()) return emptyList()

        val validSymbols = priceSeries.keys.filter {
            shares.containsKey(it) && (priceSeries[it]?.isNotEmpty() == true)
        }
        if (validSymbols.isEmpty()) return emptyList()

        val minLength = validSymbols.minOf { priceSeries[it]!!.size }
        if (minLength == 0) return emptyList()

        return (0 until minLength).map { weekIndex ->
            validSymbols.sumOf { symbol ->
                val price      = priceSeries[symbol]!![weekIndex]
                val shareCount = shares[symbol] ?: 0.0
                price * shareCount
            }
        }
    }
}