package com.example.marketlens.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.*
import com.example.marketlens.viewmodel.DashboardState
import com.example.marketlens.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = state.errorMessage ?: "Error")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Market Overview",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.indices.forEach { index ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1D23)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = index.name, color = Color.Gray)
                            Text(text = index.currentValue.toString())
                            Text(
                                text = "${index.percentChange}%",
                                color = if (index.isUp) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1D23)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Market Snapshot", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Top Gainer: ${state.topGainer?.symbol ?: "-"}")
                    Text("Top Loser: ${state.topLoser?.symbol ?: "-"}")
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1D23)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Watchlist Preview", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    state.watchlistPreview.forEach { stock ->
                        Text("${stock.symbol}  ${stock.price}  ${stock.percentChange}%")
                    }
                }
            }
        }

        items(state.recentAlerts) { alert ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1D23)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(alert.message)
                    Text(
                        alert.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}