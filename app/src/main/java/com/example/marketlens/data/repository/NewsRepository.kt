package com.example.marketlens.data.repository

import com.example.marketlens.data.model.NewsArticle
import com.example.marketlens.data.network.ApiResult

interface NewsRepository {
    suspend fun getMarketNews(): ApiResult<List<NewsArticle>>
    suspend fun getStockNews(symbol: String): ApiResult<List<NewsArticle>>
}