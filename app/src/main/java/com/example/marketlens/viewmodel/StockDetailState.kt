package com.example.marketlens.viewmodel

import com.example.marketlens.data.model.StockCandle

enum class Timeframe(val label: String, val resolution: String, val daysBack: Int) {
    ONE_WEEK    ("1W", "60",  7),
    ONE_MONTH   ("1M", "D",  30),
    THREE_MONTHS("3M", "D",  90),
    ONE_YEAR    ("1Y", "W", 365)
}

data class StockDetailState(
    val symbol: String               = "",
    val name: String                 = "",
    val price: Double                = 0.0,
    val percentChange: Double        = 0.0,
    val candle: StockCandle?         = null,
    val selectedTimeframe: Timeframe = Timeframe.ONE_MONTH,
    val isCandleLoading: Boolean     = false,
    val candleError: String?         = null,
    val isLoading: Boolean           = true,
    val errorMessage: String?        = null
)