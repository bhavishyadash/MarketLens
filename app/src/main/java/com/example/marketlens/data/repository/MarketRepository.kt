package com.example.marketlens.data.repository

import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult

/*
    Bug fixed (two things):
    1. Package was "com.example.marketlens.repository" — should be "data.repository"
    2. Was returning ApiResult<StockRowUi> — StockRowUi is a UI model that lives
       in the viewmodel package. The data layer must NEVER import from the UI layer.
       That's like a restaurant kitchen importing the menu — wrong direction.

    Now returns ApiResult<StockQuote> — a clean domain model that lives in data.model.
    The ViewModel receives StockQuote and maps it into StockRowUi/StockDetailState.
*/
interface MarketRepository {
    suspend fun getQuote(symbol: String): ApiResult<StockQuote>
}
