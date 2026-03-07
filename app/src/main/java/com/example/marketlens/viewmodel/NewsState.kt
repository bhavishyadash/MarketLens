package com.example.marketlens.viewmodel

import com.example.marketlens.data.model.NewsArticle

data class NewsState(
    val isLoading:    Boolean           = true,
    val errorMessage: String?           = null,
    val articles:     List<NewsArticle> = emptyList()
)