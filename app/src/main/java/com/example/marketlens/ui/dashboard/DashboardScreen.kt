package com.example.marketlens.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.MarketIndex
import com.example.marketlens.data.model.MarketMover
import com.example.marketlens.data.model.WatchlistItem
import com.example.marketlens.ui.components.ErrorView
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.components.SectionLabel
import com.example.marketlens.ui.components.TerminalCard
import com.example.marketlens.ui.theme.MonoFamily
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.ui.theme.TerminalBorder
import com.example.marketlens.ui.theme.TerminalRaised
import com.example.marketlens.ui.theme.TextSecondary
import com.example.marketlens.viewmodel.DashboardState
import com.example.marketlens.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "dashboard_content"
    ) { s ->
        when {
            s.isLoading -> LoadingView()
            s.errorMessage != null -> ErrorView(message = s.errorMessage, onRetry = { viewModel.refresh() })
            else -> DashboardContent(s, onRefresh = { viewModel.refresh() })
        }
    }
}

@Composable
private fun DashboardContent(state: DashboardState, onRefresh: () -> Unit) {
    LazyColumn(
        modifier             = Modifier.fillMaxSize().padding(horizontal = 14.dp),
        verticalArrangement  = Arrangement.spacedBy(14.dp),
        contentPadding       = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    SectionLabel("Market // Overview")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "Dashboard",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, "Refresh", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                state.indices.forEach { IndexCard(it, Modifier.weight(1f)) }
            }
        }
        item { MarketSnapshotCard(state.topGainer, state.topLoser) }
        item { WatchlistPreviewCard(state.watchlistPreview) }
    }
}

@Composable
private fun IndexCard(index: MarketIndex, modifier: Modifier = Modifier) {
    val changeColor = if (index.isUp) PriceUp else PriceDown
    TerminalCard(modifier = modifier, accent = changeColor) {
        Column(Modifier.padding(10.dp), Arrangement.spacedBy(4.dp)) {
            Text(
                text  = index.name.uppercase(),
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text       = "%.2f".format(index.currentValue),
                style      = MaterialTheme.typography.titleMedium,
                fontFamily = MonoFamily
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (index.isUp) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = changeColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text       = "${"%+.2f".format(index.percentChange)}%",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = changeColor,
                    fontFamily = MonoFamily
                )
            }
        }
    }
}

@Composable
private fun MarketSnapshotCard(gainer: MarketMover?, loser: MarketMover?) {
    TerminalCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(12.dp)) {
            SectionLabel("Market // Snapshot")
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                MoverChip("Top Gainer", gainer?.symbol ?: "—", gainer?.percentChange, true, Modifier.weight(1f))
                Spacer(
                    Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(TerminalBorder)
                )
                MoverChip("Top Loser",  loser?.symbol  ?: "—", loser?.percentChange,  false, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MoverChip(label: String, symbol: String, change: Double?, isGainer: Boolean, modifier: Modifier = Modifier) {
    val color = if (isGainer) PriceUp else PriceDown
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Text(
            text       = symbol,
            fontFamily = MonoFamily,
            style      = MaterialTheme.typography.titleLarge,
            color      = color
        )
        change?.let {
            Text(
                text       = "${"%+.2f".format(it)}%",
                fontFamily = MonoFamily,
                style      = MaterialTheme.typography.bodyMedium,
                color      = color
            )
        }
    }
}

@Composable
private fun WatchlistPreviewCard(items: List<WatchlistItem>) {
    TerminalCard(accent = MaterialTheme.colorScheme.secondary) {
        Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(10.dp)) {
            SectionLabel("Watchlist // Preview", accent = MaterialTheme.colorScheme.secondary)
            if (items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalRaised)
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text  = "NO POSITIONS TRACKED",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "Add stocks from Markets to see them here.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                // Header row
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("SYMBOL",  color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Text("LAST",  color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Text("CHG%",  color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                }
                HorizontalDivider(color = TerminalBorder, thickness = 1.dp)
                items.forEach { stock ->
                    val changeColor = if (stock.percentChange >= 0) PriceUp else PriceDown
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stock.symbol,
                            fontFamily = MonoFamily,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Text(
                                text       = "%.2f".format(stock.price),
                                fontFamily = MonoFamily,
                                style      = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text       = "${"%+.2f".format(stock.percentChange)}%",
                                fontFamily = MonoFamily,
                                color      = changeColor,
                                style      = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
