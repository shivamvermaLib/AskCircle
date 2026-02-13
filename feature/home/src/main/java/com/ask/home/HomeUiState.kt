package com.ask.home

import com.ask.core.EMPTY
import com.ask.user.User
import com.ask.widget.Filter
import com.ask.workmanager.WorkerStatus


data class HomeUiState(
    val createWidgetStatus: WorkerStatus = WorkerStatus.None,
    val error: String? = null,
    val user: User = User(),
    val search: String = EMPTY
)

sealed interface HomeUiEvent {
    data class UpdateSearch(val search: String) : HomeUiEvent
    data class UpdateFilter(val filter: Filter) : HomeUiEvent
    data class UpdateLastVotedOptions(val lastVotedOptions: List<String>) : HomeUiEvent
    data class Vote(val widgetId: String, val optionId: String) : HomeUiEvent
    data class Bookmark(val widgetId: String) : HomeUiEvent
    data class StartStopWidgetAcceptingVote(val widgetId: String, val start: Boolean) : HomeUiEvent
}