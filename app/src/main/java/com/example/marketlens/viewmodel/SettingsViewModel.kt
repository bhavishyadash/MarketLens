package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.SettingsRepository
import com.example.marketlens.data.repository.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepo: SettingsRepository = AppContainer.settingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState(isLoading = true))
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init { loadSettings() }

    fun onSignalsEnabledChanged(value: Boolean) {
        _state.value = _state.value.copy(settings = _state.value.settings.copy(signalsEnabled = value), saveSuccess = false)
    }

    fun onHighSignalsOnlyChanged(value: Boolean) {
        _state.value = _state.value.copy(settings = _state.value.settings.copy(highSignalsOnly = value), saveSuccess = false)
    }

    fun onNotifyOnSignalChanged(value: Boolean) {
        _state.value = _state.value.copy(settings = _state.value.settings.copy(notifyOnSignal = value), saveSuccess = false)
    }

    fun onWatchlistSectorOnlyChanged(value: Boolean) {
        _state.value = _state.value.copy(settings = _state.value.settings.copy(watchlistSectorOnly = value), saveSuccess = false)
    }

    fun save() {
        _state.value = _state.value.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            when (settingsRepo.saveSettings(_state.value.settings)) {
                is ApiResult.Success -> _state.value = _state.value.copy(isSaving = false, saveSuccess = true)
                is ApiResult.Error   -> _state.value = _state.value.copy(isSaving = false, errorMessage = "Could not save settings")
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            when (val result = settingsRepo.getSettings()) {
                is ApiResult.Success -> _state.value = SettingsState(isLoading = false, settings = result.data)
                is ApiResult.Error   -> _state.value = SettingsState(isLoading = false, settings = UserSettings())
            }
        }
    }
}
