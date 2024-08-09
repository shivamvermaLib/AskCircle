package com.ask.home

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val updateVoteUseCase: UpdateVoteUseCase,
    getWidgetsUseCase: GetWidgetsUseCase,
    private val bookmarkWidgetUseCase: BookmarkWidgetUseCase,
    private val startStopWidgetAcceptingVoteUseCase: StartStopWidgetAcceptingVoteUseCase,
    private val analyticsLogger: AnalyticsLogger
) :
    BaseViewModel(analyticsLogger) {

    private val _workerStatusFlow = MutableStateFlow(WorkerStatus.None)
    private val _errorFlow = MutableStateFlow<String?>(null)
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
        FirebaseCrashlytics.getInstance().recordException(exception)
    }

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


    val widgetsFlow = getWidgetsUseCase(_filterWithLastVotedEmptyOptionsFlow)
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), PagingData.empty())


    val uiStateFlow = combine(_workerStatusFlow, _errorFlow) { workerStatus, error ->
        HomeUiState(workerStatus, error)
    }.catch {
        it.printStackTrace()
        FirebaseCrashlytics.getInstance().recordException(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun setWorkerFlow(workerFlow: Flow<List<WorkInfo>>) {
        viewModelScope.launch(coroutineExceptionHandler) {
            workerFlow.collect { workInfos ->
                val list = workInfos.filter { it.state != WorkInfo.State.SUCCEEDED }
                    .mapNotNull { it.progress.getString(STATUS) }.map { WorkerStatus.valueOf(it) }
                _workerStatusFlow.value =
                    if (list.any { it == WorkerStatus.Loading }) WorkerStatus.Loading else WorkerStatus.Success
                if (list.any { it == WorkerStatus.Loading }.not()) {
                    //To get the latest created widget in the list
                    _triggerRefresh.value = System.currentTimeMillis()
                }
            }
        }
    }


    fun setLastVotedEmptyOptions(list: List<String>) {
        _lastVotedEmptyOptionsFlow.value = list
    }

    fun setFilterType(filter: Filter) {
        _filterFlow.value = filter
        analyticsLogger.widgetFilterTypeEvent(filter.name)
    }

    fun vote(widgetId: String, optionId: String) {
        safeApiCall({}, {
            updateVoteUseCase(widgetId, optionId)
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

}