package com.example.marketlens.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.AlertItem
import com.example.marketlens.data.model.MarketIndex
import com.example.marketlens.data.model.MarketMover
import com.example.marketlens.data.model.WatchlistItem
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.DashboardState
import com.example.marketlens.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.refresh() }) { Text("Retry") }
            }
        }
        else -> DashboardContent(state, onRefresh = { viewModel.refresh() })
    }
}

@Composable
private fun DashboardContent(state: DashboardState, onRefresh: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Market Overview", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, "Refresh") }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                state.indices.forEach { IndexCard(it, Modifier.weight(1f)) }
            }
        }
        item { MarketSnapshotCard(state.topGainer, state.topLoser) }
        item { WatchlistPreviewCard(state.watchlistPreview) }
        if (state.recentAlerts.isNotEmpty()) {
            item { Text("Recent Alerts", style = MaterialTheme.typography.titleLarge) }
            items(state.recentAlerts) { AlertCard(it) }
        }
    }
}

@Composable
private fun IndexCard(index: MarketIndex, modifier: Modifier = Modifier) {
    val changeColor = if (index.isUp) PriceUp else PriceDown
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(10.dp), Arrangement.spacedBy(4.dp)) {
            Text(index.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("%.2f".format(index.currentValue), style = MaterialTheme.typography.titleMedium)
            Text("${"%.2f".format(index.percentChange)}%", style = MaterialTheme.typography.bodySmall, color = changeColor)
        }
    }
}

@Composable
private fun MarketSnapshotCard(gainer: MarketMover?, loser: MarketMover?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(10.dp)) {
            Text("Market Snapshot", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                MoverChip("Top Gainer", gainer?.symbol ?: "—", gainer?.percentChange, true)
                MoverChip("Top Loser",  loser?.symbol  ?: "—", loser?.percentChange,  false)
            }
        }
    }
}

@Composable
private fun MoverChip(label: String, symbol: String, change: Double?, isGainer: Boolean) {
    val color = if (isGainer) PriceUp else PriceDown
    val icon  = if (isGainer) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Text(symbol, style = MaterialTheme.typography.titleSmall, color = color)
        }
        change?.let { Text("${"%.2f".format(it)}%", style = MaterialTheme.typography.bodySmall, color = color) }
    }
}

@Composable
private fun WatchlistPreviewCard(items: List<WatchlistItem>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(8.dp)) {
            Text("Watchlist Preview", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            items.forEach { stock ->
                val changeColor = if (stock.percentChange >= 0) PriceUp else PriceDown
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(stock.symbol, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${"%.2f".format(stock.price)}")
                        Text("${"%.2f".format(stock.percentChange)}%", color = changeColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: AlertItem) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(4.dp)) {
            Text(alert.message, style = MaterialTheme.typography.bodyMedium)
            Text(alert.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}