package com.example.marketlens.data

import com.example.marketlens.data.model.StockQuote

object QuoteCache {

    private const val TTL_MS = 60_000L

    private data class CachedQuote(val quote: StockQuote, val fetchedAt: Long)

    private val cache = mutableMapOf<String, CachedQuote>()

    fun get(symbol: String): StockQuote? {
        val entry = cache[symbol] ?: return null
        val age = System.currentTimeMillis() - entry.fetchedAt
        if (age > TTL_MS) {
            cache.remove(symbol)
            return null
        }
        return entry.quote
    }

    fun put(quote: StockQuote) {
        cache[quote.symbol] = CachedQuote(quote, System.currentTimeMillis())
    }

    fun invalidate(symbol: String) {
        cache.remove(symbol)
    }

    fun invalidateAll() {
        cache.clear()
    }
}
