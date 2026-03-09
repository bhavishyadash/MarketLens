package com.example.marketlens.data.repository

import com.example.marketlens.data.model.SearchResult
import com.example.marketlens.data.model.StockCandle
import com.example.marketlens.data.model.StockProfile
import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.network.MarketApi
import com.example.marketlens.data.network.YahooFinanceApi

class RealMarketRepository(
    private val api:   MarketApi,
    private val yahoo: YahooFinanceApi
) : MarketRepository {

    // ── Quote (Finnhub) ───────────────────────────────────────────────────────
    override suspend fun getQuote(symbol: String): ApiResult<StockQuote> {
        return try {
            val dto = api.getQuote(symbol)
            ApiResult.Success(StockQuote(symbol, symbol, dto.currentPrice, dto.percentChange))
        } catch (e: Exception) {
            ApiResult.Error("Could not load quote for $symbol: ${e.message}", e)
        }
    }

    // ── Search (Finnhub) ──────────────────────────────────────────────────────
    override suspend fun searchSymbols(query: String): ApiResult<List<SearchResult>> {
        return try {
            val dto = api.searchSymbols(query)
            val results = dto.result
                .filter { it.type == "Common Stock" || it.type == "ETP" }
                .map { SearchResult(it.symbol, it.description, it.type) }
            ApiResult.Success(results)
        } catch (e: Exception) {
            ApiResult.Error("Search failed: ${e.message}", e)
        }
    }

    // ── Candles (Yahoo Finance) ───────────────────────────────────────────────
    /*
        Yahoo Finance replaces Finnhub for all chart/history data.
        No API key needed, no rate limits for normal usage.

        We map our Timeframe enum to Yahoo's interval + range params:
          resolution "D"  + daysBack 30  → interval=1d,  range=1mo
          resolution "D"  + daysBack 90  → interval=1d,  range=3mo
          resolution "W"  + daysBack 365 → interval=1d,  range=1y

        We ignore the raw `from`/`to` epoch params from the interface
        and use Yahoo's range strings instead — cleaner and more reliable.
    */
    override suspend fun getCandles(
        symbol: String, resolution: String, from: Long, to: Long
    ): ApiResult<StockCandle> {
        return try {
            // Map resolution + rough date range to Yahoo params
            val (interval, range) = when {
                resolution == "W"                          -> "1d"  to "1y"
                resolution == "D" && (to - from) > 60 * 86400L -> "1d" to "3mo"
                resolution == "D"                          -> "1d"  to "1mo"
                else                                       -> "1d"  to "1mo"
            }

            val response = yahoo.getChart(symbol, interval, range)
            val result   = response.chart.result?.firstOrNull()
                ?: return ApiResult.Error("No chart data available for $symbol")

            /*
                Yahoo sometimes returns null for individual close prices
                (e.g. on trading halts or data gaps).
                We filter those out with filterNotNull().
            */
            val closePrices = result.indicators.quote
                .firstOrNull()
                ?.close
                ?.filterNotNull()
                ?: return ApiResult.Error("No price data in chart response for $symbol")

            if (closePrices.isEmpty()) {
                return ApiResult.Error("No chart data for $symbol in this time range")
            }

            // Use sequential indices as timestamps — the chart only needs prices
            val timestamps = closePrices.indices.map { it.toLong() }
            ApiResult.Success(StockCandle(timestamps, closePrices, "ok"))

        } catch (e: Exception) {
            ApiResult.Error("Could not load chart for $symbol: ${e.message}", e)
        }
    }

    // ── Profile + Key Stats (Finnhub) ─────────────────────────────────────────
    override suspend fun getStockProfile(symbol: String): ApiResult<StockProfile> {
        return try {
            val profileDto = api.getStockProfile(symbol)
            val metricDto  = api.getStockMetric(symbol).metric
            ApiResult.Success(
                StockProfile(
                    symbol             = symbol,
                    name               = profileDto.name,
                    exchange           = profileDto.exchange,
                    industry           = profileDto.industry,
                    marketCapFormatted = formatMarketCap(profileDto.marketCapMillions),
                    week52High         = metricDto.week52High,
                    week52Low          = metricDto.week52Low,
                    peRatio            = metricDto.peRatio,
                    beta               = metricDto.beta
                )
            )
        } catch (e: Exception) {
            ApiResult.Error("Could not load profile for $symbol: ${e.message}", e)
        }
    }

    private fun formatMarketCap(millions: Double): String = when {
        millions >= 1_000_000 -> "$%.2fT".format(millions / 1_000_000)
        millions >= 1_000     -> "$%.1fB".format(millions / 1_000)
        else                  -> "$%.1fM".format(millions)
    }
}