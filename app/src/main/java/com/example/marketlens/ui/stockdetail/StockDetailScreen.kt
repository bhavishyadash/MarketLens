package com.example.marketlens.ui.stockdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.marketlens.data.model.AnalyticsResult
import com.example.marketlens.data.model.StockProfile
import com.example.marketlens.ui.news.NewsCard
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.Horizon
import com.example.marketlens.viewmodel.StockDetailViewModel
import com.example.marketlens.viewmodel.Timeframe

@Composable
fun StockDetailScreen(viewModel: StockDetailViewModel, onBack: () -> Unit) {
    val state  by viewModel.state.collectAsState()
    val candle = state.candle

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onBack) { Text("Go back") }
            }
        }
        else -> {
            val changeColor = if (state.percentChange >= 0.0) PriceUp else PriceDown

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Top bar ───────────────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                    }
                    Column {
                        Text(state.symbol, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text  = state.name.ifBlank { state.symbol },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        state.profile?.industry?.let { industry ->
                            Text(
                                text  = industry,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── Price card ────────────────────────────────────────────────
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(6.dp)) {
                        Text("Price", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.2f".format(state.price)}", style = MaterialTheme.typography.headlineMedium)
                        Text("${"%.2f".format(state.percentChange)}% today", color = changeColor)
                    }
                }

                // ── Chart card ────────────────────────────────────────────────
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
                                    Text("Chart library integration — Phase 5", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            else -> Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) {
                                Text("No chart data for this timeframe", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // ── Key Stats ─────────────────────────────────────────────────
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(10.dp)) {
                        Text("Key Stats", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        when {
                            state.isProfileLoading -> Box(Modifier.fillMaxWidth().height(60.dp), Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(24.dp))
                            }
                            state.profile != null -> KeyStatsContent(state.profile!!)
                            else -> {
                                StatRow("Market Cap", "—")
                                StatRow("P/E Ratio",  "—")
                                StatRow("52W High",   "—")
                                StatRow("52W Low",    "—")
                            }
                        }
                    }
                }

                // ── Target Return Simulator ───────────────────────────────────
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(14.dp)) {

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Target Return Simulator", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text  = "Based on historical patterns only — not a prediction or financial advice.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Target price input
                        OutlinedTextField(
                            value           = state.targetPriceInput,
                            onValueChange   = { viewModel.onTargetPriceChanged(it) },
                            modifier        = Modifier.fillMaxWidth(),
                            label           = { Text("Target Price (USD)") },
                            placeholder     = { Text("e.g. ${"%.2f".format(state.price * 1.2)}") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine      = true,
                            prefix          = { Text("$") }
                        )

                        // Horizon selector
                        Text(
                            text  = "Time Horizon",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier            = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Horizon.entries.forEach { horizon ->
                                FilterChip(
                                    selected = state.selectedHorizon == horizon,
                                    onClick  = { viewModel.onHorizonSelected(horizon) },
                                    label    = { Text(horizon.label) }
                                )
                            }
                        }

                        // Calculate button
                        Button(
                            onClick  = { viewModel.onCalculate() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled  = state.targetPriceInput.isNotBlank() && !state.isAnalyticsLoading
                        ) {
                            if (state.isAnalyticsLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color       = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Calculate")
                            }
                        }

                        // Error message
                        state.analyticsError?.let { error ->
                            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        // Results
                        state.analyticsResult?.let { result ->
                            AnalyticsResultCard(result)
                        }
                    }
                }

                // ── Stock News ────────────────────────────────────────────────
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
                        Text("Latest News — ${state.symbol}", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        when {
                            state.isNewsLoading -> Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(28.dp))
                            }
                            state.newsError != null -> Text(
                                text  = state.newsError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            state.news.isEmpty() -> Text(
                                text  = "No recent news for ${state.symbol}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                state.news.take(5).forEach { NewsCard(it) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun KeyStatsContent(profile: StockProfile) {
    StatRow("Market Cap", profile.marketCapFormatted)
    StatRow("P/E Ratio",  profile.peRatio?.let { "%.1f".format(it) } ?: "N/A")
    StatRow("52W High",   profile.week52High?.let { "$%.2f".format(it) } ?: "N/A")
    StatRow("52W Low",    profile.week52Low?.let { "$%.2f".format(it) } ?: "N/A")
    StatRow("Beta",       profile.beta?.let { "%.2f".format(it) } ?: "N/A")
    StatRow("Exchange",   profile.exchange)
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AnalyticsResultCard(result: AnalyticsResult) {
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        val gainColor = if (result.gainNeededPct >= 0) PriceUp else PriceDown
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("Gain Needed", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${"%.1f".format(result.gainNeededPct)}%", color = gainColor)
        }

        val probColor = when {
            result.probabilityPct >= 60 -> PriceUp
            result.probabilityPct >= 30 -> MaterialTheme.colorScheme.onSurface
            else                        -> PriceDown
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("Historical Probability", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${"%.0f".format(result.probabilityPct)}%", color = probColor)
        }

        result.medianDays?.let { days ->
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Median Days to Target", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$days days")
            }
        }

        result.maxDrawdownPct?.let { drawdown ->
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Max Drawdown Risk", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${"%.1f".format(drawdown)}%", color = PriceDown)
            }
        }

        Text(
            text      = "Based on ${result.dataPointsUsed} trading days of history. " +
                    "Past patterns do not guarantee future results. " +
                    "This is educational only.",
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}