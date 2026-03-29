package com.example.marketlens.viewmodel

import com.example.marketlens.data.model.PriceAlert

data class AlertsState(
    val isLoading:    Boolean          = false,
    val errorMessage: String?          = null,
    val alerts:       List<PriceAlert> = emptyList()
)
