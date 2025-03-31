package com.example.weatherapp.data.models

sealed class Results<out T> {
    data object Loading : Results<Nothing>()
    data class Success<T>(val data: T?) : Results<T>()
    data class Failure(val error: Throwable) : Results<Nothing>()
}

