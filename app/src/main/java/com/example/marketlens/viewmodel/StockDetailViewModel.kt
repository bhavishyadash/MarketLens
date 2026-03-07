package com.example.marketlens.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.FakeMarketRepository
import com.example.marketlens.data.repository.MarketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
    Bug fixed: imports now correctly point to data.network and data.repository
    (was missing the "data." prefix, which would cause unresolved reference errors
    once the package names are corrected in those files).

    The ViewModel receives the symbol from SavedStateHandle — Navigation Compose
    puts the route argument there automatically. No manual passing needed.

    Factory pattern: provides a clean way to swap FakeMarketRepository for
    RealMarketRepository in Phase 2 without touching this file.
*/
class StockDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MarketRepository = FakeMarketRepository()
    //                      ↑ Phase 2: replace FakeMarketRepository() with injected real one
) : ViewModel() {

    private val _state = MutableStateFlow(StockDetailState())
    val state: StateFlow<StockDetailState> = _state.asStateFlow()

    // Navigation Compose puts the {symbol} route arg into SavedStateHandle automatically.
    // If somehow it's missing, we fall back to "UNKNOWN" and show an error.
    private val symbol: String = savedStateHandle["symbol"] ?: "UNKNOWN"

    init {
        loadQuote(symbol)
    }

    private fun loadQuote(symbol: String) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        // viewModelScope is tied to this ViewModel's lifecycle.
        // If the user navigates away, the coroutine is cancelled automatically.
        viewModelScope.launch {
            when (val result = repository.getQuote(symbol)) {
                is ApiResult.Success -> {
                    val quote = result.data  // StockQuote domain model
                    _state.value = StockDetailState(
                        symbol        = quote.symbol,
                        name          = quote.name,
                        price         = quote.price,
                        percentChange = quote.percentChange,
                        isLoading     = false,
                        errorMessage  = null
                    )
                }
                is ApiResult.Error -> {
                    _state.value = StockDetailState(
                        symbol        = symbol,
                        isLoading     = false,
                        errorMessage  = result.message
                    )
                }
            }
        }
    }
}
