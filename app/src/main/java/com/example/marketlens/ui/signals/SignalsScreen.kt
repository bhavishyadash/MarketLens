package com.example.marketlens.ui.signals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.NewsSignal
import com.example.marketlens.data.model.SignalStrength
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.SignalsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SignalsScreen(viewModel: SignalsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.refresh() }) { Text("Retry") }
            }
        }
        state.signals.isEmpty() -> EmptySignals()
        else -> Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Market Signals", style = MaterialTheme.typography.titleLarge)
                    Text("Derived from news — informational only", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Filled.Refresh, "Refresh") }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.signals, key = { it.id }) { signal ->
                    SignalCard(
                        signal   = signal,
                        onDelete = { viewModel.deleteSignal(signal.id) },
                        onRead   = { viewModel.markRead(signal.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SignalCard(signal: NewsSignal, onDelete: () -> Unit, onRead: () -> Unit) {
    val strengthColor = when (signal.strength) {
        SignalStrength.HIGH   -> PriceDown
        SignalStrength.MEDIUM -> Color(0xFFFFA726)
        SignalStrength.LOW    -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alpha = if (signal.isRead) 0.6f else 1f

    Card(
        onClick = { if (!signal.isRead) onRead() },
        colors  = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(8.dp)) {

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TrendingUp, null, tint = strengthColor.copy(alpha = alpha), modifier = Modifier.size(18.dp))
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = strengthColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                                Text(
                                    signal.strength.name,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = strengthColor
                                )
                            }
                            Surface(color = PriceUp.copy(alpha = 0.10f), shape = MaterialTheme.shapes.small) {
                                Text(
                                    signal.sector,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = PriceUp
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Text(signal.headline, style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)), maxLines = 2)

            Text(signal.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))

            if (signal.affectedSymbols.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Your watchlist:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    signal.affectedSymbols.forEach { symbol ->
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                            Text(symbol, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Text(
                SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(signal.detectedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
            )
        }
    }
}

@Composable
private fun EmptySignals() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("No signals yet", style = MaterialTheme.typography.titleMedium)
            Text(
                "Signals are generated from financial news every 30 minutes.\nMake sure signals are enabled in Settings.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
