package com.example.marketlens.ui.markets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.MarketsViewModel
import com.example.marketlens.viewmodel.StockRowUi

@Composable
fun MarketsScreen(onStockClick: (StockRowUi) -> Unit, viewModel: MarketsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        else -> Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(4.dp))
            Text("Markets", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = state.query, onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search symbol or name…") }, singleLine = true
            )
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(state.filteredStocks, key = { it.symbol }) { StockRow(it) { onStockClick(it) } }
                if (state.filteredStocks.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                            Text("No results for \"${state.query}\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockRow(stock: StockRowUi, onClick: () -> Unit) {
    val changeColor = if (stock.isUp) PriceUp else PriceDown
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column {
                Text(stock.symbol, style = MaterialTheme.typography.titleMedium)
                Text(stock.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.2f".format(stock.price)}")
                Text("${"%.2f".format(stock.percentChange)}%", color = changeColor)
            }
        }
    }
}