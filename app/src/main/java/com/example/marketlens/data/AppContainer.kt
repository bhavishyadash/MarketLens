package com.example.marketlens.data

import com.example.marketlens.data.firebase.FirebaseModule
import com.example.marketlens.data.network.NetworkModule
import com.example.marketlens.data.repository.AlertRepository
import com.example.marketlens.data.repository.FirestoreAlertRepository
import com.example.marketlens.data.repository.FirestoreNewsRepository
import com.example.marketlens.data.repository.FirestoreWatchlistRepository
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.NewsRepository
import com.example.marketlens.data.repository.RealMarketRepository
import com.example.marketlens.data.repository.WatchlistRepository

object AppContainer {

    val repository: MarketRepository by lazy {
        RealMarketRepository(api = NetworkModule.marketApi, yahoo = NetworkModule.yahooFinanceApi)
    }

    val newsRepository: NewsRepository by lazy {
        FirestoreNewsRepository(api = NetworkModule.marketApi, db = FirebaseModule.firestore)
    }

    val watchlistRepository: WatchlistRepository by lazy {
        FirestoreWatchlistRepository(db = FirebaseModule.firestore)
    }

    val alertRepository: AlertRepository by lazy {
        FirestoreAlertRepository(db = FirebaseModule.firestore)
    }
}
