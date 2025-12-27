package com.example.marketlens.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.marketlens.ui.dashboard.DashboardScreen
import com.example.marketlens.ui.markets.MarketsScreen
import com.example.marketlens.ui.news.NewsScreen
import com.example.marketlens.ui.watchlist.WatchlistScreen
import com.example.marketlens.ui.stockdetail.StockDetailScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Dashboard.route
    ) {
        composable(AppRoute.Dashboard.route) { DashboardScreen() }

        composable(AppRoute.Markets.route) {
            MarketsScreen(
                onStockClick = { stock ->
                    navController.navigate(
                        AppRoute.StockDetail.create(stock.symbol, stock.price, stock.percentChange)
                    )
                }
            )
        }

        composable(AppRoute.News.route) { NewsScreen() }
        composable(AppRoute.Watchlist.route) { WatchlistScreen() }

        composable(
            route = AppRoute.StockDetail.route,
            arguments = listOf(
                navArgument("symbol") { type = NavType.StringType },
                navArgument("price") { type = NavType.StringType },
                navArgument("change") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: "UNKNOWN"
            val price = backStackEntry.arguments?.getString("price")?.toDoubleOrNull() ?: 0.0
            val change = backStackEntry.arguments?.getString("change")?.toDoubleOrNull() ?: 0.0

            StockDetailScreen(
                symbol = symbol,
                price = price,
                percentChange = change,
                onBack = { navController.popBackStack() }
            )
        }
    }
}