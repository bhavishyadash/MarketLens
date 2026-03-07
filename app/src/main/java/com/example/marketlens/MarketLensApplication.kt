package com.example.marketlens

import android.app.Application
import com.example.marketlens.data.AppContainer

class MarketLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}