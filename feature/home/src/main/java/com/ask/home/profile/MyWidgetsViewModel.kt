package com.ask.home.profile

import androidx.lifecycle.viewModelScope
import com.ask.widget.GetCurrentUserWidgetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MyWidgetsViewModel @Inject constructor(
    getCurrentUserWidgetsUseCase: GetCurrentUserWidgetsUseCase,
    private val userRepository: com.ask.user.UserRepository,
    private val widgetRepository: com.ask.widget.WidgetRepository,
    private val analyticsLogger: com.ask.analytics.AnalyticsLogger,
) : com.ask.common.BaseViewModel(analyticsLogger) {

    private val userWidgets = getCurrentUserWidgetsUseCase()
    private val _errorFlow = MutableStateFlow<String?>(null)

    val uiStateFlow = combine(userWidgets, _errorFlow) { widgets, error ->
        MyWidgetsUiState(widgets, error)
    }.catch {
        it.printStackTrace()
        _errorFlow.value = it.message
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyWidgetsUiState())

    fun setError(error: String?) {
        _errorFlow.value = error
    }

    fun vote(widgetId: String, optionId: String, screenName: String) {
        safeApiCall({
            analyticsLogger.voteWidgetEvent(
                widgetId,
                optionId,
                userRepository.getCurrentUserId(),
                screenName
            )
        }, {
            widgetRepository.vote(widgetId, optionId, userRepository.getCurrentUserId())
                .also {
                    analyticsLogger.votedWidgetEvent(
                        widgetId,
                        optionId,
                        userRepository.getCurrentUserId(),
                        screenName
                    )
                }
        }, {
            _errorFlow.value = it
        })
    }
}

data class MyWidgetsUiState(
    val widgets: List<com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience> = emptyList(),
    val error: String? = null,
)