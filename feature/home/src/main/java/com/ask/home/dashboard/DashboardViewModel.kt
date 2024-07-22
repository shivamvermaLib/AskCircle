package com.ask.home.dashboard

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.widget.BookmarkWidgetUseCase
import com.ask.widget.Filter
import com.ask.widget.GetWidgetsUseCase
import com.ask.widget.UpdateVoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val updateVoteUseCase: UpdateVoteUseCase,
    getWidgetsUseCase: GetWidgetsUseCase,
    private val bookmarkWidgetUseCase: BookmarkWidgetUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {
    private val _filterFlow = MutableStateFlow(Filter.Latest)
    private val _lastVotedEmptyOptionsFlow = MutableStateFlow<List<String>>(emptyList())
    private val _filterWithLastVotedEmptyOptionsFlow =
        combine(_filterFlow, _lastVotedEmptyOptionsFlow) { filter, lastVotedEmptyOptions ->
            filter to lastVotedEmptyOptions
        }

    val widgetsFlow = getWidgetsUseCase(_filterWithLastVotedEmptyOptionsFlow)
        .cachedIn(viewModelScope)
        .onEach { pagingData ->
            println("MAP> $pagingData")
            pagingData.map {
                println("BOOK>${it.isBookmarked} > ${it.widget.title}")
            }
        }
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
}

/*
data class DashboardUiState(
    val error: String? = null
)
*/
