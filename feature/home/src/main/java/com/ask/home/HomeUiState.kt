package com.ask.home

import com.ask.workmanager.WorkerStatus


data class HomeUiState(
    val createWidgetStatus: WorkerStatus = WorkerStatus.None,
    val error: String? = null
)
