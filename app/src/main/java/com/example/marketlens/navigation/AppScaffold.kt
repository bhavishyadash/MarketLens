package com.example.marketlens.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.marketlens.viewmodel.AuthViewModel

@Composable
fun AppScaffold(navController: NavHostController, authViewModel: AuthViewModel) {
    val bottomNavItems = listOf(
        AppRoute.Dashboard,
        AppRoute.Markets,
        AppRoute.News,
        AppRoute.Watchlist,
        AppRoute.Portfolio
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route
    val showBottomBar  = currentRoute in bottomNavItems.map { it.route }
    val showTopBar     = currentRoute in (bottomNavItems.map { it.route } + AppRoute.Settings.route)

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopBar(
                    userName     = authViewModel.currentUserName,
                    showSettings = currentRoute != AppRoute.Settings.route,
                    onAlerts     = { navController.navigate(AppRoute.Alerts.route) },
                    onSettings   = { navController.navigate(AppRoute.Settings.route) },
                    onSignOut    = {
                        authViewModel.signOut()
                        navController.navigate(AppRoute.Auth.route) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(AppRoute.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            label = { Text(item.label) },
                            icon  = { Icon(iconForRoute(item.route), contentDescription = item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            AppNavGraph(navController = navController, authViewModel = authViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    userName:     String,
    showSettings: Boolean,
    onAlerts:     () -> Unit,
    onSettings:   () -> Unit,
    onSignOut:    () -> Unit
) {
    TopAppBar(
        title   = { Text("Hey, $userName", style = MaterialTheme.typography.bodyMedium) },
        actions = {
            IconButton(onClick = onAlerts) { Icon(Icons.Filled.Notifications, "Alerts") }
            if (showSettings) {
                IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, "Settings") }
            }
            IconButton(onClick = onSignOut) { Icon(Icons.Filled.Logout, "Sign out") }
        }
    )
}

private fun iconForRoute(route: String) = when (route) {
    AppRoute.Dashboard.route -> Icons.Filled.Home
    AppRoute.Markets.route   -> Icons.Filled.ShowChart
    AppRoute.News.route      -> Icons.Filled.Article
    AppRoute.Watchlist.route -> Icons.Filled.Star
    AppRoute.Portfolio.route -> Icons.Filled.PieChart
    else                     -> Icons.Filled.Home
}
