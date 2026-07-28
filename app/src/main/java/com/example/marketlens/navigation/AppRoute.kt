package com.example.marketlens.navigation

sealed class AppRoute(val route: String, val label: String) {
    data object Auth      : AppRoute("auth",      "")
    data object Dashboard : AppRoute("dashboard", "Dashboard")
    data object Markets   : AppRoute("markets",   "Markets")
    data object News      : AppRoute("news",       "News")
    data object Watchlist : AppRoute("watchlist", "Watchlist")
    data object Portfolio : AppRoute("portfolio", "Portfolio")
    data object Alerts    : AppRoute("alerts",    "Alerts")
    data object Settings  : AppRoute("settings",  "Settings")

    data object StockDetail : AppRoute("stock/{symbol}", "") {
        fun create(symbol: String) = "stock/$symbol"
    }
}
