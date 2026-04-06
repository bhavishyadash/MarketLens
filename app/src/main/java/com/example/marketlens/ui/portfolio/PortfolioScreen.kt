package com.example.marketlens.ui.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.AnalyticsResult
import com.example.marketlens.data.model.HoldingSnapshot
import com.example.marketlens.ui.components.ErrorView
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.PortfolioHorizon
import com.example.marketlens.viewmodel.PortfolioViewModel

@Composable
fun PortfolioScreen(viewModel: PortfolioViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    when {
        state.isLoading -> LoadingView()
        state.errorMessage != null -> ErrorView(message = state.errorMessage!!, onRetry = null)
        else -> Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Portfolio Simulator", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { viewModel.onShowForm() }) {
                    Icon(Icons.Filled.Add, "Add holding", tint = PriceUp)
                }
            }

            if (state.showForm) {
                AddHoldingForm(
                    symbols = state.watchlistSymbols,
                    selectedSymbol = state.formSymbol,
                    shares = state.formShares,
                    purchasePrice = state.formPurchasePrice,
                    isSaving = state.isSaving,
                    error = state.formError,
                    isDropdownExpanded = state.isDropdownExpanded,
                    onDropdownExpand = { viewModel.onDropdownExpand() },
                    onDropdownDismiss = { viewModel.onDropdownDismiss() },
                    onSymbolSelected = { viewModel.onSymbolSelected(it) },
                    onSharesChanged = { viewModel.onFormSharesChanged(it) },
                    onPurchasePriceChanged = { viewModel.onFormPurchasePriceChanged(it) },
                    onAdd = { viewModel.onAddHolding() },
                    onCancel = { viewModel.onHideForm() }
                )
            }

            if (state.holdings.isEmpty() && !state.showForm) {
                EmptyPortfolio(onAdd = { viewModel.onShowForm() })
            } else if (state.holdings.isNotEmpty()) {

                state.result?.let { result ->
                    PortfolioValueCard(
                        currentValue = result.currentValue,
                        totalGainLoss = result.totalGainLoss,
                        totalGainLossPct = result.totalGainLossPct
                    )
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(10.dp)) {
                        Text("Holdings", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        if (state.result != null) {
                            state.result!!.holdings.forEach { snapshot ->
                                HoldingRow(snapshot = snapshot, onDelete = { viewModel.onDeleteHolding(snapshot.symbol) })
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        } else {
                            state.holdings.forEach { holding ->
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Column {
                                        Text(holding.symbol, style = MaterialTheme.typography.titleMedium)
                                        Text("${holding.shares} shares @ ${"$%.2f".format(holding.purchasePrice)}",
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { viewModel.onDeleteHolding(holding.symbol) }) {
                                        Icon(Icons.Filled.Delete, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Simulate Portfolio Growth", style = MaterialTheme.typography.titleMedium)
                            Text("Target is automatically derived from your portfolio's 2-year historical performance — not a prediction or financial advice.",
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        state.result?.let { result ->
                            val twoYearSign  = if (result.historicalGainPct >= 0) "+" else ""
                            val twoYearColor = if (result.historicalGainPct >= 0) PriceUp else PriceDown
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("2Y Historical Portfolio Gain", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                Text("$twoYearSign${"%.1f".format(result.historicalGainPct)}%", color = twoYearColor, style = MaterialTheme.typography.bodyMedium)
                            }
                            val targetSign = if (result.scaledTargetPct >= 0) "+" else ""
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Scaled Target for Horizon", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                Text("$targetSign${"%.1f".format(result.scaledTargetPct)}%", style = MaterialTheme.typography.bodyMedium)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }

                        Text("Time Horizon", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                            PortfolioHorizon.entries.forEach { horizon ->
                                FilterChip(selected = state.selectedHorizon == horizon, onClick = { viewModel.onHorizonSelected(horizon) }, label = { Text(horizon.label) })
                            }
                        }

                        Button(onClick = { viewModel.onSimulate() }, modifier = Modifier.fillMaxWidth(), enabled = !state.isSimulating) {
                            if (state.isSimulating) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            else Text("Run Simulation")
                        }

                        state.simulationError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

                        state.result?.simulation?.let { simulation ->
                            SimulationResultCard(simulation = simulation, currentValue = state.result!!.currentValue,
                                targetPct = state.result!!.scaledTargetPct, horizonLabel = state.selectedHorizon.label)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHoldingForm(
    symbols: List<String>, selectedSymbol: String, shares: String, purchasePrice: String,
    isSaving: Boolean, error: String?, isDropdownExpanded: Boolean,
    onDropdownExpand: () -> Unit, onDropdownDismiss: () -> Unit, onSymbolSelected: (String) -> Unit,
    onSharesChanged: (String) -> Unit, onPurchasePriceChanged: (String) -> Unit,
    onAdd: () -> Unit, onCancel: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Add Holding", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, "Cancel") }
            }
            ExposedDropdownMenuBox(expanded = isDropdownExpanded,
                onExpandedChange = { if (it) onDropdownExpand() else onDropdownDismiss() },
                modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedSymbol.ifBlank { "Select from watchlist" }, onValueChange = {},
                    readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(), label = { Text("Stock Symbol") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) }
                )
                ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = onDropdownDismiss) {
                    if (symbols.isEmpty()) {
                        DropdownMenuItem(text = { Text("No watchlist stocks found.\nAdd stocks to your watchlist first.", style = MaterialTheme.typography.bodySmall) }, onClick = onDropdownDismiss)
                    } else {
                        symbols.forEach { symbol -> DropdownMenuItem(text = { Text(symbol) }, onClick = { onSymbolSelected(symbol) }) }
                    }
                }
            }
            OutlinedTextField(value = shares, onValueChange = onSharesChanged, modifier = Modifier.fillMaxWidth(),
                label = { Text("Number of Shares") }, placeholder = { Text("e.g. 10") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            OutlinedTextField(value = purchasePrice, onValueChange = onPurchasePriceChanged, modifier = Modifier.fillMaxWidth(),
                label = { Text("Purchase Price (USD)") }, placeholder = { Text("e.g. 175.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, prefix = { Text("$") })
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth(), enabled = !isSaving) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text("Add")
            }
        }
    }
}

@Composable
private fun PortfolioValueCard(currentValue: Double, totalGainLoss: Double, totalGainLossPct: Double) {
    val gainColor = if (totalGainLoss >= 0) PriceUp else PriceDown
    val gainSign  = if (totalGainLoss >= 0) "+" else ""
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(6.dp)) {
            Text("Portfolio Value", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$%.2f".format(currentValue), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("$gainSign$%.2f ($gainSign%.2f%)".format(totalGainLoss, totalGainLossPct), color = gainColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun HoldingRow(snapshot: HoldingSnapshot, onDelete: () -> Unit) {
    val gainColor = if (snapshot.gainLoss >= 0) PriceUp else PriceDown
    val gainSign  = if (snapshot.gainLoss >= 0) "+" else ""
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(snapshot.symbol, style = MaterialTheme.typography.titleMedium)
            Text("${snapshot.shares} shares @ ${"$%.2f".format(snapshot.purchasePrice)}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$%.2f".format(snapshot.currentValue), style = MaterialTheme.typography.bodyMedium)
            Text("$gainSign%.2f%%".format(snapshot.gainLossPct), style = MaterialTheme.typography.bodySmall, color = gainColor)
        }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun SimulationResultCard(simulation: AnalyticsResult, currentValue: Double, targetPct: Double, horizonLabel: String) {
    val targetValue = currentValue * (1 + targetPct / 100.0)
    val probColor = when {
        simulation.probabilityPct >= 60 -> PriceUp
        simulation.probabilityPct >= 30 -> MaterialTheme.colorScheme.onSurface
        else -> PriceDown
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Results — $horizonLabel", style = MaterialTheme.typography.titleSmall)
        SimRow("Target Portfolio Value", "$%.2f".format(targetValue), null)
        SimRow("Historical Probability", "%.0f%%".format(simulation.probabilityPct), probColor)
        simulation.medianDays?.let { weeks -> SimRow("Median Weeks to Target", "$weeks weeks", null) }
        simulation.maxDrawdownPct?.let { drawdown -> SimRow("Max Drawdown Risk", "%.1f%%".format(drawdown), PriceDown) }
        SimRow("Data Points Used", "${simulation.dataPointsUsed} weekly snapshots", null)
        Text("Based on blended historical portfolio performance. Past patterns do not guarantee future results. This is educational only.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SimRow(label: String, value: String, valueColor: Color?) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor ?: MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EmptyPortfolio(onAdd: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("No holdings yet", style = MaterialTheme.typography.titleMedium)
            Text("Add stocks from your watchlist to simulate how your portfolio may perform",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            Button(onClick = onAdd) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add First Holding")
            }
        }
    }
}