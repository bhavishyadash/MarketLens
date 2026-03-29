package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.repository.SignalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignalsViewModel(
    private val signalRepo: SignalRepository = AppContainer.signalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignalsState(isLoading = true))
    val state: StateFlow<SignalsState> = _state.asStateFlow()

    init { loadSignals() }

    fun refresh() { loadSignals() }

    fun deleteSignal(signalId: String) {
        viewModelScope.launch {
            signalRepo.deleteSignal(signalId)
            loadSignals()
        }
    }

    fun markRead(signalId: String) {
        viewModelScope.launch {
            signalRepo.markRead(signalId)
            loadSignals()
        }
    }

    private fun loadSignals() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = signalRepo.getSignals()) {
                is ApiResult.Success -> _state.value = SignalsState(isLoading = false, signals = result.data)
                is ApiResult.Error   -> _state.value = SignalsState(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
