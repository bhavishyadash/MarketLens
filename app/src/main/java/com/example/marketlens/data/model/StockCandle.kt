package com.example.marketlens.data.model

data class StockCandle(
    val timestamps: List<Long>,
    val closePrices: List<Double>,
    val status: String
) {
    val hasData: Boolean get() = status == "ok" && timestamps.isNotEmpty()
}