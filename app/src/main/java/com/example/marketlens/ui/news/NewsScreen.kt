package com.example.marketlens.ui.news

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.NewsArticle
import com.example.marketlens.data.model.NewsSignal
import com.example.marketlens.data.model.SignalStrength
import com.example.marketlens.ui.components.ErrorView
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.NewsViewModel
import com.example.marketlens.viewmodel.SignalsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class NewsTab { NEWS, SIGNALS }

@Composable
fun NewsScreen(
    newsViewModel:    NewsViewModel    = viewModel(),
    signalsViewModel: SignalsViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(NewsTab.NEWS) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
                selected = selectedTab == NewsTab.NEWS,
                onClick  = { selectedTab = NewsTab.NEWS },
                text     = { Text("News") }
            )
            Tab(
                selected = selectedTab == NewsTab.SIGNALS,
                onClick  = { selectedTab = NewsTab.SIGNALS },
                text     = { Text("Signals") }
            )
        }

        AnimatedContent(
            targetState  = selectedTab,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
            label        = "news_tab_content",
            modifier     = Modifier.fillMaxSize()
        ) { tab ->
            when (tab) {
                NewsTab.NEWS    -> NewsContent(newsViewModel)
                NewsTab.SIGNALS -> SignalsContent(signalsViewModel)
            }
        }
    }
}

@Composable
private fun NewsContent(viewModel: NewsViewModel) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingView()
        state.errorMessage != null -> ErrorView(message = state.errorMessage!!, onRetry = { viewModel.refresh() })
        state.articles.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No articles available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        else -> Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Market News", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Filled.Refresh, "Refresh") }
            }
            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.articles, key = { it.id }) { NewsCard(it) }
            }
        }
    }
}

@Composable
private fun SignalsContent(viewModel: SignalsViewModel) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingView()
        state.errorMessage != null -> ErrorView(message = state.errorMessage!!, onRetry = { viewModel.refresh() })
        state.signals.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text("No signals yet", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Signals are generated from news every 30 minutes.\nMake sure signals are enabled in Settings.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        else -> Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Market Signals", style = MaterialTheme.typography.titleLarge)
                    Text("Derived from news — informational only", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Filled.Refresh, "Refresh") }
            }
            LazyColumn(
                Modifier.fillMaxSize(),
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
fun NewsCard(article: NewsArticle) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(8.dp)) {
            article.sector?.let { sector ->
                Surface(color = PriceUp.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                    Text(sector, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, color = PriceUp)
                }
            }
            Text(article.headline, style = MaterialTheme.typography.titleSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (article.summary.isNotBlank()) {
                Text(article.summary, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(article.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatTimestamp(article.publishedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = strengthColor.copy(alpha = alpha), modifier = Modifier.size(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(color = strengthColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                            Text(signal.strength.name, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = strengthColor)
                        }
                        Surface(color = PriceUp.copy(alpha = 0.10f), shape = MaterialTheme.shapes.small) {
                            Text(signal.sector, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = PriceUp)
                        }
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
            Text(signal.headline, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
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
            Text(SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(signal.detectedAt)),
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
        }
    }
}

private fun formatTimestamp(unixSeconds: Long): String {
    val diffMins  = (System.currentTimeMillis() - unixSeconds * 1000L) / 60_000
    val diffHours = diffMins / 60
    val diffDays  = diffHours / 24
    return when {
        diffMins  < 1  -> "Just now"
        diffMins  < 60 -> "${diffMins}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays  < 7  -> "${diffDays}d ago"
        else -> SimpleDateFormat("MMM d", Locale.US).format(Date(unixSeconds * 1000L))
    }
}