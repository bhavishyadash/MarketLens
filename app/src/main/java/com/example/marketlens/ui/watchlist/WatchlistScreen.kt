package com.example.marketlens.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.WatchlistRowUi
import com.example.marketlens.viewmodel.WatchlistViewModel

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        state.items.isEmpty() -> EmptyWatchlist()
        else -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text("Watchlist", style = MaterialTheme.typography.titleLarge)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.items, key = { it.symbol }) { item ->
                    WatchlistCard(item)
                }
            }
        }
    }
}

@Composable
private fun WatchlistCard(item: WatchlistRowUi) {
    val changeColor = if (item.isUp) PriceUp else PriceDown

    Card(
        colors = CardDefaults.cardColors(
            // Bug fixed: was Color(0xFF1A1D23) hardcoded
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.symbol, style = MaterialTheme.typography.titleMedium)
                Text(
                    text  = "Saved",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.2f".format(item.price)}")
                Text("${"%.2f".format(item.percentChange)}%", color = changeColor)
            }
        }
    }
}

// Shown when the watchlist is empty (Phase 1: never reached; Phase 2: user has no saved stocks)
@Composable
private fun EmptyWatchlist() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Your watchlist is empty", style = MaterialTheme.typography.titleMedium)
            Text(
                text  = "Go to Markets and tap a stock to save it",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
