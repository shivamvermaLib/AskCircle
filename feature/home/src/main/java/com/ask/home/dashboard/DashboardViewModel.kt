package com.ask.home.dashboard

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.widget.Filter
import com.ask.widget.GetWidgetsUseCase
import com.ask.widget.UpdateVoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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
    private val _filterFlow = MutableStateFlow(Filter.Latest)
    private val _lastVotedEmptyOptionsFlow = MutableStateFlow<List<String>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val widgetsFlow =
        combine(_filterFlow, _lastVotedEmptyOptionsFlow) { filter, lastVotedEmptyOptions ->
            filter to lastVotedEmptyOptions
        }.flatMapMerge { getWidgetsUseCase(it.first, it.second) }.cachedIn(viewModelScope)

    fun setLastVotedEmptyOptions(list: List<String>) {
        _lastVotedEmptyOptionsFlow.value = list
    }

    fun setFilterType(filter: Filter) {
        _filterFlow.value = filter
        analyticsLogger.widgetFilterTypeEvent(filter.name)
    }

    fun vote(widgetId: String, optionId: String, screenName: String) {
        safeApiCall({}, {
            updateVoteUseCase(widgetId, optionId, screenName)
        }, {
//            _errorFlow.value = it
        })
    }
}

/*
data class DashboardUiState(
    val error: String? = null
)
*/
