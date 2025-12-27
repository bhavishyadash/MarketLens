package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NewsViewModel : ViewModel() {

    private val _state = MutableStateFlow(NewsState(isLoading = true))
    val state: StateFlow<NewsState> = _state.asStateFlow()

    init {
        loadFakeNews()
    }

    private fun loadFakeNews() {
        val items = listOf(
            NewsArticleUi("Tech stocks rise as markets digest new inflation data", "Reuters", "12m ago"),
            NewsArticleUi("NVIDIA leads chipmakers higher amid AI demand", "Bloomberg", "38m ago"),
            NewsArticleUi("Fed commentary pushes yields up; equities mixed", "WSJ", "1h ago"),
            NewsArticleUi("Oil climbs; energy sector outperforms", "CNBC", "2h ago"),
            NewsArticleUi("Market recap: NASDAQ ends green, volatility cools", "Financial Times", "4h ago")
        )

        _state.value = NewsState(
            isLoading = false,
            errorMessage = null,
            articles = items
        )
    }
}