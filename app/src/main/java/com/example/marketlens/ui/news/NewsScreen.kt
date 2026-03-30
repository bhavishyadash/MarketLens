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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.NewsArticle
import com.example.marketlens.ui.components.ErrorView
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.viewmodel.NewsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewsScreen(viewModel: NewsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "news_content"
    ) { s ->
        when {
            s.isLoading -> LoadingView()
            s.errorMessage != null -> ErrorView(message = s.errorMessage, onRetry = { viewModel.refresh() })
            s.articles.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No articles available", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Market News", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Filled.Refresh, "Refresh") }
                }
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(s.articles, key = { it.id }) { NewsCard(it) }
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
