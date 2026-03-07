package com.example.marketlens.data.model

/*
    Bug fixed: the repository was returning StockRowUi (a ViewModel/UI model).
    That broke the layering rule:
        Data layer  → should only know about raw domain data
        ViewModel   → maps domain data into UI-ready models

    StockQuote is a pure domain model. No UI logic, no formatting.
    The ViewModel decides how to display it.
*/
data class StockQuote(
    val symbol: String,
    val name: String,
    val price: Double,
    val percentChange: Double
)
