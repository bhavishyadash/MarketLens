package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(
    private val newsRepo: NewsRepository = AppContainer.newsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewsState(isLoading = true))
    val state: StateFlow<NewsState> = _state.asStateFlow()

    init { loadNews() }

    fun refresh() { loadNews() }

    private fun loadNews() {
        _state.value = NewsState(isLoading = true)
        viewModelScope.launch {
            when (val result = newsRepo.getMarketNews()) {
                is ApiResult.Success -> _state.value = NewsState(isLoading = false, articles = result.data)
                is ApiResult.Error   -> _state.value = NewsState(isLoading = false, errorMessage = result.message)
            }
        }
    }
}