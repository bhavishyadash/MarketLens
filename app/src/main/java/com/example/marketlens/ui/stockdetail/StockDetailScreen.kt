package com.example.marketlens.ui.stockdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StockDetailScreen(
    symbol: String,
    price: Double,
    percentChange: Double,
    onBack: () -> Unit
) {
    val isUp = percentChange >= 0.0
    val changeColor = if (isUp) Color(0xFF4CAF50) else Color(0xFFF44336)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top bar: Back + Symbol
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text(text = symbol, style = MaterialTheme.typography.titleLarge)
        }

        // Price header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D23))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Price", color = Color.Gray)
                Text(
                    text = "$${"%.2f".format(price)}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "${"%.2f".format(percentChange)}% today",
                    color = changeColor
                )
            }
        }

        // Chart placeholder
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D23))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Chart (placeholder)", style = MaterialTheme.typography.titleMedium)
                Divider(color = Color(0xFF2A2F3A))
                Text("Timeframes: 1D 1W 1M 1Y", color = Color.Gray)
                Spacer(Modifier.height(140.dp))
                Text(
                    "Chart goes here later (MPAndroidChart or custom Canvas)",
                    color = Color.Gray
                )
            }
        }

        // Key stats placeholder
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D23))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Key Stats", style = MaterialTheme.typography.titleMedium)
                StatRow("Market Cap", "—")
                StatRow("P/E", "—")
                StatRow("52W High", "—")
                StatRow("52W Low", "—")
            }
        }

        // Planned feature placeholder
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D23))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value)
    }
}