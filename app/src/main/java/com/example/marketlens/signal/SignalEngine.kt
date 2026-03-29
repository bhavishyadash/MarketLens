package com.example.marketlens.signal

import com.example.marketlens.data.model.NewsArticle
import com.example.marketlens.data.model.NewsSignal
import java.util.UUID

object SignalEngine {

    fun process(
        articles:         List<NewsArticle>,
        watchlistSymbols: List<String>,
        alreadySeenIds:   Set<Long>
    ): List<NewsSignal> {

        val signals = mutableListOf<NewsSignal>()

        for (article in articles) {
            if (article.id in alreadySeenIds) continue

            val detection = KeywordDetector.detect(article.headline, article.summary)
                ?: continue

            val sectorStocks    = KeywordDetector.getStocksForSector(detection.sector)
            val affectedSymbols = watchlistSymbols.filter { it in sectorStocks }

            signals.add(
                NewsSignal(
                    id = UUID.randomUUID().toString(),
                    headline = article.headline,
                    sector = detection.sector,
                    strength = detection.strength,
                    affectedSymbols = affectedSymbols,
                    reason = detection.reason,
                    articleUrl = article.url,
                    detectedAt = System.currentTimeMillis()
                )
            )
        }

        return signals.sortedByDescending { it.strength.ordinal }
    }
}