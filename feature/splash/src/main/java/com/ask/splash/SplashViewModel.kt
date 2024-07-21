package com.ask.splash

import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.core.RemoteConfigRepository
import com.ask.widget.SyncUsersAndWidgetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    analyticsLogger: AnalyticsLogger,
    private val syncUsersAndWidgetsUseCase: SyncUsersAndWidgetsUseCase,
    private val remoteConfigRepository: RemoteConfigRepository
) :
    BaseViewModel(analyticsLogger) {

    private val _uiStateFlow = MutableStateFlow<SplashUIState>(SplashUIState.Init)
    val uiStateFlow = _uiStateFlow.asStateFlow()

    fun init(isConnected: Boolean, preloadImages: suspend (List<String>) -> Unit) {
        safeApiCall({
            _uiStateFlow.value = SplashUIState.Loading
        }, {
            syncUsersAndWidgetsUseCase.invoke(true, isConnected, preloadImages)
            _uiStateFlow.value = SplashUIState.Success(
                remoteConfigRepository.getSyncTimeInMinutes()
            )
        }, {
            _uiStateFlow.value = SplashUIState.Error(it)
        })
    }

}