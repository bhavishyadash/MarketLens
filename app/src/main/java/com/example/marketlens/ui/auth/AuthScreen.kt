package com.example.marketlens.ui.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketlens.ui.theme.Amber
import com.example.marketlens.ui.theme.AmberSoft
import com.example.marketlens.ui.theme.Cyan
import com.example.marketlens.ui.theme.MonoFamily
import com.example.marketlens.ui.theme.TerminalBlack
import com.example.marketlens.ui.theme.TerminalBorder
import com.example.marketlens.ui.theme.TerminalSurface
import com.example.marketlens.ui.theme.TextSecondary
import com.example.marketlens.ui.theme.TextTertiary
import com.example.marketlens.viewmodel.AuthMode
import com.example.marketlens.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onAuthSuccess()
    }

    var passwordVisible by remember { mutableStateOf(false) }

    val cursor = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by cursor.animateFloatAsState(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label         = "cursor-alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Meta line — ticker/coordinates flavor
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = "SYS/AUTH.v1",
                    fontFamily = MonoFamily,
                    color      = TextTertiary,
                    style      = MaterialTheme.typography.labelMedium
                )
                Text(
                    text       = "NYSE // NASDAQ // 24H",
                    fontFamily = MonoFamily,
                    color      = TextTertiary,
                    style      = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            // Big amber wordmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text       = "MARKET",
                    fontSize   = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-1).sp
                )
                Text(
                    text       = "LENS",
                    fontSize   = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Amber,
                    letterSpacing = (-1).sp
                )
                Text(
                    text       = "▊",
                    fontSize   = 40.sp,
                    color      = Cyan.copy(alpha = cursorAlpha)
                )
            }
            Text(
                text       = "// REAL-TIME MARKET INSIGHTS",
                fontFamily = MonoFamily,
                color      = TextSecondary,
                style      = MaterialTheme.typography.labelLarge,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // Auth panel — terminal card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurface, RoundedCornerShape(3.dp))
                    .border(1.dp, TerminalBorder, RoundedCornerShape(3.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text  = (if (state.mode == AuthMode.LOGIN) "AUTHENTICATE" else "PROVISION ACCOUNT"),
                        color = Amber,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text       = if (state.mode == AuthMode.LOGIN) "01/02" else "02/02",
                        fontFamily = MonoFamily,
                        color      = TextTertiary,
                        style      = MaterialTheme.typography.labelMedium
                    )
                }

                if (state.mode == AuthMode.SIGNUP) {
                    OutlinedTextField(
                        value           = state.displayName,
                        onValueChange   = viewModel::onDisplayNameChanged,
                        modifier        = Modifier.fillMaxWidth(),
                        label           = { Text("Display name") },
                        singleLine      = true,
                        shape           = RoundedCornerShape(3.dp),
                        colors          = fieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                }

                OutlinedTextField(
                    value           = state.email,
                    onValueChange   = viewModel::onEmailChanged,
                    modifier        = Modifier.fillMaxWidth(),
                    label           = { Text("Email") },
                    singleLine      = true,
                    shape           = RoundedCornerShape(3.dp),
                    colors          = fieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value                = state.password,
                    onValueChange        = viewModel::onPasswordChanged,
                    modifier             = Modifier.fillMaxWidth(),
                    label                = { Text("Password") },
                    singleLine           = true,
                    shape                = RoundedCornerShape(3.dp),
                    colors               = fieldColors(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = TextSecondary
                            )
                        }
                    }
                )

                state.errorMessage?.let { error ->
                    Text(
                        text       = "! $error",
                        fontFamily = MonoFamily,
                        color      = MaterialTheme.colorScheme.error,
                        style      = MaterialTheme.typography.bodySmall,
                        modifier   = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick   = { viewModel.onSubmit() },
                    modifier  = Modifier.fillMaxWidth().height(48.dp),
                    enabled   = !state.isLoading,
                    shape     = RoundedCornerShape(3.dp),
                    colors    = ButtonDefaults.buttonColors(
                        containerColor = Amber,
                        contentColor   = TerminalBlack,
                        disabledContainerColor = AmberSoft,
                        disabledContentColor   = TerminalBlack.copy(alpha = 0.5f)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color       = TerminalBlack
                        )
                    } else {
                        Text(
                            text     = (if (state.mode == AuthMode.LOGIN) "▶ SIGN IN" else "▶ CREATE ACCOUNT"),
                            style    = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text  = if (state.mode == AuthMode.LOGIN) "No account? " else "Already registered? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    TextButton(
                        onClick        = { viewModel.onToggleMode() },
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text  = if (state.mode == AuthMode.LOGIN) "Sign up →" else "Sign in →",
                            style = MaterialTheme.typography.bodySmall,
                            color = Amber
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Legal / footer
            Text(
                text       = "PROTECTED BY FIREBASE // TLS/1.3",
                fontFamily = MonoFamily,
                color      = TextTertiary,
                style      = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Amber,
    unfocusedBorderColor    = TerminalBorder,
    focusedLabelColor       = Amber,
    unfocusedLabelColor     = TextSecondary,
    cursorColor             = Amber,
    focusedTextColor        = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor      = MaterialTheme.colorScheme.onSurface
)
