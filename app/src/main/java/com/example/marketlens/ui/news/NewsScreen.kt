package com.example.marketlens.ui.news

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.viewmodel.NewsArticleUi
import com.example.marketlens.viewmodel.NewsViewModel

@Composable
fun NewsScreen(
    viewModel: NewsViewModel = viewModel()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("News", style = MaterialTheme.typography.titleLarge)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.articles) { article ->
                NewsCard(article)
            }
        }
    }
}

@Composable
private fun NewsCard(article: NewsArticleUi) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D23))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(article.title, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(article.source, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Text(article.time, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}