package com.example.marketlens.ui.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.AlertDirection
import com.example.marketlens.data.model.PriceAlert
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.components.SectionLabel
import com.example.marketlens.ui.components.TerminalCard
import com.example.marketlens.ui.theme.Amber
import com.example.marketlens.ui.theme.MonoFamily
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.ui.theme.TextSecondary
import com.example.marketlens.viewmodel.AlertsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsScreen(viewModel: AlertsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingView(label = "Loading alerts")
        state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.refresh() }) { Text("Retry") }
            }
        }
        state.alerts.isEmpty() -> EmptyAlerts()
        else -> Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    SectionLabel("Alerts // Price Triggers")
                    Text("Alerts", style = MaterialTheme.typography.displaySmall)
                }
                IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Filled.Refresh, "Refresh") }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.alerts, key = { it.id }) { alert ->
                    AlertCard(alert = alert, onDelete = { viewModel.deleteAlert(alert.id) })
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: PriceAlert, onDelete: () -> Unit) {
    val directionColor = if (alert.direction == AlertDirection.ABOVE) PriceUp else PriceDown
    val directionLabel = if (alert.direction == AlertDirection.ABOVE) "ABOVE" else "BELOW"
    val accent = if (alert.isTriggered) TextSecondary else directionColor

    TerminalCard(accent = accent) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = accent
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text       = alert.symbol,
                        fontFamily = MonoFamily,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text       = "$directionLabel  ${"$%.2f".format(alert.targetPrice)}",
                        fontFamily = MonoFamily,
                        style      = MaterialTheme.typography.bodySmall,
                        color      = directionColor
                    )
                    Text(
                        text  = if (alert.isTriggered) "✓ TRIGGERED" else "● ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (alert.isTriggered) TextSecondary else Amber
                    )
                    Text(
                        text  = SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(alert.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Delete alert", tint = TextSecondary)
            }
        }
    }
}

@Composable
private fun EmptyAlerts() {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("NO PRICE ALERTS", color = Amber, style = MaterialTheme.typography.labelLarge)
            Text(
                text  = "Open any stock and set an alert from the detail screen.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
