package com.example.marketlens.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(
    message:   String,
    onRetry:   (() -> Unit)? = null,
    isOffline: Boolean = false,
    modifier:  Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector        = if (isOffline) Icons.Filled.CloudOff else Icons.Filled.ErrorOutline,
                contentDescription = null,
                modifier           = Modifier.size(48.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text      = if (isOffline) "No internet connection" else message,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            if (isOffline) {
                Text(
                    text      = "Check your connection and try again",
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    style     = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
            onRetry?.let {
                Button(onClick = it) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Retry")
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
