package com.example.marketlens.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.ui.components.LoadingView
import com.example.marketlens.ui.components.SectionLabel
import com.example.marketlens.ui.components.TerminalCard
import com.example.marketlens.ui.theme.Amber
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.ui.theme.TerminalBlack
import com.example.marketlens.ui.theme.TerminalBorder
import com.example.marketlens.ui.theme.TextSecondary
import com.example.marketlens.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingView(label = "Loading preferences")
        else -> Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            SectionLabel("System // Preferences")
            Text("Settings", style = MaterialTheme.typography.displaySmall)

            TerminalCard(accent = Amber) {
                Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(4.dp)) {
                    SectionLabel("News // Signals")
                    Spacer(Modifier.height(6.dp))
                    Text("News Signals", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text  = "Signals are informational only — not financial advice.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = TerminalBorder)
                    Spacer(Modifier.height(4.dp))

                    SettingToggle(
                        label       = "Enable signals",
                        description = "Scan news for market signals every 30 minutes",
                        checked     = state.settings.signalsEnabled,
                        onChecked   = viewModel::onSignalsEnabledChanged
                    )
                    HorizontalDivider(color = TerminalBorder)
                    SettingToggle(
                        label       = "High signals only",
                        description = "Only show HIGH strength signals, ignore low/medium",
                        checked     = state.settings.highSignalsOnly,
                        onChecked   = viewModel::onHighSignalsOnlyChanged,
                        enabled     = state.settings.signalsEnabled
                    )
                    HorizontalDivider(color = TerminalBorder)
                    SettingToggle(
                        label       = "Notify on signal",
                        description = "Send push notification when a new signal is detected",
                        checked     = state.settings.notifyOnSignal,
                        onChecked   = viewModel::onNotifyOnSignalChanged,
                        enabled     = state.settings.signalsEnabled
                    )
                    HorizontalDivider(color = TerminalBorder)
                    SettingToggle(
                        label       = "Watchlist sector only",
                        description = "Only show signals that affect stocks in your watchlist",
                        checked     = state.settings.watchlistSectorOnly,
                        onChecked   = viewModel::onWatchlistSectorOnlyChanged,
                        enabled     = state.settings.signalsEnabled
                    )
                }
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (state.saveSuccess) {
                Text("✓ SETTINGS SAVED", color = PriceUp, style = MaterialTheme.typography.labelLarge)
            }

            Button(
                onClick  = { viewModel.save() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled  = !state.isSaving,
                shape    = RoundedCornerShape(3.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Amber,
                    contentColor   = TerminalBlack
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = TerminalBlack)
                } else {
                    Text("▶ SAVE SETTINGS", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun SettingToggle(
    label:       String,
    description: String,
    checked:     Boolean,
    onChecked:   (Boolean) -> Unit,
    enabled:     Boolean = true
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else TextSecondary
            )
            Text(description, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor  = TerminalBlack,
                checkedTrackColor  = Amber,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                uncheckedBorderColor = TerminalBorder
            )
        )
    }
}
