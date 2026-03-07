package com.example.marketlens.data.network

/*
    Bug fixed: package was "com.example.marketlens.network"
    but the file lives in  data/network/ folder.
    Package must always mirror the folder path or Android Studio
    will show "Package directive doesn't match file location" warnings
    and refactoring tools will break.
*/
sealed class ApiResult<out T> {
    // Success wraps whatever data type the caller expects
    data class Success<T>(val data: T) : ApiResult<T>()

    // Error carries a human-readable message + the original exception (optional)
    data class Error(val message: String, val throwable: Throwable? = null) : ApiResult<Nothing>()
}
