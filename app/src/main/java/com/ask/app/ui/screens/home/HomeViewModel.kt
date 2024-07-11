package com.ask.app.ui.screens.home

import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.ask.app.STATUS
import com.ask.app.analytics.AnalyticsLogger
import com.ask.app.ui.screens.utils.BaseViewModel
import com.ask.app.workmanager.WorkerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(analyticsLogger: AnalyticsLogger) :
    BaseViewModel(analyticsLogger) {

    private val _workerStatusFlow = MutableStateFlow(WorkerStatus.None)
    private val _errorFlow = MutableStateFlow<String?>(null)

    val uiStateFlow = combine(_workerStatusFlow, _errorFlow) { workerStatus, error ->
        HomeUiState(workerStatus, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun setWorkerFlow(workerFlow: Flow<MutableList<WorkInfo>>) {
        viewModelScope.launch {
            workerFlow.collect { workInfos ->
                val list = workInfos.filter { it.state != WorkInfo.State.SUCCEEDED }
                    .mapNotNull { it.progress.getString(STATUS) }.map { WorkerStatus.valueOf(it) }
                _workerStatusFlow.value =
                    if (list.any { it == WorkerStatus.Loading }) WorkerStatus.Loading else WorkerStatus.Success
            }
        }
    }

}