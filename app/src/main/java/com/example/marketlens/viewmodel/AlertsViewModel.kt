package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.AlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsViewModel(
    private val alertRepo: AlertRepository = AppContainer.alertRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AlertsState(isLoading = true))
    val state: StateFlow<AlertsState> = _state.asStateFlow()

    init { loadAlerts() }

    fun refresh() { loadAlerts() }

    fun deleteAlert(alertId: String) {
        viewModelScope.launch {
            alertRepo.deleteAlert(alertId)
            loadAlerts()
        }
    }

    private fun loadAlerts() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = alertRepo.getAlerts()) {
                is ApiResult.Success -> _state.value = AlertsState(isLoading = false, alerts = result.data)
                is ApiResult.Error   -> _state.value = AlertsState(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
