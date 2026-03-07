package com.example.marketlens.data

import com.example.marketlens.data.firebase.FirebaseModule
import com.example.marketlens.data.network.NetworkModule
import com.example.marketlens.data.repository.FirestoreNewsRepository
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.NewsRepository
import com.example.marketlens.data.repository.RealMarketRepository


object AppContainer {

    val repository: MarketRepository by lazy {
        RealMarketRepository(NetworkModule.marketApi)
    }

    val newsRepository: NewsRepository by lazy {
        FirestoreNewsRepository(
            api = NetworkModule.marketApi,
            db  = FirebaseModule.firestore
        )
    }
}