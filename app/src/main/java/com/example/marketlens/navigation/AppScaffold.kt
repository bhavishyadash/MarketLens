package com.example.marketlens.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.marketlens.ui.components.LivePill
import com.example.marketlens.ui.theme.Amber
import com.example.marketlens.ui.theme.MonoFamily
import com.example.marketlens.ui.theme.TerminalBlack
import com.example.marketlens.ui.theme.TerminalBorder
import com.example.marketlens.ui.theme.TerminalSurface
import com.example.marketlens.ui.theme.TextSecondary
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
        containerColor = TerminalBlack,
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
                TerminalBottomBar(
                    items         = bottomNavItems,
                    currentRoute  = currentRoute,
                    onNavigate    = { route ->
                        navController.navigate(route) {
                            popUpTo(AppRoute.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            color = TerminalBlack,
            modifier = Modifier.padding(innerPadding)
        ) {
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
    Column {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor         = TerminalBlack,
                titleContentColor      = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            title = {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text       = "MKT.LENS",
                        fontFamily = MonoFamily,
                        fontWeight = FontWeight.Bold,
                        color      = Amber,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    LivePill()
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = userName.uppercase(),
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            actions = {
                IconButton(onClick = onAlerts) {
                    Icon(Icons.Filled.Notifications, "Alerts")
                }
                if (showSettings) {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, "Settings")
                    }
                }
                IconButton(onClick = onSignOut) {
                    Icon(Icons.Filled.Logout, "Sign out")
                }
            }
        )
        HorizontalDivider(color = TerminalBorder, thickness = 1.dp)
    }
}

@Composable
private fun TerminalBottomBar(
    items: List<AppRoute>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Column {
        HorizontalDivider(color = TerminalBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                TerminalNavItem(
                    label    = item.label,
                    selected = selected,
                    onClick  = { onNavigate(item.route) },
                    icon     = { color ->
                        Icon(
                            imageVector        = iconForRoute(item.route),
                            contentDescription = item.label,
                            tint               = color,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TerminalNavItem(
    label:    String,
    selected: Boolean,
    onClick:  () -> Unit,
    icon:     @Composable (Color) -> Unit
) {
    val tint = if (selected) Amber else TextSecondary
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon(tint)
        Text(
            text  = label.uppercase(),
            color = tint,
            style = MaterialTheme.typography.labelSmall
        )
        Box(
            Modifier
                .height(2.dp)
                .width(if (selected) 20.dp else 0.dp)
                .background(Amber)
        )
    }
}

private fun iconForRoute(route: String) = when (route) {
    AppRoute.Dashboard.route -> Icons.Filled.Home
    AppRoute.Markets.route   -> Icons.Filled.ShowChart
    AppRoute.News.route      -> Icons.Filled.Article
    AppRoute.Watchlist.route -> Icons.Filled.Star
    AppRoute.Portfolio.route -> Icons.Filled.PieChart
    else                     -> Icons.Filled.Home
}
