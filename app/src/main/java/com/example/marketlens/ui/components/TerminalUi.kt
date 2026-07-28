package com.example.marketlens.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.marketlens.ui.theme.Amber
import com.example.marketlens.ui.theme.AmberSoft
import com.example.marketlens.ui.theme.LiveDot
import com.example.marketlens.ui.theme.TerminalBorder
import com.example.marketlens.ui.theme.TerminalDim
import com.example.marketlens.ui.theme.TerminalSurface

/**
 * Uppercase tracked micro-label — the persistent chrome of a terminal.
 * Use for card headers, sections, and any tiny "MARKET / GAINERS" style tags.
 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = Amber,
    textAlign: TextAlign? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier
                .size(width = 2.dp, height = 10.dp)
                .background(accent)
        )
        Text(
            text  = text.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            textAlign = textAlign
        )
    }
}

/**
 * Terminal panel — sharp corners, hairline top-border in amber to mimic the
 * "channel" bars on a Bloomberg screen. Use in place of Material Card when you
 * want the on-brand chrome.
 */
@Composable
fun TerminalCard(
    modifier: Modifier = Modifier,
    accent: Color = Amber,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = TerminalSurface,
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(1.dp, TerminalBorder)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(accent)
            )
            Column(content = content)
        }
    }
}

/**
 * Small pill with a pulsing dot — "LIVE" indicator on the dashboard.
 */
@Composable
fun LivePill(
    text: String = "LIVE",
    color: Color = LiveDot,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "live-pulse")
    val alpha by infinite.animateFloatAsState(
        initialValue = 0.35f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live-alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(TerminalDim)
            .border(1.dp, TerminalBorder, RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        Text(
            text  = text.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * Static status pill (e.g. sector tag, filter chip in read-only state).
 */
@Composable
fun StatusPill(
    text: String,
    color: Color = Amber,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text  = text.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/** Vertical hairline column used inside panels for dense number pairs. */
@Composable
fun VDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(TerminalBorder)
    )
}

@Suppress("unused")
val AmberWashColor = AmberSoft
