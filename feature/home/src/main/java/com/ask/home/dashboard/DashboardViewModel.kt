package com.ask.home.dashboard

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.work.WorkInfo
import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.widget.BookmarkWidgetUseCase
import com.ask.widget.Filter
import com.ask.widget.GetWidgetsUseCase
import com.ask.widget.StartStopWidgetAcceptingVoteUseCase
import com.ask.widget.UpdateVoteUseCase
import com.ask.workmanager.STATUS
import com.ask.workmanager.WorkerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val updateVoteUseCase: UpdateVoteUseCase,
    getWidgetsUseCase: GetWidgetsUseCase,
    private val bookmarkWidgetUseCase: BookmarkWidgetUseCase,
    private val startStopWidgetAcceptingVoteUseCase: StartStopWidgetAcceptingVoteUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {
    private val _filterFlow = MutableStateFlow(Filter.Latest)
    private val _lastVotedEmptyOptionsFlow = MutableStateFlow<List<String>>(emptyList())
    private val _triggerRefresh = MutableStateFlow(System.currentTimeMillis())
    private val _filterWithLastVotedEmptyOptionsFlow =
        combine(
            _filterFlow,
            _lastVotedEmptyOptionsFlow,
            _triggerRefresh
        ) { filter, lastVotedEmptyOptions, triggerRefresh ->
            Triple(filter, lastVotedEmptyOptions, triggerRefresh)
        }
    private val _lastVoteWidgetId = MutableStateFlow<String?>(null)
    val widgetsFlow = getWidgetsUseCase(_filterWithLastVotedEmptyOptionsFlow)
        .map { pagingData ->
            pagingData.map {
                it.apply {
                    it.isLastVotedWidget = _lastVoteWidgetId.value == it.widget.id
                }
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), PagingData.empty())

    fun setLastVotedEmptyOptions(list: List<String>) {
        _lastVotedEmptyOptionsFlow.value = list
    }

    fun setFilterType(filter: Filter) {
        _filterFlow.value = filter
        analyticsLogger.widgetFilterTypeEvent(filter.name)
    }

    fun vote(widgetId: String, optionId: String, screenName: String) {
        safeApiCall({}, {
            _lastVoteWidgetId.value = widgetId
            updateVoteUseCase(widgetId, optionId, screenName)
        }, {
//            _errorFlow.value = it
        })
    }

    fun onBookmarkClick(widgetId: String) {
        safeApiCall({}, {
            bookmarkWidgetUseCase.invoke(widgetId)
        }, {
            println("Error: $it")
        })
    }

    fun onStopVoteClick(widgetId: String) {
        safeApiCall({}, {
            startStopWidgetAcceptingVoteUseCase.invoke(widgetId, false)
        }, {})
    }

    fun onStartVoteClick(widgetId: String) {
        safeApiCall({}, {
            startStopWidgetAcceptingVoteUseCase.invoke(widgetId, true)
        }, {})
    }

    fun setWorkerFlow(workerFlow: Flow<List<WorkInfo>>) {
        viewModelScope.launch {
            workerFlow.collect { workInfos ->
                val list = workInfos.filter { it.state != WorkInfo.State.SUCCEEDED }
                    .mapNotNull { it.progress.getString(STATUS) }.map { WorkerStatus.valueOf(it) }
                if (list.any { it == WorkerStatus.Loading }.not()) {
                    _triggerRefresh.value = System.currentTimeMillis()
                }
            }
        }
    }
}