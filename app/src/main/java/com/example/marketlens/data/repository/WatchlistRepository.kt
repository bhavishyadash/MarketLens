package com.example.marketlens.data.repository

import com.example.marketlens.data.network.ApiResult

interface WatchlistRepository {
    suspend fun getWatchlistSymbols(): ApiResult<List<String>>
    suspend fun addSymbol(symbol: String): ApiResult<Unit>
    suspend fun removeSymbol(symbol: String): ApiResult<Unit>
    suspend fun isInWatchlist(symbol: String): Boolean
}