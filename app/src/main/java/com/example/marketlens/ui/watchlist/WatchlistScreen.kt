package com.example.marketlens.ui.watchlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.ui.components.ErrorView
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.WatchlistRowUi
import com.example.marketlens.viewmodel.WatchlistViewModel

@Composable
fun WatchlistScreen(viewModel: WatchlistViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "watchlist_content"
    ) { s ->
        when {
            s.isLoading -> LoadingView()
            s.errorMessage != null -> ErrorView(message = s.errorMessage, onRetry = { viewModel.refresh() })
            s.items.isEmpty() -> EmptyWatchlist()
            else -> WatchlistContent(
                items    = s.items,
                onRemove = { viewModel.removeSymbol(it) },
                onRefresh = { viewModel.refresh() }
            )
        }
    }
}

@Composable
private fun WatchlistContent(items: List<WatchlistRowUi>, onRemove: (String) -> Unit, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Watchlist", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, "Refresh") }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items, key = { it.symbol }) { item ->
                WatchlistCard(item = item, onRemove = { onRemove(item.symbol) })
            }
        }
    }
}

@Composable
private fun WatchlistCard(item: WatchlistRowUi, onRemove: () -> Unit) {
    val changeColor = if (item.isUp) PriceUp else PriceDown
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.symbol, style = MaterialTheme.typography.titleMedium)
                Text("Watching", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("${"%.2f".format(item.price)}")
                    Text("${"%.2f".format(item.percentChange)}%", color = changeColor)
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, "Remove ${item.symbol}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyWatchlist() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Your watchlist is empty", style = MaterialTheme.typography.titleMedium)
            Text(
                "Open any stock and tap the bookmark icon to add it",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
