package com.ask.app.ui.screens.home

import com.ask.app.workmanager.WorkerStatus

data class HomeUiState(
    val createWidgetStatus: WorkerStatus = WorkerStatus.None,
    val error: String? = null
)
