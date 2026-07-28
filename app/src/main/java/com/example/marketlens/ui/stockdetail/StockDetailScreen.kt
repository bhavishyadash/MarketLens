package com.example.marketlens.ui.stockdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
import com.example.marketlens.ui.components.LivePill
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.components.SectionLabel
import com.example.marketlens.ui.components.StockChart
import com.example.marketlens.ui.components.TerminalCard
import com.example.marketlens.ui.news.NewsCard
import com.example.marketlens.ui.theme.Amber
import com.example.marketlens.ui.theme.MonoFamily
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceStyleLarge
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.ui.theme.TerminalBlack
import com.example.marketlens.ui.theme.TerminalBorder
import com.example.marketlens.ui.theme.TextSecondary
import com.example.marketlens.viewmodel.Horizon
import com.example.marketlens.viewmodel.StockDetailViewModel
import com.example.marketlens.viewmodel.Timeframe

@Composable
fun StockDetailScreen(viewModel: StockDetailViewModel, onBack: () -> Unit) {
    val state  by viewModel.state.collectAsState()
    val candle = state.candle

    when {
        state.isLoading -> LoadingView(label = "Loading ${state.symbol}")
        state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onBack) { Text("Go back", color = Amber) }
            }
        }
        else -> {
            val changeColor = if (state.percentChange >= 0.0) PriceUp else PriceDown

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text       = state.symbol,
                                    fontFamily = MonoFamily,
                                    style      = MaterialTheme.typography.headlineMedium
                                )
                                LivePill()
                            }
                            Text(
                                text  = state.name.ifBlank { state.symbol },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            state.profile?.industry?.let {
                                Text(
                                    text       = it.uppercase(),
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = TextSecondary,
                                    fontFamily = MonoFamily
                                )
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.onToggleWatchlist() }) {
                        Icon(
                            imageVector = if (state.isInWatchlist) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (state.isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                            tint = if (state.isInWatchlist) Amber else TextSecondary
                        )
                    }
                }

                // Hero price panel — big mono price + delta
                TerminalCard(accent = changeColor) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), Arrangement.spacedBy(4.dp)) {
                        SectionLabel("Last // Price")
                        Text(
                            text  = "%.2f".format(state.price),
                            style = PriceStyleLarge
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.percentChange >= 0) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = changeColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text       = "${"%+.2f".format(state.percentChange)}%   TODAY",
                                fontFamily = MonoFamily,
                                color      = changeColor,
                                style      = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                TerminalCard {
                    Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(10.dp)) {
                        SectionLabel("Chart // Price History")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(Timeframe.ONE_MONTH, Timeframe.THREE_MONTHS, Timeframe.ONE_YEAR).forEach { tf ->
                                FilterChip(
                                    selected = state.selectedTimeframe == tf,
                                    onClick  = { viewModel.onTimeframeSelected(tf) },
                                    shape    = RoundedCornerShape(2.dp),
                                    label    = {
                                        Text(
                                            text       = tf.label,
                                            fontFamily = MonoFamily,
                                            style      = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Amber,
                                        selectedLabelColor     = TerminalBlack
                                    )
                                )
                            }
                        }
                        HorizontalDivider(color = TerminalBorder)
                        when {
                            state.isCandleLoading -> Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                                CircularProgressIndicator(color = Amber, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                            }
                            state.candleError != null -> Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                                Text(state.candleError!!, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            candle != null && candle.hasData -> StockChart(prices = candle.closePrices, modifier = Modifier.fillMaxWidth())
                            else -> Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                                Text("NO CHART DATA", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                TerminalCard {
                    Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(10.dp)) {
                        SectionLabel("Fundamentals // Key Stats")
                        HorizontalDivider(color = TerminalBorder)
                        when {
                            state.isProfileLoading -> Box(Modifier.fillMaxWidth().height(60.dp), Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = Amber, strokeWidth = 2.dp)
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

                TerminalCard(accent = Amber) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(12.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            SectionLabel("Alerts // Price Trigger")
                            Text("Set Price Alert", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text  = "Get notified when ${state.symbol} crosses your target.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        HorizontalDivider(color = TerminalBorder)
                        OutlinedTextField(
                            value           = state.alertPriceInput,
                            onValueChange   = { viewModel.onAlertPriceChanged(it) },
                            modifier        = Modifier.fillMaxWidth(),
                            label           = { Text("Alert Price (USD)") },
                            placeholder     = { Text("e.g. ${"%.2f".format(state.price)}") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine      = true,
                            shape           = RoundedCornerShape(2.dp),
                            prefix          = { Text("$", fontFamily = MonoFamily) }
                        )
                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick  = { viewModel.onSetAlert(AlertDirection.ABOVE) },
                                modifier = Modifier.weight(1f),
                                enabled  = state.alertPriceInput.isNotBlank(),
                                shape    = RoundedCornerShape(2.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = PriceUp, contentColor = TerminalBlack)
                            ) { Text("▲ ABOVE", style = MaterialTheme.typography.labelLarge) }
                            Button(
                                onClick  = { viewModel.onSetAlert(AlertDirection.BELOW) },
                                modifier = Modifier.weight(1f),
                                enabled  = state.alertPriceInput.isNotBlank(),
                                shape    = RoundedCornerShape(2.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = PriceDown, contentColor = TerminalBlack)
                            ) { Text("▼ BELOW", style = MaterialTheme.typography.labelLarge) }
                        }
                        if (state.alertSetSuccess) {
                            Text("✓ ALERT SET SUCCESSFULLY", color = PriceUp, style = MaterialTheme.typography.labelLarge)
                        }
                        state.alertError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                TerminalCard(accent = MaterialTheme.colorScheme.secondary) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(12.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            SectionLabel("Simulator // Target Return", accent = MaterialTheme.colorScheme.secondary)
                            Text("Target Return Simulator", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text  = "Based on historical patterns only — not a prediction or financial advice.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        HorizontalDivider(color = TerminalBorder)
                        OutlinedTextField(
                            value           = state.targetPriceInput,
                            onValueChange   = { viewModel.onTargetPriceChanged(it) },
                            modifier        = Modifier.fillMaxWidth(),
                            label           = { Text("Target Price (USD)") },
                            placeholder     = { Text("e.g. ${"%.2f".format(state.price * 1.2)}") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine      = true,
                            shape           = RoundedCornerShape(2.dp),
                            prefix          = { Text("$", fontFamily = MonoFamily) }
                        )
                        Text("TIME HORIZON", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Horizon.entries.forEach { horizon ->
                                FilterChip(
                                    selected = state.selectedHorizon == horizon,
                                    onClick  = { viewModel.onHorizonSelected(horizon) },
                                    shape    = RoundedCornerShape(2.dp),
                                    label    = {
                                        Text(
                                            text       = horizon.label,
                                            fontFamily = MonoFamily,
                                            style      = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor     = TerminalBlack
                                    )
                                )
                            }
                        }
                        Button(
                            onClick  = { viewModel.onCalculate() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled  = state.targetPriceInput.isNotBlank() && !state.isAnalyticsLoading,
                            shape    = RoundedCornerShape(2.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor   = TerminalBlack
                            )
                        ) {
                            if (state.isAnalyticsLoading) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = TerminalBlack)
                            } else {
                                Text("▶ CALCULATE", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        state.analyticsError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        state.analyticsResult?.let { AnalyticsResultCard(it) }
                    }
                }

                TerminalCard {
                    Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(10.dp)) {
                        SectionLabel("News // ${state.symbol}")
                        HorizontalDivider(color = TerminalBorder)
                        when {
                            state.isNewsLoading -> Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(24.dp), color = Amber, strokeWidth = 2.dp)
                            }
                            state.newsError != null -> Text(state.newsError!!, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            state.news.isEmpty() -> Text("NO RECENT NEWS FOR ${state.symbol}", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                            else -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { state.news.take(5).forEach { NewsCard(it) } }
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
        Text(
            text  = label.uppercase(),
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text       = value,
            fontFamily = MonoFamily,
            style      = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AnalyticsResultCard(result: AnalyticsResult) {
    HorizontalDivider(color = TerminalBorder)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val gainColor = if (result.gainNeededPct >= 0) PriceUp else PriceDown
        StatRowColored("Gain Needed",        "${"%+.1f".format(result.gainNeededPct)}%", gainColor)
        val probColor = when {
            result.probabilityPct >= 60 -> PriceUp
            result.probabilityPct >= 30 -> Amber
            else                        -> PriceDown
        }
        StatRowColored("Historical Probability", "${"%.0f".format(result.probabilityPct)}%", probColor)
        result.medianDays?.let { weeks ->
            StatRowColored("Median Weeks to Target", "$weeks weeks", MaterialTheme.colorScheme.onSurface)
        }
        result.maxDrawdownPct?.let { drawdown ->
            StatRowColored("Max Drawdown Risk", "${"%.1f".format(drawdown)}%", PriceDown)
        }
        Text(
            text      = "Based on ${result.dataPointsUsed} trading days of history. Past patterns do not guarantee future results.",
            style     = MaterialTheme.typography.labelSmall,
            color     = TextSecondary,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatRowColored(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(
            text  = label.uppercase(),
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text       = value,
            fontFamily = MonoFamily,
            style      = MaterialTheme.typography.bodyMedium,
            color      = valueColor
        )
    }
}
