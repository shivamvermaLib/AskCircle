package com.ask.splash

import android.content.Context
import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.common.googleLogin
import com.ask.core.RemoteConfigRepository
import com.ask.user.AnonymousLoginUseCase
import com.ask.user.CheckCurrentUserUseCase
import com.ask.user.GoogleLoginUseCase
import com.ask.widget.SyncUsersAndWidgetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    analyticsLogger: AnalyticsLogger,
    private val syncUsersAndWidgetsUseCase: SyncUsersAndWidgetsUseCase,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val checkCurrentUserUseCase: CheckCurrentUserUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val anonymousLoginUseCase: AnonymousLoginUseCase,
) : BaseViewModel(analyticsLogger) {

    private val _uiStateFlow = MutableStateFlow<SplashUIState>(SplashUIState.Init)
    val uiStateFlow = _uiStateFlow.asStateFlow()

    fun onEvent(splashUiEvent: SplashUiEvent) {
        safeApiCall({
            _uiStateFlow.value = SplashUIState.Loading(0.1f)
        }, {
            val user = when (splashUiEvent) {
                is SplashUiEvent.GoogleLoginUiEvent -> {
                    googleLogin(splashUiEvent.context)?.let { credential ->
                        googleLoginUseCase(credential.idToken,true)
                    }
                }

                is SplashUiEvent.AnonymousLoginUiEvent -> anonymousLoginUseCase()
                is SplashUiEvent.InitEvent -> checkCurrentUserUseCase()
            }
            if (user == null) {
                _uiStateFlow.value = SplashUIState.NotLoggedIn
            } else {
                syncData(splashUiEvent)
            }
        }, {
            _uiStateFlow.value = SplashUIState.Error(it)
        })
    }

    private suspend fun syncData(splashUiEvent: SplashUiEvent) {
        syncUsersAndWidgetsUseCase.invoke(
            true, splashUiEvent.isConnected, splashUiEvent.preloadImages,
            { _uiStateFlow.value = SplashUIState.Loading(it) }, {})
        _uiStateFlow.value = SplashUIState.Success(
            remoteConfigRepository.getSyncTimeInMinutes()
        )
    }
}