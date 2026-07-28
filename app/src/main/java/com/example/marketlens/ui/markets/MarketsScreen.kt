package com.example.marketlens.ui.markets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.ui.components.ErrorView
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.components.SectionLabel
import com.example.marketlens.ui.theme.Amber
import com.example.marketlens.ui.theme.MonoFamily
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.ui.theme.TerminalBorder
import com.example.marketlens.ui.theme.TerminalSurface
import com.example.marketlens.ui.theme.TextSecondary
import com.example.marketlens.viewmodel.MarketsViewModel
import com.example.marketlens.viewmodel.StockRowUi

@Composable
fun MarketsScreen(onStockClick: (StockRowUi) -> Unit, viewModel: MarketsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    AnimatedContent(
        targetState = state.isLoading to state.errorMessage,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "markets_content"
    ) { (isLoading, error) ->
        when {
            isLoading -> LoadingView(label = "Streaming market feed")
            error != null -> ErrorView(message = error, onRetry = { viewModel.refresh() })
            else -> Column(Modifier.fillMaxSize().padding(horizontal = 14.dp), Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.height(4.dp))
                SectionLabel("Trending // Instruments")
                Text("Markets", style = MaterialTheme.typography.displaySmall)
                OutlinedTextField(
                    value           = state.query,
                    onValueChange   = viewModel::onQueryChange,
                    modifier        = Modifier.fillMaxWidth(),
                    placeholder     = { Text("SEARCH SYMBOL OR NAME", style = MaterialTheme.typography.labelLarge) },
                    singleLine      = true,
                    shape           = RoundedCornerShape(3.dp),
                    leadingIcon     = { Icon(Icons.Filled.Search, null, tint = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Amber,
                        unfocusedBorderColor = TerminalBorder,
                        cursorColor          = Amber,
                        focusedTextColor     = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor   = MaterialTheme.colorScheme.onSurface
                    )
                )
                // Column headers
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SYMBOL", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                        Text("LAST",  color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Text("CHG%",  color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                }
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.filteredStocks, key = { it.symbol }) { stock ->
                        StockRow(stock) { onStockClick(stock) }
                    }
                    if (state.filteredStocks.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                                Text(
                                    "NO RESULTS FOR \"${state.query.uppercase()}\"",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(TerminalSurface)
            .border(width = 1.dp, color = TerminalBorder)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text       = stock.symbol,
                fontFamily = MonoFamily,
                style      = MaterialTheme.typography.titleMedium
            )
            Text(
                text  = stock.name,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text       = "%.2f".format(stock.price),
                fontFamily = MonoFamily,
                style      = MaterialTheme.typography.bodyMedium
            )
            Text(
                text       = "${"%+.2f".format(stock.percentChange)}%",
                fontFamily = MonoFamily,
                color      = changeColor,
                style      = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
