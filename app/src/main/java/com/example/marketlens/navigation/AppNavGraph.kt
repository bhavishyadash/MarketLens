package com.example.marketlens.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.marketlens.ui.dashboard.DashboardScreen
import com.example.marketlens.ui.markets.MarketsScreen
import com.example.marketlens.ui.news.NewsScreen
import com.example.marketlens.ui.stockdetail.StockDetailScreen
import com.example.marketlens.ui.watchlist.WatchlistScreen
import com.example.marketlens.viewmodel.StockDetailViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Dashboard.route
    ) {
        composable(AppRoute.Dashboard.route) {
            DashboardScreen()
        }

        composable(AppRoute.Markets.route) {
            MarketsScreen(
                onStockClick = { stock ->
                    navController.navigate(AppRoute.StockDetail.create(stock.symbol))
                }
            )
        }

        composable(AppRoute.News.route) {
            NewsScreen()
        }

        composable(AppRoute.Watchlist.route) {
            WatchlistScreen()
        }

        composable(
            route = AppRoute.StockDetail.route,
            arguments = listOf(navArgument("symbol") { type = NavType.StringType })
        ) { backStackEntry ->
            val vm: StockDetailViewModel = viewModel(backStackEntry)
            StockDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}