package com.example.marketlens.viewmodel

import com.example.marketlens.data.repository.UserSettings

data class SettingsState(
    val isLoading:    Boolean      = false,
    val isSaving:     Boolean      = false,
    val errorMessage: String?      = null,
    val saveSuccess:  Boolean      = false,
    val settings:     UserSettings = UserSettings()
)
