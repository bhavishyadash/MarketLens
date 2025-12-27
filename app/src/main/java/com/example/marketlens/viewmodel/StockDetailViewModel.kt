package com.example.marketlens.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.network.ApiResult
import com.example.marketlens.repository.FakeMarketRepository
import com.example.marketlens.repository.MarketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repo: MarketRepository = FakeMarketRepository()

    private val _state = MutableStateFlow(StockDetailState())
    val state: StateFlow<StockDetailState> = _state.asStateFlow()

    private val symbol: String = savedStateHandle["symbol"] ?: "UNKNOWN"

    init {
        load(symbol)
    }

    private fun load(symbol: String) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = repo.getQuote(symbol)) {
                is ApiResult.Success -> {
                    val item = result.data
                    _state.value = StockDetailState(
                        symbol = item.symbol,
                        price = item.price,
                        percentChange = item.percentChange,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is ApiResult.Error -> {
                    _state.value = StockDetailState(
                        symbol = symbol,
                        price = 0.0,
                        percentChange = 0.0,
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}