package com.example.marketlens.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.marketlens.ui.theme.PriceUp
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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(Modifier.height(24.dp))

            Text(
                text       = "MarketLens",
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold,
                color      = PriceUp
            )
            Text(
                text      = "Real-time market insights",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text       = if (state.mode == AuthMode.LOGIN) "Welcome back" else "Create account",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (state.mode == AuthMode.SIGNUP) {
                OutlinedTextField(
                    value         = state.displayName,
                    onValueChange = viewModel::onDisplayNameChanged,
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Your Name") },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

            OutlinedTextField(
                value         = state.email,
                onValueChange = viewModel::onEmailChanged,
                modifier      = Modifier.fillMaxWidth(),
                label         = { Text("Email") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value         = state.password,
                onValueChange = viewModel::onPasswordChanged,
                modifier      = Modifier.fillMaxWidth(),
                label         = { Text("Password") },
                singleLine    = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.VisibilityOff
                            else
                                Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            state.errorMessage?.let { error ->
                Text(
                    text  = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick  = { viewModel.onSubmit() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled  = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text     = if (state.mode == AuthMode.LOGIN) "Sign In" else "Create Account",
                        fontSize = 16.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text  = if (state.mode == AuthMode.LOGIN) "Don't have an account? "
                    else "Already have an account? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick      = { viewModel.onToggleMode() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text  = if (state.mode == AuthMode.LOGIN) "Sign Up" else "Sign In",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}