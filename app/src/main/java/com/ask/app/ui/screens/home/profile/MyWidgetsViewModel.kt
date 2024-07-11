package com.ask.app.ui.screens.home.profile

import androidx.lifecycle.viewModelScope
import com.ask.app.analytics.AnalyticsLogger
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.app.data.repository.UserRepository
import com.ask.app.data.repository.WidgetRepository
import com.ask.app.domain.GetCurrentUserWidgetsUseCase
import com.ask.app.ui.screens.utils.BaseViewModel
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
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
    private val analyticsLogger: AnalyticsLogger,
) : BaseViewModel(analyticsLogger) {

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
    val widgets: List<WidgetWithOptionsAndVotesForTargetAudience> = emptyList(),
    val error: String? = null,
)