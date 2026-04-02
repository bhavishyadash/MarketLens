package com.example.marketlens.analytics

object PortfolioBlender {

    /*
        Takes a map of symbol → price series and a map of symbol → shares.
        Returns a single blended portfolio value series.

        Each point in the result represents the total portfolio value
        at that point in time:
          week[i] = Σ (shares[symbol] × prices[symbol][i])

        We trim all series to the shortest one first so every week
        has a value for every holding.
    */
    fun blend(
        priceSeries: Map<String, List<Double>>,
        shares:      Map<String, Double>
    ): List<Double> {
        if (priceSeries.isEmpty()) return emptyList()

        // Only include symbols that have both price data and a share count
        val validSymbols = priceSeries.keys.filter {
            shares.containsKey(it) && (priceSeries[it]?.isNotEmpty() == true)
        }
        if (validSymbols.isEmpty()) return emptyList()

        // Trim all series to the shortest length so indices align
        val minLength = validSymbols.minOf { priceSeries[it]!!.size }
        if (minLength == 0) return emptyList()

        // For each week, sum (shares × price) across all holdings
        return (0 until minLength).map { weekIndex ->
            validSymbols.sumOf { symbol ->
                val price      = priceSeries[symbol]!![weekIndex]
                val shareCount = shares[symbol] ?: 0.0
                price * shareCount
            }
        }
    }
}
