package com.example.marketlens.navigation

sealed class AppRoute(val route: String, val label: String) {
    data object Dashboard : AppRoute("dashboard", "Dashboard")
    data object Markets : AppRoute("markets", "Markets")
    data object News : AppRoute("news", "News")
    data object Watchlist : AppRoute("watchlist", "Watchlist")
}