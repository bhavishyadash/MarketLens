package com.example.marketlens.viewmodel

data class NewsState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val articles: List<NewsArticleUi> = emptyList()
)

data class NewsArticleUi(
    val title: String,
    val source: String,
    val time: String
)