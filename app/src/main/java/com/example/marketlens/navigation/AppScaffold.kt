package com.example.marketlens.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppScaffold(navController: NavHostController) {
    val items = listOf(
        AppRoute.Dashboard,
        AppRoute.Markets,
        AppRoute.News,
        AppRoute.Watchlist
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val selected = currentRoute == item.route

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(AppRoute.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(text = item.label) },
                        icon = {
                            Icon(
                                imageVector = iconForRoute(item.route),
                                contentDescription = item.label
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            AppNavGraph(navController)
        }
    }
}

private fun iconForRoute(route: String) = when (route) {
    AppRoute.Dashboard.route -> Icons.Filled.Home
    AppRoute.Markets.route -> Icons.Filled.ShowChart
    AppRoute.News.route -> Icons.Filled.Article
    AppRoute.Watchlist.route -> Icons.Filled.Star
    else -> Icons.Filled.Home
}