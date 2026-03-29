package com.example.marketlens.signal

import com.example.marketlens.data.model.SignalStrength

data class DetectionResult(
    val sector:   String,
    val strength: SignalStrength,
    val reason:   String
)

object KeywordDetector {

    private data class Rule(
        val keywords: List<String>,
        val strength: SignalStrength,
        val reason:   String
    )

    private val SECTOR_RULES: Map<String, List<Rule>> = mapOf(

        "Technology" to listOf(
            Rule(listOf("ai regulation", "chip ban", "semiconductor export", "tech antitrust", "data privacy law"), SignalStrength.HIGH,
                "Regulatory action on tech sector detected"),
            Rule(listOf("nvidia earnings", "apple earnings", "microsoft earnings", "google earnings", "meta earnings"), SignalStrength.HIGH,
                "Major tech earnings event detected"),
            Rule(listOf("ai", "artificial intelligence", "machine learning", "chatgpt", "openai", "generative"), SignalStrength.MEDIUM,
                "AI-related news may impact tech sector"),
            Rule(listOf("chip", "semiconductor", "nvidia", "amd", "intel", "tsmc"), SignalStrength.MEDIUM,
                "Semiconductor news detected"),
            Rule(listOf("cloud", "aws", "azure", "google cloud", "software", "cybersecurity", "data breach"), SignalStrength.LOW,
                "General tech sector news detected")
        ),

        "Energy" to listOf(
            Rule(listOf("opec cut", "oil embargo", "energy sanction", "pipeline attack", "refinery explosion"), SignalStrength.HIGH,
                "Major energy supply disruption detected"),
            Rule(listOf("crude oil", "oil price", "natural gas price", "energy crisis"), SignalStrength.MEDIUM,
                "Energy price movement news detected"),
            Rule(listOf("solar", "renewable", "clean energy", "wind power", "ev charging", "battery storage"), SignalStrength.MEDIUM,
                "Renewable energy news detected"),
            Rule(listOf("exxon", "chevron", "shell", "bp", "opec", "gas", "oil"), SignalStrength.LOW,
                "General energy sector news detected")
        ),

        "Financials" to listOf(
            Rule(listOf("fed rate hike", "interest rate decision", "federal reserve meeting", "rate cut", "fomc"), SignalStrength.HIGH,
                "Federal Reserve policy decision detected"),
            Rule(listOf("bank failure", "banking crisis", "credit crunch", "financial contagion"), SignalStrength.HIGH,
                "Banking system stress event detected"),
            Rule(listOf("inflation report", "cpi data", "pce data", "jobs report", "unemployment"), SignalStrength.MEDIUM,
                "Key economic data release detected"),
            Rule(listOf("yield curve", "treasury", "bond", "interest rate", "inflation", "fed", "federal reserve"), SignalStrength.LOW,
                "General financial sector news detected")
        ),

        "Healthcare" to listOf(
            Rule(listOf("fda approval", "fda rejection", "drug recall", "clinical trial results", "vaccine approval"), SignalStrength.HIGH,
                "FDA regulatory decision detected"),
            Rule(listOf("pandemic", "outbreak", "public health emergency", "who alert"), SignalStrength.HIGH,
                "Public health emergency news detected"),
            Rule(listOf("biotech", "pharma earnings", "drug pricing", "pfizer", "moderna", "johnson"), SignalStrength.MEDIUM,
                "Pharmaceutical sector news detected"),
            Rule(listOf("health", "hospital", "medical", "drug", "vaccine", "clinical"), SignalStrength.LOW,
                "General healthcare sector news detected")
        ),

        "Automotive" to listOf(
            Rule(listOf("ev recall", "self-driving accident", "auto tariff", "car ban"), SignalStrength.HIGH,
                "Major automotive regulatory or safety event detected"),
            Rule(listOf("tesla earnings", "ford earnings", "gm earnings", "rivian", "lucid"), SignalStrength.MEDIUM,
                "Auto manufacturer earnings or event detected"),
            Rule(listOf("electric vehicle", "ev sales", "charging infrastructure", "autonomous driving"), SignalStrength.MEDIUM,
                "EV industry news detected"),
            Rule(listOf("tesla", "ford", "gm", "auto", "car", "vehicle"), SignalStrength.LOW,
                "General automotive sector news detected")
        ),

        "Geopolitical" to listOf(
            Rule(listOf("war escalation", "military strike", "nuclear threat", "trade war", "sanctions imposed"), SignalStrength.HIGH,
                "High-impact geopolitical event detected"),
            Rule(listOf("tariff", "trade deal", "sanction", "embargo", "geopolit", "china tension", "taiwan"), SignalStrength.MEDIUM,
                "Trade or geopolitical tension detected"),
            Rule(listOf("war", "conflict", "russia", "china", "election", "coup", "protest"), SignalStrength.LOW,
                "General geopolitical news detected")
        ),

        "ConsumerGoods" to listOf(
            Rule(listOf("retail sales data", "consumer spending report", "holiday sales", "inflation impact consumer"), SignalStrength.MEDIUM,
                "Consumer spending data detected"),
            Rule(listOf("amazon", "walmart", "target", "costco", "retail", "consumer", "e-commerce"), SignalStrength.LOW,
                "General consumer sector news detected")
        ),

        "RealEstate" to listOf(
            Rule(listOf("housing crash", "mortgage crisis", "real estate bubble", "home prices fall"), SignalStrength.HIGH,
                "Housing market stress detected"),
            Rule(listOf("housing market", "mortgage rate", "home sales", "reit", "construction"), SignalStrength.LOW,
                "Real estate sector news detected")
        )
    )

    private val SECTOR_STOCKS: Map<String, List<String>> = mapOf(
        "Technology"    to listOf("AAPL", "MSFT", "NVDA", "GOOGL", "META", "AMD", "TSLA"),
        "Energy"        to listOf("XOM", "CVX", "COP", "SLB"),
        "Financials"    to listOf("JPM", "BAC", "GS", "MS", "V", "MA"),
        "Healthcare"    to listOf("JNJ", "PFE", "MRK", "ABBV", "UNH"),
        "Automotive"    to listOf("TSLA", "F", "GM", "RIVN"),
        "Geopolitical"  to listOf("XOM", "LMT", "RTX", "GD"),
        "ConsumerGoods" to listOf("AMZN", "WMT", "TGT", "COST"),
        "RealEstate"    to listOf("AMT", "PLD", "SPG")
    )

    fun detect(headline: String, summary: String): DetectionResult? {
        val text = (headline + " " + summary).lowercase()

        for ((sector, rules) in SECTOR_RULES) {
            for (rule in rules) {
                if (rule.keywords.any { text.contains(it) }) {
                    return DetectionResult(
                        sector   = sector,
                        strength = rule.strength,
                        reason   = rule.reason
                    )
                }
            }
        }
        return null
    }

    fun getStocksForSector(sector: String): List<String> {
        return SECTOR_STOCKS[sector] ?: emptyList()
    }
}
