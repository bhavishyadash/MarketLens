package com.example.marketlens.data

import com.example.marketlens.data.firebase.FirebaseModule
import com.example.marketlens.data.network.NetworkModule
import com.example.marketlens.data.repository.AlertRepository
import com.example.marketlens.data.repository.FirestoreAlertRepository
import com.example.marketlens.data.repository.FirestoreNewsRepository
import com.example.marketlens.data.repository.FirestorePortfolioRepository
import com.example.marketlens.data.repository.FirestoreSettingsRepository
import com.example.marketlens.data.repository.FirestoreSignalRepository
import com.example.marketlens.data.repository.FirestoreWatchlistRepository
import com.example.marketlens.data.repository.MarketRepository
import com.example.marketlens.data.repository.NewsRepository
import com.example.marketlens.data.repository.PortfolioRepository
import com.example.marketlens.data.repository.RealMarketRepository
import com.example.marketlens.data.repository.SettingsRepository
import com.example.marketlens.data.repository.SignalRepository
import com.example.marketlens.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppContainer {

    private val _watchlistChanged = MutableSharedFlow<Unit>(replay = 0)
    val watchlistChanged: SharedFlow<Unit> = _watchlistChanged.asSharedFlow()

    suspend fun notifyWatchlistChanged() {
        _watchlistChanged.emit(Unit)
    }

    val repository: MarketRepository by lazy {
        RealMarketRepository(yahoo = NetworkModule.yahooFinanceApi)
    }

    val newsRepository: NewsRepository by lazy {
        FirestoreNewsRepository(
            yahoo = NetworkModule.yahooFinanceApi,
            db    = FirebaseModule.firestore
        )
    }

    val watchlistRepository: WatchlistRepository by lazy {
        FirestoreWatchlistRepository(db = FirebaseModule.firestore)
    }

    val alertRepository: AlertRepository by lazy {
        FirestoreAlertRepository(db = FirebaseModule.firestore)
    }

    val signalRepository: SignalRepository by lazy {
        FirestoreSignalRepository(db = FirebaseModule.firestore)
    }

    val settingsRepository: SettingsRepository by lazy {
        FirestoreSettingsRepository(db = FirebaseModule.firestore)
    }

    val portfolioRepository: PortfolioRepository by lazy {
        FirestorePortfolioRepository(db = FirebaseModule.firestore)
    }
}
