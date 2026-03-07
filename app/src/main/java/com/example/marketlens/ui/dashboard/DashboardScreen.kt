package com.example.marketlens.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.example.marketlens.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingBox()
        state.errorMessage != null -> ErrorBox(state.errorMessage!!)
        else -> DashboardContent(state = state)
    }
}

@Composable
private fun DashboardContent(state: com.example.marketlens.viewmodel.DashboardState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { SectionTitle("Market Overview") }

        // ── Index cards row ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.indices.forEach { index ->
                    IndexCard(index = index, modifier = Modifier.weight(1f))
                }
            }
        }

        // ── Top mover snapshot ────────────────────────────────────────────
        item { MarketSnapshotCard(state.topGainer, state.topLoser) }

        // ── Watchlist preview ─────────────────────────────────────────────
        item { WatchlistPreviewCard(state.watchlistPreview) }

        // ── Recent alerts ─────────────────────────────────────────────────
        if (state.recentAlerts.isNotEmpty()) {
            item { SectionTitle("Recent Alerts") }
            items(state.recentAlerts) { alert ->
                AlertCard(alert)
            }
        }
    }
}

// ── Index Card ────────────────────────────────────────────────────────────────
@Composable
private fun IndexCard(index: MarketIndex, modifier: Modifier = Modifier) {
    val changeColor = if (index.isUp) PriceUp else PriceDown

    Card(
        modifier = modifier,
        // Bug fixed: was Color(0xFF1A1D23) hardcoded.
        // MaterialTheme.colorScheme.surface is defined in Theme.kt
        // and will automatically be correct in both dark and light mode.
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = index.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "%.2f".format(index.currentValue),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${"%.2f".format(index.percentChange)}%",
                style = MaterialTheme.typography.bodySmall,
                color = changeColor
            )
        }
    }
}

// ── Market Snapshot Card ──────────────────────────────────────────────────────
@Composable
private fun MarketSnapshotCard(gainer: MarketMover?, loser: MarketMover?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Market Snapshot", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MoverChip(
                    label    = "Top Gainer",
                    symbol   = gainer?.symbol ?: "—",
                    change   = gainer?.percentChange,
                    isGainer = true
                )
                MoverChip(
                    label    = "Top Loser",
                    symbol   = loser?.symbol ?: "—",
                    change   = loser?.percentChange,
                    isGainer = false
                )
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
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(symbol, style = MaterialTheme.typography.titleSmall, color = color)
        }
        change?.let {
            Text("${"%.2f".format(it)}%", style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

// ── Watchlist Preview ─────────────────────────────────────────────────────────
@Composable
private fun WatchlistPreviewCard(items: List<WatchlistItem>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Watchlist Preview", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            items.forEach { stock ->
                val changeColor = if (stock.percentChange >= 0) PriceUp else PriceDown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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

// ── Alert Card ────────────────────────────────────────────────────────────────
@Composable
private fun AlertCard(alert: AlertItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(alert.message, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = alert.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────
@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBox(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}
