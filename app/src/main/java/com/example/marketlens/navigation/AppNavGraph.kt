package com.example.marketlens.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.marketlens.ui.alerts.AlertsScreen
import com.example.marketlens.ui.auth.AuthScreen
import com.example.marketlens.ui.dashboard.DashboardScreen
import com.example.marketlens.ui.markets.MarketsScreen
import com.example.marketlens.ui.news.NewsScreen
import com.example.marketlens.ui.settings.SettingsScreen
import com.example.marketlens.ui.signals.SignalsScreen
import com.example.marketlens.ui.stockdetail.StockDetailScreen
import com.example.marketlens.ui.watchlist.WatchlistScreen
import com.example.marketlens.viewmodel.AuthViewModel
import com.example.marketlens.viewmodel.StockDetailViewModel

private const val NAV_DURATION = 300

@Composable
fun AppNavGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    val startDestination = if (authViewModel.isAlreadySignedIn)
        AppRoute.Dashboard.route else AppRoute.Auth.route

    NavHost(
        navController       = navController,
        startDestination    = startDestination,
        enterTransition     = { fadeIn(tween(NAV_DURATION)) },
        exitTransition      = { fadeOut(tween(NAV_DURATION)) },
        popEnterTransition  = { fadeIn(tween(NAV_DURATION)) },
        popExitTransition   = { fadeOut(tween(NAV_DURATION)) }
    ) {

        composable(AppRoute.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(AppRoute.Auth.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(AppRoute.Dashboard.route) { DashboardScreen() }
        composable(AppRoute.Markets.route) {
            MarketsScreen(onStockClick = { stock ->
                navController.navigate(AppRoute.StockDetail.create(stock.symbol))
            })
        }
        composable(AppRoute.News.route)      { NewsScreen() }
        composable(AppRoute.Watchlist.route) { WatchlistScreen() }
        composable(AppRoute.Alerts.route)    { AlertsScreen() }
        composable(AppRoute.Signals.route)   { SignalsScreen() }
        composable(AppRoute.Settings.route)  { SettingsScreen() }

        composable(
            route     = AppRoute.StockDetail.route,
            arguments = listOf(navArgument("symbol") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_DURATION))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_DURATION))
            }
        ) {
            val vm: StockDetailViewModel = viewModel(factory = StockDetailViewModel.Factory)
            StockDetailScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
