package com.example.marketlens.ui.stockdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.StockDetailViewModel
import com.example.marketlens.viewmodel.Timeframe

@Composable
fun StockDetailScreen(viewModel: StockDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onBack) { Text("Go back") }
            }
        }
        else -> {
            val changeColor = if (state.percentChange >= 0.0) PriceUp else PriceDown
            val candle = state.candle  // local val fixes the smart cast error

            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(14.dp)) {

                // Top bar
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back") }
                    Column {
                        Text(state.symbol, style = MaterialTheme.typography.titleLarge)
                        if (state.name.isNotBlank() && state.name != state.symbol)
                            Text(state.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Price card
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(6.dp)) {
                        Text("Price", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.2f".format(state.price)}", style = MaterialTheme.typography.headlineMedium)
                        Text("${"%.2f".format(state.percentChange)}% today", color = changeColor, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Chart card
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
                        Text("Price Chart", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Timeframe.entries.forEach { tf ->
                                FilterChip(
                                    selected = state.selectedTimeframe == tf,
                                    onClick  = { viewModel.onTimeframeSelected(tf) },
                                    label    = { Text(tf.label) }
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        when {
                            state.isCandleLoading -> Box(Modifier.fillMaxWidth().height(160.dp), Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(32.dp))
                            }
                            state.candleError != null -> Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) {
                                Text(state.candleError!!, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            candle != null && candle.hasData -> Box(Modifier.fillMaxWidth().height(160.dp), Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${candle.closePrices.size} data points loaded ✓", style = MaterialTheme.typography.bodySmall, color = PriceUp)
                                    Text("Chart renders here in Phase 3", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            else -> Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) {
                                Text("No chart data for this timeframe", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Key stats
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(10.dp)) {
                        Text("Key Stats", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        StatRow("Market Cap", "—"); StatRow("P/E Ratio", "—")
                        StatRow("52W High", "—"); StatRow("52W Low", "—")
                    }
                }

                // Analytics placeholder
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(6.dp)) {
                        Text("Target Return Simulator", style = MaterialTheme.typography.titleMedium)
                        Text("Coming in Phase 4: probability & time-to-target estimates (informational only).",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}