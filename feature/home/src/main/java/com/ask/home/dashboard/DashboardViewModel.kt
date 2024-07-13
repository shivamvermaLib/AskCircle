package com.ask.home.dashboard

import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.user.UserRepository
import com.ask.widget.GetWidgetsUseCase
import com.ask.widget.WidgetRepository
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
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
    private val userRepository: UserRepository,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {
    private val _widgetsFlow = getWidgetsUseCase()
    private val _filterTypeFlow = MutableStateFlow(FilterType.Latest)
    private val _errorFlow = MutableStateFlow<String?>(null)

    val uiStateFlow =
        combine(_widgetsFlow, _errorFlow, _filterTypeFlow) { widgets, error, filterType ->
            DashboardUiState(when (filterType) {
                FilterType.Latest -> widgets
                FilterType.Trending -> widgets.sortedByDescending { widgetWithOptionsAndVotesForTargetAudience ->
                    widgetWithOptionsAndVotesForTargetAudience.options.sumOf { it.totalVotes }
                }
            }, error)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DashboardUiState()
        )

    fun setFilterType(filterType: FilterType) {
        _filterTypeFlow.value = filterType
        analyticsLogger.widgetFilterTypeEvent(filterType.name)
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

data class DashboardUiState(
    val widgets: List<WidgetWithOptionsAndVotesForTargetAudience> = emptyList(),
    val error: String? = null
)

enum class FilterType { Latest, Trending }