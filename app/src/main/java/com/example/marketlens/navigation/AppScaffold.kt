package com.example.marketlens.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.marketlens.viewmodel.AuthViewModel

@Composable
fun AppScaffold(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val bottomNavItems = listOf(
        AppRoute.Dashboard,
        AppRoute.Markets,
        AppRoute.News,
        AppRoute.Watchlist
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    // Only show bottom nav on the 4 main screens
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        topBar = {
            // Show sign out button only on main screens
            if (showBottomBar) {
                SignOutBar(
                    userName  = authViewModel.currentUserName,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(AppRoute.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
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
                            icon  = {
                                Icon(
                                    imageVector     = iconForRoute(item.route),
                                    contentDescription = item.label
                                )
                            }
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
private fun SignOutBar(userName: String, onSignOut: () -> Unit) {
    TopAppBar(
        title = { Text("Hey, $userName", style = MaterialTheme.typography.bodyMedium) },
        actions = {
            IconButton(onClick = onSignOut) {
                Icon(Icons.Filled.Logout, contentDescription = "Sign out")
            }
        }
    )
}

private fun iconForRoute(route: String) = when (route) {
    AppRoute.Dashboard.route -> Icons.Filled.Home
    AppRoute.Markets.route   -> Icons.Filled.ShowChart
    AppRoute.News.route      -> Icons.Filled.Article
    AppRoute.Watchlist.route -> Icons.Filled.Star
    else                     -> Icons.Filled.Home
}