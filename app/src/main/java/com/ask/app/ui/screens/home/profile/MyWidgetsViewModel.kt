package com.ask.app.ui.screens.home.profile

import androidx.lifecycle.viewModelScope
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.app.domain.GetCurrentUserWidgetsUseCase
import com.ask.app.ui.screens.utils.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MyWidgetsViewModel @Inject constructor(getCurrentUserWidgetsUseCase: GetCurrentUserWidgetsUseCase) :
    BaseViewModel() {

    private val userWidgets = getCurrentUserWidgetsUseCase()
    private val _error = MutableStateFlow<String?>(null)

    val uiStateFlow = combine(userWidgets, _error) { widgets, error ->
        MyWidgetsUiState(widgets, error)
    }.catch {
        it.printStackTrace()
        _error.value = it.message
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyWidgetsUiState())

    fun setError(error: String?) {
        _error.value = error
    }
}

data class MyWidgetsUiState(
    val widgets: List<WidgetWithOptionsAndVotesForTargetAudience> = emptyList(),
    val error: String? = null,
)