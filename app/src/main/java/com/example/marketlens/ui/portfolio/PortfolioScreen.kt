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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.HoldingSnapshot
import com.example.marketlens.data.model.AnalyticsResult
import com.example.marketlens.ui.components.ErrorView
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.PortfolioHorizon
import com.example.marketlens.viewmodel.PortfolioViewModel

@Composable
fun PortfolioScreen(viewModel: PortfolioViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingView()
        state.errorMessage != null -> ErrorView(message = state.errorMessage!!, onRetry = null)
        else -> Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                    symbol        = state.formSymbol,
                    shares        = state.formShares,
                    purchasePrice = state.formPurchasePrice,
                    isSaving      = state.isSaving,
                    error         = state.formError,
                    onSymbolChanged        = viewModel::onFormSymbolChanged,
                    onSharesChanged        = viewModel::onFormSharesChanged,
                    onPurchasePriceChanged = viewModel::onFormPurchasePriceChanged,
                    onAdd    = { viewModel.onAddHolding() },
                    onCancel = { viewModel.onHideForm() }
                )
            }

            if (state.holdings.isEmpty() && !state.showForm) {
                EmptyPortfolio(onAdd = { viewModel.onShowForm() })
            } else if (state.holdings.isNotEmpty()) {

                state.result?.let { result ->
                    PortfolioValueCard(
                        currentValue     = result.currentValue,
                        totalGainLoss    = result.totalGainLoss,
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
                                        Text("${holding.shares} shares @ ${"$%.2f".format(holding.purchasePrice)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text(
                                "Based on historical patterns only — not a prediction or financial advice.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        OutlinedTextField(
                            value           = state.targetGainPct,
                            onValueChange   = viewModel::onTargetGainChanged,
                            modifier        = Modifier.fillMaxWidth(),
                            label           = { Text("Target Gain (%)") },
                            placeholder     = { Text("e.g. 20") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine      = true,
                            suffix          = { Text("%") }
                        )

                        Text("Time Horizon", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                            PortfolioHorizon.entries.forEach { horizon ->
                                FilterChip(
                                    selected = state.selectedHorizon == horizon,
                                    onClick  = { viewModel.onHorizonSelected(horizon) },
                                    label    = { Text(horizon.label) }
                                )
                            }
                        }

                        Button(
                            onClick  = { viewModel.onSimulate() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled  = !state.isSimulating
                        ) {
                            if (state.isSimulating) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Run Simulation")
                            }
                        }

                        state.simulationError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        state.result?.simulation?.let { simulation ->
                            SimulationResultCard(
                                simulation       = simulation,
                                currentValue     = state.result!!.currentValue,
                                targetGainPct    = state.targetGainPct.toDoubleOrNull() ?: 0.0,
                                horizonLabel     = state.selectedHorizon.label
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AddHoldingForm(
    symbol:        String,
    shares:        String,
    purchasePrice: String,
    isSaving:      Boolean,
    error:         String?,
    onSymbolChanged:        (String) -> Unit,
    onSharesChanged:        (String) -> Unit,
    onPurchasePriceChanged: (String) -> Unit,
    onAdd:    () -> Unit,
    onCancel: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Add Holding", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, "Cancel") }
            }
            OutlinedTextField(
                value         = symbol,
                onValueChange = onSymbolChanged,
                modifier      = Modifier.fillMaxWidth(),
                label         = { Text("Stock Symbol") },
                placeholder   = { Text("e.g. AAPL") },
                singleLine    = true
            )
            OutlinedTextField(
                value           = shares,
                onValueChange   = onSharesChanged,
                modifier        = Modifier.fillMaxWidth(),
                label           = { Text("Number of Shares") },
                placeholder     = { Text("e.g. 10") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine      = true
            )
            OutlinedTextField(
                value           = purchasePrice,
                onValueChange   = onPurchasePriceChanged,
                modifier        = Modifier.fillMaxWidth(),
                label           = { Text("Purchase Price (USD)") },
                placeholder     = { Text("e.g. 175.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine      = true,
                prefix          = { Text("$") }
            )
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick  = onAdd,
                modifier = Modifier.fillMaxWidth(),
                enabled  = !isSaving
            ) {
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
            Text(
                "$gainSign${"$%.2f".format(totalGainLoss)} ($gainSign${"%.2f".format(totalGainLossPct)}%)",
                color = gainColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HoldingRow(snapshot: HoldingSnapshot, onDelete: () -> Unit) {
    val gainColor = if (snapshot.gainLoss >= 0) PriceUp else PriceDown
    val gainSign  = if (snapshot.gainLoss >= 0) "+" else ""

    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(snapshot.symbol, style = MaterialTheme.typography.titleMedium)
            Text(
                "${snapshot.shares} shares @ ${"$%.2f".format(snapshot.purchasePrice)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$%.2f".format(snapshot.currentValue), style = MaterialTheme.typography.bodyMedium)
            Text(
                "$gainSign${"%.2f".format(snapshot.gainLossPct)}%",
                style = MaterialTheme.typography.bodySmall,
                color = gainColor
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SimulationResultCard(
    simulation:    AnalyticsResult,
    currentValue:  Double,
    targetGainPct: Double,
    horizonLabel:  String
) {
    val targetValue   = currentValue * (1 + targetGainPct / 100.0)
    val probColor = when {
        simulation.probabilityPct >= 60 -> PriceUp
        simulation.probabilityPct >= 30 -> MaterialTheme.colorScheme.onSurface
        else                            -> PriceDown
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Simulation Results — $horizonLabel", style = MaterialTheme.typography.titleSmall)
        SimRow("Target Portfolio Value", "$%.2f".format(targetValue), null)
        SimRow("Historical Probability", "${"%.0f".format(simulation.probabilityPct)}%", probColor)
        simulation.medianDays?.let { weeks ->
            SimRow("Median Weeks to Target", "$weeks weeks", null)
        }
        simulation.maxDrawdownPct?.let { drawdown ->
            SimRow("Max Drawdown Risk", "${"%.1f".format(drawdown)}%", PriceDown)
        }
        SimRow("Data Points Used", "${simulation.dataPointsUsed} weekly snapshots", null)
        Text(
            "Based on blended historical portfolio performance. Past patterns do not guarantee future results. This is educational only.",
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SimRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color?) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyPortfolio(onAdd: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Your portfolio is empty", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onAdd) { Text("Add first holding") }
        }
    }
}