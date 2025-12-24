package com.example.marketlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.marketlens.ui.dashboard.DashboardScreen
import com.example.marketlens.ui.theme.MarketLensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MarketLensTheme {
                Surface(modifier = Modifier) {
                    DashboardScreen()
                }
            }
        }
    }
}