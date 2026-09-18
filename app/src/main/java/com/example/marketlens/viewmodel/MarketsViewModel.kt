package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.MarketRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

private const val SEARCH_DEBOUNCE_MS = 350L
private const val MAX_SEARCH_RESULTS = 12
private const val MAX_CONCURRENT_QUOTES = 4

class MarketsViewModel(
    private val repo: MarketRepository = AppContainer.repository
) : ViewModel() {

    private val _state = MutableStateFlow(MarketsState(isLoading = true))
    val state: StateFlow<MarketsState> = _state.asStateFlow()

    private val defaultSymbols = listOf(
        "AAPL", "MSFT", "NVDA", "TSLA", "AMZN",
        "GOOGL", "META", "JPM", "V", "UNH", "WMT", "XOM"
    )

    private var loadJob: Job? = null
    private var searchJob: Job? = null
    private val quoteSemaphore = Semaphore(MAX_CONCURRENT_QUOTES)

    init { loadDefaults() }

    fun refresh() {
        val query = _state.value.query.trim()
        if (query.isEmpty()) loadDefaults() else launchSearch(query, debounce = false)
    }

    fun onQueryChange(newQuery: String) {
        val query = newQuery.trim()

        loadJob?.cancel()
        searchJob?.cancel()

        if (query.isEmpty()) {
            val defaults = _state.value.allStocks
            _state.value = _state.value.copy(
                query = newQuery,
                filteredStocks = defaults,
                isLoading = defaults.isEmpty(),
                errorMessage = null,
                isSearching = false,
                searchError = null
            )
            if (defaults.isEmpty()) loadDefaults()
            return
        }

        _state.value = _state.value.copy(
            query = newQuery,
            filteredStocks = emptyList(),
            isLoading = false,
            errorMessage = null,
            isSearching = true,
            searchError = null
        )
        launchSearch(query, debounce = true)
    }

    private fun loadDefaults() {
        searchJob?.cancel()
        loadJob?.cancel()

        _state.value = _state.value.copy(
            isLoading = true,
            errorMessage = null,
            isSearching = false,
            searchError = null
        )

        loadJob = viewModelScope.launch {
            val batch = fetchQuoteRows(defaultSymbols.map { QuoteCandidate(it, it) })
            currentCoroutineContext().ensureActive()

            // A typed query supersedes the default load even if a data source
            // happens to swallow cancellation internally.
            if (_state.value.query.isNotBlank()) return@launch

            _state.value = if (batch.rows.isEmpty()) {
                _state.value.copy(
                    isLoading = false,
                    errorMessage = "Could not load market data. Try again."
                )
            } else {
                _state.value.copy(
                    allStocks = batch.rows,
                    filteredStocks = batch.rows,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    private fun launchSearch(query: String, debounce: Boolean) {
        loadJob?.cancel()
        searchJob?.cancel()

        _state.value = _state.value.copy(
            filteredStocks = emptyList(),
            isLoading = false,
            errorMessage = null,
            isSearching = true,
            searchError = null
        )

        searchJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MS)

            val searchResult = try {
                repo.searchSymbols(query)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                ApiResult.Error("Search failed: ${error.message ?: "Unknown error"}", error)
            }

            currentCoroutineContext().ensureActive()
            if (_state.value.query.trim() != query) return@launch

            when (searchResult) {
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        filteredStocks = emptyList(),
                        isSearching = false,
                        searchError = searchResult.message
                    )
                }

                is ApiResult.Success -> {
                    val candidates = searchResult.data
                        .asSequence()
                        .filter { it.symbol.isNotBlank() }
                        .distinctBy { it.symbol.uppercase() }
                        .take(MAX_SEARCH_RESULTS)
                        .map { QuoteCandidate(it.symbol, it.description) }
                        .toList()

                    val batch = fetchQuoteRows(candidates)
                    currentCoroutineContext().ensureActive()
                    if (_state.value.query.trim() != query) return@launch

                    val searchError = when {
                        batch.rows.isEmpty() && batch.failureCount > 0 ->
                            "Could not load quotes for \"$query\"."
                        batch.failureCount > 0 ->
                            "Some matching quotes could not be loaded."
                        else -> null
                    }

                    _state.value = _state.value.copy(
                        filteredStocks = batch.rows,
                        isSearching = false,
                        searchError = searchError
                    )
                }
            }
        }
    }

    private suspend fun fetchQuoteRows(candidates: List<QuoteCandidate>): QuoteBatch = coroutineScope {
        val results = candidates.map { candidate ->
            async {
                val result: ApiResult<StockQuote> = quoteSemaphore.withPermit {
                    try {
                        repo.getQuote(candidate.symbol)
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (error: Exception) {
                        ApiResult.Error("Could not load ${candidate.symbol}", error)
                    }
                }
                candidate to result
            }
        }.map { it.await() }

        val rows = results.mapNotNull { (candidate, result) ->
            val quote = (result as? ApiResult.Success)?.data ?: return@mapNotNull null
            val name = quote.name
                .takeUnless { it.isBlank() || it.equals(quote.symbol, ignoreCase = true) }
                ?: candidate.fallbackName.takeIf { it.isNotBlank() }
                ?: quote.symbol

            StockRowUi(quote.symbol, name, quote.price, quote.percentChange)
        }

        QuoteBatch(
            rows = rows,
            failureCount = results.count { (_, result) -> result is ApiResult.Error }
        )
    }
}

private data class QuoteCandidate(val symbol: String, val fallbackName: String)

private data class QuoteBatch(val rows: List<StockRowUi>, val failureCount: Int)
