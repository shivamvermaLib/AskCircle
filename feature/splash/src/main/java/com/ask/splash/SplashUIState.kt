package com.ask.splash

import android.content.Context

sealed interface SplashUIState {
    data object Init : SplashUIState
    data object NotLoggedIn : SplashUIState
    data class Loading(val progress: Float) : SplashUIState
    data class Success(val time: Long) : SplashUIState
    data class Error(val message: String) : SplashUIState
}

sealed class SplashUiEvent(
    open val isConnected: Boolean,
    open val preloadImages: suspend (List<String>) -> Unit,
) {
    data class GoogleLoginUiEvent(
        val context: Context,
        override val isConnected: Boolean,
        override val preloadImages: suspend (List<String>) -> Unit,
    ) : SplashUiEvent(isConnected, preloadImages)

    data class AnonymousLoginUiEvent(
        override val isConnected: Boolean,
        override val preloadImages: suspend (List<String>) -> Unit,
    ) : SplashUiEvent(isConnected, preloadImages)

    data class InitEvent(
        override val isConnected: Boolean,
        override val preloadImages: suspend (List<String>) -> Unit,
    ) : SplashUiEvent(isConnected, preloadImages)

}