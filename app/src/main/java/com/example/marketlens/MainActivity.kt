package com.example.marketlens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.marketlens.navigation.AppScaffold
import com.example.marketlens.ui.theme.MarketLensTheme
import com.example.marketlens.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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