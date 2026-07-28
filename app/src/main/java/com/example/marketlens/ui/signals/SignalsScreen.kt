package com.example.marketlens.ui.signals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.NewsSignal
import com.example.marketlens.data.model.SignalStrength
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.components.SectionLabel
import com.example.marketlens.ui.components.StatusPill
import com.example.marketlens.ui.components.TerminalCard
import com.example.marketlens.ui.theme.Amber
import com.example.marketlens.ui.theme.Cyan
import com.example.marketlens.ui.theme.MonoFamily
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.ui.theme.TerminalBorder
import com.example.marketlens.ui.theme.TerminalRaised
import com.example.marketlens.ui.theme.TextSecondary
import com.example.marketlens.viewmodel.SignalsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SignalsScreen(viewModel: SignalsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingView(label = "Scanning news feed")
        state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.refresh() }) { Text("Retry") }
            }
        }
        state.signals.isEmpty() -> EmptySignals()
        else -> Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    SectionLabel("Signals // Newsflow", accent = Cyan)
                    Text("Signals", style = MaterialTheme.typography.displaySmall)
                    Text(
                        text  = "Derived from news — informational only",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Filled.Refresh, "Refresh") }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
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
        SignalStrength.MEDIUM -> Amber
        SignalStrength.LOW    -> TextSecondary
    }
    val alpha = if (signal.isRead) 0.55f else 1f

    TerminalCard(accent = strengthColor) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.TrendingUp, null,
                        tint     = strengthColor.copy(alpha = alpha),
                        modifier = Modifier.size(16.dp)
                    )
                    StatusPill(text = signal.strength.name, color = strengthColor)
                    StatusPill(text = signal.sector,        color = PriceUp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!signal.isRead) {
                        TextButton(onClick = onRead, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)) {
                            Text("MARK READ", color = Amber, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Delete, "Delete", tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Text(
                text  = signal.headline,
                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)),
                maxLines = 2
            )

            Text(
                text  = signal.reason,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = alpha)
            )

            if (signal.affectedSymbols.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("HITS:", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    signal.affectedSymbols.forEach { symbol ->
                        Text(
                            text = symbol,
                            fontFamily = MonoFamily,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .background(TerminalRaised, RoundedCornerShape(2.dp))
                                .border(1.dp, TerminalBorder, RoundedCornerShape(2.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text       = SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(signal.detectedAt)),
                fontFamily = MonoFamily,
                style      = MaterialTheme.typography.labelSmall,
                color      = TextSecondary.copy(alpha = alpha)
            )
        }
    }
}

@Composable
private fun EmptySignals() {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("NO SIGNALS YET", color = Cyan, style = MaterialTheme.typography.labelLarge)
            Text(
                text  = "Signals are generated from financial news every 30 minutes.\nMake sure signals are enabled in Settings.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
