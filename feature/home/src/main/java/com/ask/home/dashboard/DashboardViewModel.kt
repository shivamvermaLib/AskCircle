package com.ask.home.dashboard

import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.widget.FilterType
import com.ask.widget.GetWidgetsUseCase
import com.ask.widget.UpdateVoteUseCase
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val updateVoteUseCase: UpdateVoteUseCase,
    getWidgetsUseCase: GetWidgetsUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {
    private val _filterTypeFlow = MutableStateFlow(FilterType.Latest)
    private val _errorFlow = MutableStateFlow<String?>(null)


    @OptIn(ExperimentalCoroutinesApi::class)
    val uiStateFlow =
        combine(
            _filterTypeFlow.flatMapMerge { getWidgetsUseCase(it) },
            _errorFlow
        ) { widgets, error ->
            DashboardUiState(widgets, error)
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
        safeApiCall({}, {
            updateVoteUseCase(widgetId, optionId, screenName)
        }, {
            _errorFlow.value = it
        })
    }
}

data class DashboardUiState(
    val widgets: List<WidgetWithOptionsAndVotesForTargetAudience> = emptyList(),
    val error: String? = null
)

