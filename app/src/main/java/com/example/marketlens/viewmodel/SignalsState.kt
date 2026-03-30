package com.example.marketlens.viewmodel

import com.example.marketlens.data.model.NewsSignal

data class SignalsState(
    val isLoading:    Boolean          = false,
    val errorMessage: String?          = null,
    val signals:      List<NewsSignal> = emptyList()
)
