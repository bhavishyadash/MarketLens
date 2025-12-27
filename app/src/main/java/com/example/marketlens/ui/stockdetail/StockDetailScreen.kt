package com.example.marketlens.ui.stockdetail

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.marketlens.viewmodel.StockDetailViewModel

@Composable
fun StockDetailScreen(
    viewModel: StockDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.errorMessage != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.errorMessage ?: "Something went wrong")
        }
        return
    }

    val isUp = state.percentChange >= 0.0
    val changeColor = if (isUp) Color(0xFF4CAF50) else Color(0xFFF44336)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text(text = state.symbol, style = MaterialTheme.typography.titleLarge)
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D23))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Price", color = Color.Gray)
                Text(
                    text = "$${"%.2f".format(state.price)}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "${"%.2f".format(state.percentChange)}% today",
                    color = changeColor
                )
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D23))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Chart (placeholder)", style = MaterialTheme.typography.titleMedium)
                Divider(color = Color(0xFF2A2F3A))
                Text("Timeframes: 1D 1W 1M 1Y", color = Color.Gray)
                Spacer(Modifier.height(140.dp))
                Text("Chart goes here later", color = Color.Gray)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D23))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Key Stats", style = MaterialTheme.typography.titleMedium)
                StatRow("Market Cap", "—")
                StatRow("P/E", "—")
                StatRow("52W High", "—")
                StatRow("52W Low", "—")
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D23))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Target Return Simulator", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Coming soon: probability + time-to-target estimates (informational).",
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value)
    }
}