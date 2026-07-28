package com.example.marketlens.ui.news

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.data.model.NewsArticle
import com.example.marketlens.data.model.NewsSignal
import com.example.marketlens.data.model.SignalStrength
import com.example.marketlens.ui.components.ErrorView
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
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor   = MaterialTheme.colorScheme.background,
            contentColor     = Amber,
            divider          = { HorizontalDivider(color = TerminalBorder) }
        ) {
            Tab(
                selected = selectedTab == NewsTab.NEWS,
                onClick  = { selectedTab = NewsTab.NEWS },
                selectedContentColor = Amber,
                unselectedContentColor = TextSecondary,
                text = { Text("NEWS", style = MaterialTheme.typography.labelLarge) }
            )
            Tab(
                selected = selectedTab == NewsTab.SIGNALS,
                onClick  = { selectedTab = NewsTab.SIGNALS },
                selectedContentColor = Amber,
                unselectedContentColor = TextSecondary,
                text = { Text("SIGNALS", style = MaterialTheme.typography.labelLarge) }
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
        state.isLoading -> LoadingView(label = "Fetching newsflow")
        state.errorMessage != null -> ErrorView(message = state.errorMessage!!, onRetry = { viewModel.refresh() })
        state.articles.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("NO ARTICLES AVAILABLE", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
        }
        else -> Column(Modifier.fillMaxSize().padding(horizontal = 14.dp), Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    SectionLabel("Newsflow // Market")
                    Text("News", style = MaterialTheme.typography.displaySmall)
                }
                IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Filled.Refresh, "Refresh") }
            }
            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
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
        state.isLoading -> LoadingView(label = "Scanning news feed")
        state.errorMessage != null -> ErrorView(message = state.errorMessage!!, onRetry = { viewModel.refresh() })
        state.signals.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("NO SIGNALS YET", color = Cyan, style = MaterialTheme.typography.labelLarge)
                Text(
                    text  = "Signals are generated from news every 30 minutes.\nMake sure signals are enabled in Settings.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        else -> Column(Modifier.fillMaxSize().padding(horizontal = 14.dp), Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(8.dp))
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
                Modifier.fillMaxSize(),
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
fun NewsCard(article: NewsArticle) {
    TerminalCard(accent = Amber) {
        Column(Modifier.fillMaxWidth().padding(14.dp), Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                article.sector?.let { sector ->
                    StatusPill(sector, color = PriceUp)
                }
                Text(
                    text       = article.source.uppercase(),
                    fontFamily = MonoFamily,
                    color      = TextSecondary,
                    style      = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                text     = article.headline,
                style    = MaterialTheme.typography.titleSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (article.summary.isNotBlank()) {
                Text(
                    text     = article.summary,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text       = formatTimestamp(article.publishedAt).uppercase(),
                fontFamily = MonoFamily,
                style      = MaterialTheme.typography.labelSmall,
                color      = TextSecondary
            )
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
                        Icons.AutoMirrored.Filled.TrendingUp, null,
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
                text     = signal.headline,
                style    = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            Text(
                text  = signal.reason,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = alpha)
            )
            if (signal.affectedSymbols.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("HITS:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    signal.affectedSymbols.forEach { symbol ->
                        Text(
                            text       = symbol,
                            fontFamily = MonoFamily,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.onSurface,
                            modifier   = Modifier
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
