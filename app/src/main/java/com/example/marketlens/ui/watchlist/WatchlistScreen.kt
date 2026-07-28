package com.example.marketlens.ui.watchlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.ui.components.ErrorView
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.components.SectionLabel
import com.example.marketlens.ui.theme.Amber
import com.example.marketlens.ui.theme.MonoFamily
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.ui.theme.TerminalBorder
import com.example.marketlens.ui.theme.TerminalRaised
import com.example.marketlens.ui.theme.TerminalSurface
import com.example.marketlens.ui.theme.TextSecondary
import com.example.marketlens.viewmodel.WatchlistRowUi
import com.example.marketlens.viewmodel.WatchlistViewModel

@Composable
fun WatchlistScreen(viewModel: WatchlistViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.onScreenVisible() }

    AnimatedContent(
        targetState  = state,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label        = "watchlist_content"
    ) { s ->
        when {
            s.isLoading -> LoadingView(label = "Loading watchlist")
            s.errorMessage != null -> ErrorView(message = s.errorMessage, onRetry = null)
            s.items.isEmpty() -> EmptyWatchlist()
            else -> WatchlistContent(items = s.items, onRemove = { viewModel.removeSymbol(it) })
        }
    }
}

@Composable
private fun WatchlistContent(items: List<WatchlistRowUi>, onRemove: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        SectionLabel("Watchlist // Tracked")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Watchlist", style = MaterialTheme.typography.displaySmall)
            Text(
                text       = "${items.size}",
                fontFamily = MonoFamily,
                color      = Amber,
                style      = MaterialTheme.typography.titleMedium
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items, key = { it.symbol }) { item ->
                WatchlistRow(item = item, onRemove = { onRemove(item.symbol) })
            }
        }
    }
}

@Composable
private fun WatchlistRow(item: WatchlistRowUi, onRemove: () -> Unit) {
    val changeColor = if (item.isUp) PriceUp else PriceDown
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text       = item.symbol,
                fontFamily = MonoFamily,
                style      = MaterialTheme.typography.titleMedium
            )
            Text(
                text  = "TRACKED",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "%.2f".format(item.price),
                    fontFamily = MonoFamily,
                    style      = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text       = "${"%+.2f".format(item.percentChange)}%",
                    fontFamily = MonoFamily,
                    color      = changeColor,
                    style      = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, "Remove ${item.symbol}", tint = TextSecondary)
            }
        }
    }
}

@Composable
private fun EmptyWatchlist() {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .background(TerminalRaised, RoundedCornerShape(3.dp))
                .border(1.dp, TerminalBorder, RoundedCornerShape(3.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.Star, null, tint = Amber, modifier = Modifier.size(28.dp))
            Text("NO POSITIONS TRACKED", color = Amber, style = MaterialTheme.typography.labelLarge)
            Text(
                text  = "Open any stock and tap the bookmark icon to add it here.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
