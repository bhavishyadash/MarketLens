package com.example.marketlens.data.model

data class PriceAlert(
    val id:          String,
    val symbol:      String,
    val targetPrice: Double,
    val direction:   AlertDirection,
    val createdAt:   Long,
    val isTriggered: Boolean = false
)

enum class AlertDirection { ABOVE, BELOW }
