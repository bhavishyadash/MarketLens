package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.RealMarketRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketsViewModel(
    private val repo: MarketRepository = AppContainer.repository
) : ViewModel() {

    private val _state = MutableStateFlow(MarketsState(isLoading = true))
    val state: StateFlow<MarketsState> = _state.asStateFlow()

    /*
        Expanded to 50 stocks across all major sectors.
        Previously 12 stocks with 12 separate API calls.
        Now 50 stocks with ONE bulk Yahoo Finance call.
        Load time drops from ~5s to ~1s.
    */
    private val defaultSymbols = listOf(
        // Technology
        "AAPL", "MSFT", "NVDA", "GOOGL", "META", "AMZN", "AMD", "INTC", "TSMC", "ORCL",
        // Defense & Aerospace
        "LMT", "NOC", "RTX", "GD", "BA", "LHX",
        // Energy
        "XOM", "CVX", "COP", "SLB", "BP", "EOG",
        // Financials
        "JPM", "BAC", "GS", "MS", "V", "MA", "BRK-B",
        // Healthcare
        "JNJ", "PFE", "MRK", "ABBV", "UNH", "LLY",
        // Automotive & EV
        "TSLA", "F", "GM", "RIVN",
        // Consumer
        "WMT", "TGT", "COST", "NKE", "MCD",
        // Telecom & Media
        "NFLX", "DIS", "CMCSA", "T", "VZ",
        // Other
        "SPY", "QQQ"
    )

    private var searchJob: Job? = null

    init { loadMarkets() }

    fun refresh() { loadMarkets() }

    private fun loadMarkets() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            /*
                Cast to RealMarketRepository to access getBulkQuotes.
                This sends ONE request for all 50 symbols instead of 50 requests.
                Falls back to individual calls if cast fails (e.g. FakeMarketRepository).
            */
            val result = when (repo) {
                is RealMarketRepository -> repo.getBulkQuotes(defaultSymbols)
                else -> {
                    val quotes = defaultSymbols.mapNotNull {
                        (repo.getQuote(it) as? ApiResult.Success)?.data
                    }
                    ApiResult.Success(quotes)
                }
            }

            when (result) {
                is ApiResult.Success -> {
                    val stocks = result.data.map {
                        StockRowUi(it.symbol, it.name, it.price, it.percentChange)
                    }
                    if (stocks.isEmpty()) {
                        _state.value = MarketsState(isLoading = false, errorMessage = "Could not load market data.")
                    } else {
                        _state.value = MarketsState(allStocks = stocks, filteredStocks = stocks, isLoading = false)
                    }
                }
                is ApiResult.Error -> {
                    _state.value = MarketsState(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _state.value = _state.value.copy(query = newQuery)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            val q = newQuery.trim()
            val filtered = if (q.isEmpty()) _state.value.allStocks
            else _state.value.allStocks.filter {
                it.symbol.contains(q, ignoreCase = true) || it.name.contains(q, ignoreCase = true)
            }
            _state.value = _state.value.copy(filteredStocks = filtered)
        }
    }
}
