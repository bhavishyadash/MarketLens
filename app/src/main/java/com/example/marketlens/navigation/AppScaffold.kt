package com.example.marketlens.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppScaffold(
    navController: NavHostController
) {
    val items = listOf(
        AppRoute.Dashboard,
        AppRoute.Markets,
        AppRoute.News,
        AppRoute.Watchlist
    )

    val backStackEntry = navController.currentBackStackEntryAsState().value
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
                                // Avoid building a huge back stack when switching tabs
                                popUpTo(AppRoute.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(item.label) },
                        icon = { /* add icons later */ }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Your screens render inside here
        Surface(modifier = Modifier.padding(innerPadding)) {
            AppNavGraph(navController)
        }
    }
}