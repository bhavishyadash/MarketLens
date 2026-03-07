package com.example.marketlens.util

object SectorMapper {

    private val SECTOR_KEYWORDS: Map<String, List<String>> = linkedMapOf(
        "Technology"    to listOf("ai", "tech", "chip", "semiconductor", "nvidia", "apple",
            "microsoft", "software", "cloud", "cyber", "machine learning"),
        "Energy"        to listOf("oil", "gas", "energy", "crude", "opec", "pipeline",
            "renewable", "solar", "exxon", "chevron"),
        "Financials"    to listOf("fed", "federal reserve", "interest rate", "bank", "inflation",
            "yield", "bond", "treasury", "jpmorgan", "goldman", "rate hike"),
        "Healthcare"    to listOf("pharma", "drug", "fda", "biotech", "vaccine",
            "hospital", "health", "pfizer"),
        "ConsumerGoods" to listOf("retail", "consumer", "walmart", "amazon", "spending",
            "e-commerce", "target", "costco"),
        "Automotive"    to listOf("tesla", "electric vehicle", "ev", "ford", "gm",
            "auto", "car", "self-driving"),
        "Real Estate"   to listOf("housing", "mortgage", "real estate", "reit",
            "home sales", "construction"),
        "Geopolitical"  to listOf("war", "sanction", "tariff", "trade", "china",
            "russia", "geopolit", "embargo")
    )

    fun map(headline: String): String? {
        val lower = headline.lowercase()
        for ((sector, keywords) in SECTOR_KEYWORDS) {
            if (keywords.any { lower.contains(it) }) return sector
        }
        return null
    }
}