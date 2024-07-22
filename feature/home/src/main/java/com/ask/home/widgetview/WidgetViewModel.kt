package com.ask.home.widgetview

import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.widget.GetWidgetDetailsUseCase
import com.ask.widget.UpdateVoteUseCase
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    analyticsLogger: AnalyticsLogger,
    private val updateVoteUseCase: UpdateVoteUseCase,
    private val getWidgetDetailsUseCase: GetWidgetDetailsUseCase,
) : BaseViewModel(analyticsLogger) {

    private val _uiStateFlow = MutableStateFlow(WidgetUiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    fun init(
        widgetId: String,
        lastVotedEmptyOptions: List<String>,
        preloadImages: suspend (List<String>) -> Unit
    ) {
        safeApiCall({
            _uiStateFlow.value = _uiStateFlow.value.copy(loading = true)
        }, {
            getWidgetDetailsUseCase.invoke(widgetId, lastVotedEmptyOptions, preloadImages).let {
                _uiStateFlow.value = _uiStateFlow.value.copy(
                    loading = false,
                    widgetWithOptionsAndVotesForTargetAudience = it
                )
            }
        }, {
            _uiStateFlow.value = _uiStateFlow.value.copy(loading = false, error = it)
        })

    }

    fun vote(widgetId: String, optionId: String, screenName: String) {
        safeApiCall({}, {
            updateVoteUseCase(widgetId, optionId, screenName)
        }, {
//            _errorFlow.value = it
        })
    }

}

data class WidgetUiState(
    val widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience? = null,
    val loading: Boolean = false,
    val error: String? = null
)