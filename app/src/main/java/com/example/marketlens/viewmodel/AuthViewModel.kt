package com.example.marketlens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()


    val isAlreadySignedIn: Boolean
        get() = auth.currentUser != null

    val currentUserName: String
        get() = auth.currentUser?.displayName
            ?: auth.currentUser?.email?.substringBefore("@")
            ?: "User"


    fun onEmailChanged(value: String) {
        _state.value = _state.value.copy(email = value.trim(), errorMessage = null)
    }

    fun onPasswordChanged(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun onDisplayNameChanged(value: String) {
        _state.value = _state.value.copy(displayName = value, errorMessage = null)
    }

    fun onToggleMode() {
        val newMode = if (_state.value.mode == AuthMode.LOGIN) AuthMode.SIGNUP else AuthMode.LOGIN
        _state.value = _state.value.copy(mode = newMode, errorMessage = null)
    }


    fun onSubmit() {
        val state = _state.value

        if (state.email.isBlank()) {
            _state.value = state.copy(errorMessage = "Email cannot be empty")
            return
        }
        if (state.password.length < 6) {
            _state.value = state.copy(errorMessage = "Password must be at least 6 characters")
            return
        }
        if (state.mode == AuthMode.SIGNUP && state.displayName.isBlank()) {
            _state.value = state.copy(errorMessage = "Please enter your name")
            return
        }

        _state.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                if (_state.value.mode == AuthMode.LOGIN) {
                    signIn(state.email, state.password)
                } else {
                    signUp(state.email, state.password, state.displayName)
                }
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("no user record") == true      -> "No account found with this email"
                    e.message?.contains("password is invalid") == true  -> "Incorrect password"
                    e.message?.contains("email address is already") == true -> "An account with this email already exists"
                    e.message?.contains("badly formatted") == true      -> "Please enter a valid email address"
                    else -> e.message ?: "Something went wrong. Please try again."
                }
                _state.value = _state.value.copy(isLoading = false, errorMessage = message)
            }
        }
    }

    private suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
        _state.value = _state.value.copy(isLoading = false, isSuccess = true)
    }

    private suspend fun signUp(email: String, password: String, displayName: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        // Save display name to Firebase user profile
        result.user?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
        )?.await()
        _state.value = _state.value.copy(isLoading = false, isSuccess = true)
    }


    fun signOut() {
        auth.signOut()
        // Reset state so auth screen starts fresh
        _state.value = AuthState()
    }
}