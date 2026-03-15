package com.example.marketlens.viewmodel


data class AuthState(
    val mode:         AuthMode = AuthMode.LOGIN,
    val email:        String   = "",
    val password:     String   = "",
    val displayName:  String   = "",
    val isLoading:    Boolean  = false,
    val errorMessage: String?  = null,
    val isSuccess:    Boolean  = false
)

enum class AuthMode { LOGIN, SIGNUP }