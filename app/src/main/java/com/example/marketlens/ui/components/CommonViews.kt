package com.example.marketlens.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.marketlens.ui.theme.Amber
import com.example.marketlens.ui.theme.MonoFamily
import com.example.marketlens.ui.theme.TerminalBorder

@Composable
fun LoadingView(modifier: Modifier = Modifier, label: String = "LOADING MARKET DATA") {
    val infinite = rememberInfiniteTransition(label = "loading-pulse")
    val alpha by infinite.animateFloatAsState(
        initialValue = 0.4f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading-alpha"
    )
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color       = Amber,
                strokeWidth = 2.dp,
                modifier    = Modifier.size(28.dp)
            )
            Text(
                text     = label.uppercase(),
                color    = Amber.copy(alpha = alpha),
                style    = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun ErrorView(
    message:   String,
    onRetry:   (() -> Unit)? = null,
    isOffline: Boolean = false,
    modifier:  Modifier = Modifier
) {
    val errorCode = if (isOffline) "ERR/NETWORK_UNREACHABLE" else "ERR/DATA_UNAVAILABLE"
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .padding(32.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(3.dp))
                .border(1.dp, TerminalBorder, RoundedCornerShape(3.dp))
                .padding(24.dp)
        ) {
            Icon(
                imageVector        = if (isOffline) Icons.Filled.CloudOff else Icons.Filled.ErrorOutline,
                contentDescription = null,
                modifier           = Modifier.size(36.dp),
                tint               = MaterialTheme.colorScheme.error
            )
            Text(
                text       = errorCode,
                fontFamily = MonoFamily,
                color      = MaterialTheme.colorScheme.error,
                style      = MaterialTheme.typography.labelLarge
            )
            Text(
                text      = if (isOffline) "No connection to the market feed." else message,
                color     = MaterialTheme.colorScheme.onSurface,
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            if (isOffline) {
                Text(
                    text      = "Reconnect and retry to resume streaming.",
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    style     = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
            onRetry?.let {
                OutlinedButton(
                    onClick = it,
                    border  = BorderStroke(1.dp, Amber),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = Amber)
                ) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("RETRY", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun FadeInContent(
    visible:  Boolean,
    modifier: Modifier = Modifier,
    content:  @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(animationSpec = tween(300)),
        exit    = fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        content()
    }
}
