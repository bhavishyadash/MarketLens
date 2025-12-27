package com.example.marketlens.navigation

sealed class AppRoute(val route: String, val label: String) {
    data object Dashboard : AppRoute("dashboard", "Dashboard")
    data object Markets : AppRoute("markets", "Markets")
    data object News : AppRoute("news", "News")
    data object Watchlist : AppRoute("watchlist", "Watchlist")

    data object StockDetail : AppRoute("stock/{symbol}/{price}/{change}", "") {
        fun create(symbol: String, price: Double, change: Double): String =
            "stock/$symbol/${price}/${change}"
    }
}