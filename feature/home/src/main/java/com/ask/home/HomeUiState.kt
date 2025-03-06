package com.ask.home

import com.ask.core.EMPTY
import com.ask.user.User
import com.ask.workmanager.WorkerStatus


data class HomeUiState(
    val createWidgetStatus: WorkerStatus = WorkerStatus.None,
    val error: String? = null,
    val user: User = User(),
    val search: String = EMPTY
)
