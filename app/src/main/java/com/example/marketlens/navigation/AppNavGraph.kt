package com.example.marketlens.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.marketlens.ui.dashboard.DashboardScreen
import com.example.marketlens.ui.markets.MarketsScreen
import com.example.marketlens.ui.news.NewsScreen
import com.example.marketlens.ui.watchlist.WatchlistScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Dashboard.route
    ) {
        composable(AppRoute.Dashboard.route) { DashboardScreen() }
        composable(AppRoute.Markets.route) { MarketsScreen() }
        composable(AppRoute.News.route) { NewsScreen() }
        composable(AppRoute.Watchlist.route) { WatchlistScreen() }
    }
}