package com.example.marketlens.ui.stockdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.marketlens.data.model.AnalyticsResult
import com.example.marketlens.data.model.AlertDirection
import com.example.marketlens.data.model.StockProfile
import com.example.marketlens.ui.components.StockChart
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
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                        }
                        Column {
                            Text(state.symbol, style = MaterialTheme.typography.titleLarge)
                            Text(state.name.ifBlank { state.symbol }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            state.profile?.industry?.let {
                                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.onToggleWatchlist() }) {
                        Icon(
                            imageVector = if (state.isInWatchlist) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (state.isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                            tint = if (state.isInWatchlist) PriceUp else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(6.dp)) {
                        Text("Price", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.2f".format(state.price)}", style = MaterialTheme.typography.headlineMedium)
                        Text("${"%.2f".format(state.percentChange)}% today", color = changeColor)
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
                        Text("Price Chart", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(Timeframe.ONE_MONTH, Timeframe.THREE_MONTHS, Timeframe.ONE_YEAR).forEach { tf ->
                                FilterChip(
                                    selected = state.selectedTimeframe == tf,
                                    onClick  = { viewModel.onTimeframeSelected(tf) },
                                    label    = { Text(tf.label) }
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        when {
                            state.isCandleLoading -> Box(Modifier.fillMaxWidth().height(180.dp), Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(32.dp))
                            }
                            state.candleError != null -> Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                                Text(state.candleError!!, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            candle != null && candle.hasData -> StockChart(prices = candle.closePrices, modifier = Modifier.fillMaxWidth())
                            else -> Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                                Text("No chart data for this timeframe", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

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

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Set Price Alert", style = MaterialTheme.typography.titleMedium)
                            Text("Get notified when ${state.symbol} crosses your target.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        OutlinedTextField(
                            value           = state.alertPriceInput,
                            onValueChange   = { viewModel.onAlertPriceChanged(it) },
                            modifier        = Modifier.fillMaxWidth(),
                            label           = { Text("Alert Price (USD)") },
                            placeholder     = { Text("e.g. ${"%.2f".format(state.price)}") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine      = true,
                            prefix          = { Text("$") }
                        )
                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick  = { viewModel.onSetAlert(AlertDirection.ABOVE) },
                                modifier = Modifier.weight(1f),
                                enabled  = state.alertPriceInput.isNotBlank(),
                                colors   = ButtonDefaults.buttonColors(containerColor = PriceUp)
                            ) { Text("Alert Above") }
                            Button(
                                onClick  = { viewModel.onSetAlert(AlertDirection.BELOW) },
                                modifier = Modifier.weight(1f),
                                enabled  = state.alertPriceInput.isNotBlank(),
                                colors   = ButtonDefaults.buttonColors(containerColor = PriceDown)
                            ) { Text("Alert Below") }
                        }
                        if (state.alertSetSuccess) {
                            Text("✓ Alert set successfully", color = PriceUp, style = MaterialTheme.typography.bodySmall)
                        }
                        state.alertError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Target Return Simulator", style = MaterialTheme.typography.titleMedium)
                            Text("Based on historical patterns only — not a prediction or financial advice.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
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
                        Text("Time Horizon", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Horizon.entries.forEach { horizon ->
                                FilterChip(
                                    selected = state.selectedHorizon == horizon,
                                    onClick  = { viewModel.onHorizonSelected(horizon) },
                                    label    = { Text(horizon.label) }
                                )
                            }
                        }
                        Button(
                            onClick  = { viewModel.onCalculate() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled  = state.targetPriceInput.isNotBlank() && !state.isAnalyticsLoading
                        ) {
                            if (state.isAnalyticsLoading) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Calculate")
                            }
                        }
                        state.analyticsError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        state.analyticsResult?.let { AnalyticsResultCard(it) }
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
                        Text("Latest News — ${state.symbol}", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        when {
                            state.isNewsLoading -> Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp)) }
                            state.newsError != null -> Text(state.newsError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            state.news.isEmpty() -> Text("No recent news for ${state.symbol}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { state.news.take(5).forEach { NewsCard(it) } }
                        }
                    }
                }
            }
        }
    }
}

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
            Text("${"%.1f".format(result.gainNeededPct)}%", color = gainColor, style = MaterialTheme.typography.bodyMedium)
        }
        val probColor = when {
            result.probabilityPct >= 60 -> PriceUp
            result.probabilityPct >= 30 -> MaterialTheme.colorScheme.onSurface
            else                        -> PriceDown
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("Historical Probability", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${"%.0f".format(result.probabilityPct)}%", color = probColor, style = MaterialTheme.typography.bodyMedium)
        }
        result.medianDays?.let { weeks ->
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Median Weeks to Target", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$weeks weeks")
            }
        }
        result.maxDrawdownPct?.let { drawdown ->
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Max Drawdown Risk", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${"%.1f".format(drawdown)}%", color = PriceDown, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(
            text      = "Based on ${result.dataPointsUsed} trading days of history. Past patterns do not guarantee future results.",
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}
