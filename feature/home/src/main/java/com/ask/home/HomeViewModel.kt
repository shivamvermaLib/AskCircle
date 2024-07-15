package com.ask.home

import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
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
class HomeViewModel @Inject constructor(analyticsLogger: AnalyticsLogger) :
    BaseViewModel(analyticsLogger) {

    private val _workerStatusFlow = MutableStateFlow(WorkerStatus.None)
    private val _errorFlow = MutableStateFlow<String?>(null)
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
        FirebaseCrashlytics.getInstance().recordException(exception)
    }

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
            }
        }
    }

}