package com.example.marketlens.viewmodel

import com.example.marketlens.data.model.AnalyticsResult
import com.example.marketlens.data.model.NewsArticle
import com.example.marketlens.data.model.StockCandle
import com.example.marketlens.data.model.StockProfile

enum class Timeframe(val label: String, val resolution: String, val daysBack: Int) {
    ONE_WEEK    ("1W", "60",  7),
    ONE_MONTH   ("1M", "D",  30),
    THREE_MONTHS("3M", "D",  90),
    ONE_YEAR    ("1Y", "W", 365)
}

enum class Horizon(val label: String, val days: Int) {
    ONE_MONTH    ("1 Month",  30),
    THREE_MONTHS ("3 Months", 90),
    SIX_MONTHS   ("6 Months", 180),
    ONE_YEAR     ("1 Year",   365)
}

data class StockDetailState(
    val symbol:             String           = "",
    val name:               String           = "",
    val price:              Double           = 0.0,
    val percentChange:      Double           = 0.0,
    val candle:             StockCandle?     = null,
    val selectedTimeframe:  Timeframe        = Timeframe.ONE_MONTH,
    val isCandleLoading:    Boolean          = false,
    val candleError:        String?          = null,
    val profile:            StockProfile?    = null,
    val isProfileLoading:   Boolean          = false,
    val profileError:       String?          = null,
    val targetPriceInput:   String           = "",
    val selectedHorizon:    Horizon          = Horizon.THREE_MONTHS,
    val analyticsResult:    AnalyticsResult? = null,
    val isAnalyticsLoading: Boolean          = false,
    val analyticsError:     String?          = null,
    val news:               List<NewsArticle> = emptyList(),
    val isNewsLoading:      Boolean          = false,
    val newsError:          String?          = null,
    val isLoading:          Boolean          = true,
    val errorMessage:       String?          = null
)