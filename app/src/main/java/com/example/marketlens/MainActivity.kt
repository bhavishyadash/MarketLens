package com.example.marketlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.marketlens.navigation.AppScaffold
import com.example.marketlens.ui.theme.MarketLensTheme
import com.example.marketlens.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {


    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MarketLensTheme {
                val navController = rememberNavController()
                AppScaffold(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}