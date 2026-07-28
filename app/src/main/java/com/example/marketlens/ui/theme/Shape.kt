package com.example.marketlens.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Sharp corners — terminal panel feel. Nothing above 6dp.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small      = RoundedCornerShape(3.dp),
    medium     = RoundedCornerShape(4.dp),
    large      = RoundedCornerShape(6.dp),
    extraLarge = RoundedCornerShape(6.dp),
)
