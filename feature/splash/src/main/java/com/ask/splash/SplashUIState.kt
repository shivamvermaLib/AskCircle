package com.ask.splash

sealed interface SplashUIState {
    data object Init : SplashUIState
    data object Loading : SplashUIState
    data class Success(val time: Long) : SplashUIState
    data class Error(val message: String) : SplashUIState
}