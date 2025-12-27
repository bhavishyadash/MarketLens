package com.example.marketlens.ui.stockdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StockDetailScreen(
    symbol: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text(text = symbol, style = MaterialTheme.typography.titleLarge)
        }

        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Stock Detail (placeholder)")
                Text("Next: chart, timeframes, key stats, related news")
            }
        }
    }
}