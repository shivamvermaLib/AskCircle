package com.ask.app.ui.screens.home.dashboard

import androidx.lifecycle.viewModelScope
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.app.data.repository.UserRepository
import com.ask.app.data.repository.WidgetRepository
import com.ask.app.domain.GetWidgetsUseCase
import com.ask.app.ui.screens.utils.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val widgetRepository: WidgetRepository,
    getWidgetsUseCase: GetWidgetsUseCase,
    private val userRepository: UserRepository
) : BaseViewModel() {
    private val _widgetsFlow = getWidgetsUseCase()
    private val _errorFlow = MutableStateFlow<String?>(null)

    val uiStateFlow =
        combine(_widgetsFlow, _errorFlow) { widgets, error ->
            DashboardUiState(widgets, error)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DashboardUiState()
        )

    fun vote(widgetId: String, optionId: String) {
        safeApiCall({}, {
            widgetRepository.vote(widgetId, optionId, userRepository.getCurrentUserId())
        }, {
            _errorFlow.value = it
        })
    }
}

data class DashboardUiState(
    val widgets: List<WidgetWithOptionsAndVotesForTargetAudience> = emptyList(),
    val error: String? = null
)