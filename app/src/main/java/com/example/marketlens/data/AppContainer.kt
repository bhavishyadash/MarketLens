package com.example.marketlens.data

import android.content.Context
import com.example.marketlens.data.local.AppDatabase
import com.example.marketlens.data.network.NetworkModule
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.NewsRepository
import com.example.marketlens.data.repository.RealMarketRepository
import com.example.marketlens.data.repository.RealNewsRepository

object AppContainer {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(appContext)
    }

    val repository: MarketRepository by lazy {
        RealMarketRepository(NetworkModule.marketApi)
    }

    val newsRepository: NewsRepository by lazy {
        RealNewsRepository(api = NetworkModule.marketApi, dao = database.newsDao())
    }
}