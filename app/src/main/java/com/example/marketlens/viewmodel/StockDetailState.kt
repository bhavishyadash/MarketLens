package com.example.marketlens.viewmodel

/*
    Added: "name" field — StockQuote now carries the company name,
    so we surface it in the state so the UI can display it.
*/
data class StockDetailState(
    val symbol: String        = "",
    val name: String          = "",      // e.g. "Apple Inc."
    val price: Double         = 0.0,
    val percentChange: Double = 0.0,
    val isLoading: Boolean    = true,
    val errorMessage: String? = null
)
