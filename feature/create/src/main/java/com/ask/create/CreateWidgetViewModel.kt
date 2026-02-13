package com.ask.create

import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.category.GetCategoryUseCase
import com.ask.common.BaseViewModel
import com.ask.common.GetAllBadWordsUseCase
import com.ask.common.GetCreateWidgetRemoteConfigUseCase
import com.ask.common.combine
import com.ask.core.EMPTY
import com.ask.country.GetCountryUseCase
import com.ask.widget.Widget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CreateWidgetViewModel @Inject constructor(
    getCountryUseCase: GetCountryUseCase,
    getCategoriesUseCase: GetCategoryUseCase,
    getCreateWidgetRemoteConfigUseCase: GetCreateWidgetRemoteConfigUseCase,
    getAllBadWordsUseCase: GetAllBadWordsUseCase,
    analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {
    private val _badWordsListFlow =
        getAllBadWordsUseCase().map { it.map { badWord -> badWord.english } }
    private val minOptions = 2
    private val maxOptions = getCreateWidgetRemoteConfigUseCase().maxOptionSize
    private val _countriesFlow = getCountryUseCase()
    private val _categoriesFlow = getCategoriesUseCase()

    private val _uiStateFlow = MutableStateFlow(CreateWidgetUiState())

    fun onEvent(event: CreateWidgetUiEvent) {
        _uiStateFlow.update { state ->
            when (event) {
                CreateWidgetUiEvent.AddOptionEvent -> addOption(state)
                is CreateWidgetUiEvent.DescChangedEvent -> state.copy(
                    widget = state.widget.copy(
                        description = event.desc
                    )
                )

                is CreateWidgetUiEvent.EndTimeChangedEvent -> if (event.endTime == null) {
                    state.copy(widget = state.widget.copy(endAt = null))
                } else if (event.endTime >= state.widget.startAt) {
                    state.copy(widget = state.widget.copy(endAt = event.endTime))
                } else {
                    state
                }

                is CreateWidgetUiEvent.ErrorEvent -> state.copy(error = event.error)
                is CreateWidgetUiEvent.GenderChangedEvent -> state.copy(
                    targetAudienceGender = state.targetAudienceGender.copy(
                        gender = event.gender
                    )
                )

                is CreateWidgetUiEvent.MaxAgeChangedEvent -> state.copy(
                    targetAudienceAgeRange = state.targetAudienceAgeRange.copy(
                        max = event.maxAge,
                        min = if (state.targetAudienceAgeRange.min > event.maxAge) {
                            event.maxAge
                        } else {
                            state.targetAudienceAgeRange.min
                        }
                    )
                )

                is CreateWidgetUiEvent.MinAgeChangedEvent -> state.copy(
                    targetAudienceAgeRange = state.targetAudienceAgeRange.copy(
                        min = event.minAge,
                        max = if (state.targetAudienceAgeRange.max < event.minAge) {
                            event.minAge
                        } else {
                            state.targetAudienceAgeRange.max
                        }
                    )
                )

                is CreateWidgetUiEvent.OptionChangedEvent -> state.copy(
                    options = state.options.toMutableList().apply {
                        this[event.index] = event.option
                    }.toList()
                )

                is CreateWidgetUiEvent.OptionTypeChangedEvent -> when (event.optionType) {
                    CreateWidgetUiState.WidgetOptionType.Text -> {
                        state.copy(
                            options = listOf(
                                Widget.Option(text = EMPTY), Widget.Option(text = EMPTY)
                            )
                        )
                    }

                    CreateWidgetUiState.WidgetOptionType.Image -> {
                        state.copy(
                            options = listOf(
                                Widget.Option(imageUrl = EMPTY), Widget.Option(imageUrl = EMPTY)
                            )
                        )
                    }
                }

                is CreateWidgetUiEvent.RemoveCountryEvent -> state.copy(
                    targetAudienceLocations = state.targetAudienceLocations.toMutableList().apply {
                        removeIf { it.country == event.country.name }
                    }.toList()
                )

                is CreateWidgetUiEvent.RemoveOptionEvent -> state.copy(
                    options = if (state.options.size in (minOptions + 1)..maxOptions) {
                        state.options.toMutableList().apply {
                            removeAt(event.index)
                        }.toList()
                    } else state.options
                )

                is CreateWidgetUiEvent.SelectCategoryWidgetEvent -> state.copy(widgetCategories = event.categories)
                is CreateWidgetUiEvent.SelectCountryEvent -> state.copy(
                    targetAudienceLocations = state.targetAudienceLocations + Widget.TargetAudienceLocation(
                        country = event.country.name
                    )
                )

                is CreateWidgetUiEvent.StartTimeChangedEvent -> onStartTimeChange(event, state)
                is CreateWidgetUiEvent.TitleChangedEvent -> state.copy(widget = state.widget.copy(title = event.title))
                is CreateWidgetUiEvent.AllowAnonymousEvent -> state.copy(
                    widget = state.widget.copy(
                        allowAnonymous = event.allowAnonymous
                    )
                )

                is CreateWidgetUiEvent.WidgetResultChangedEvent -> state.copy(
                    widget = state.widget.copy(
                        widgetResult = event.result
                    )
                )

                is CreateWidgetUiEvent.AllowMultipleSelection -> state.copy(
                    widget = state.widget.copy(
                        allowMultipleSelection = event.allow
                    )
                )

                is CreateWidgetUiEvent.UpdateMarriageStatusFilterEvent -> state.copy(
                    targetAudienceGender = state.targetAudienceGender.copy(
                        marriageStatusFilter = event.marriageStatusFilterEvent
                    )
                )

                is CreateWidgetUiEvent.UpdateEducationFilterEvent -> state.copy(
                    targetAudienceGender = state.targetAudienceGender.copy(
                        educationFilter = event.filter
                    )
                )

                is CreateWidgetUiEvent.UpdateOccupationFilterEvent -> state.copy(
                    targetAudienceGender = state.targetAudienceGender.copy(
                        occupationFilter = event.occupationFilter
                    )
                )

                is CreateWidgetUiEvent.UpdateWidgetEvent -> state.copy(
                    widget = event.widget.widget,
                    optionType = if (event.widget.options.any { it.option.text == null && it.option.imageUrl != null }) CreateWidgetUiState.WidgetOptionType.Image else CreateWidgetUiState.WidgetOptionType.Text,
                    options = event.widget.options.map { it.option },
                    targetAudienceGender = event.widget.targetAudienceGender,
                    targetAudienceAgeRange = event.widget.targetAudienceAgeRange,
                    targetAudienceLocations = event.widget.targetAudienceLocations,
                    widgetCategories = event.widget.categories,
                    error = -1,
                )

                else -> {
                    state
                }
            }
        }
    }

    private fun addOption(widgetUiState: CreateWidgetUiState): CreateWidgetUiState {
        val optionType = widgetUiState.optionType
        return if (widgetUiState.options.size in minOptions..<maxOptions) {
            when (optionType) {
                CreateWidgetUiState.WidgetOptionType.Text -> {
                    widgetUiState.copy(options = widgetUiState.options + Widget.Option(text = EMPTY))
                }

                CreateWidgetUiState.WidgetOptionType.Image -> {
                    widgetUiState.copy(options = widgetUiState.options + Widget.Option(imageUrl = EMPTY))
                }
            }
        } else {
            widgetUiState
        }
    }

    private fun onStartTimeChange(
        event: CreateWidgetUiEvent.StartTimeChangedEvent, it: CreateWidgetUiState
    ): CreateWidgetUiState {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return if (event.startTime >= calendar.timeInMillis) {
            if (it.widget.endAt != null && it.widget.endAt!! < event.startTime) {
                val calendar2 = Calendar.getInstance().apply {
                    timeInMillis = event.startTime
                    add(Calendar.DATE, 1)
                }
                it.copy(
                    widget = it.widget.copy(
                        endAt = calendar2.timeInMillis, startAt = event.startTime
                    )
                )
            } else {
                it.copy(
                    widget = it.widget.copy(startAt = event.startTime)
                )
            }
        } else {
            it
        }
    }

    val uiStateFlow = combine(
        _uiStateFlow,
        _countriesFlow,
        _categoriesFlow,
        _badWordsListFlow,
    ) { uiState, countries, categories, badWords ->
        val titleError = if (uiState.widget.title.isBlank()) {
            R.string.title_is_required
        } else if (badWords.any { uiState.widget.title.lowercase().contains(it.lowercase()) }) {
            R.string.title_cannot_contain_bad_words
        } else {
            -1
        }
        val descError = if (uiState.widget.description.isNullOrBlank().not() && badWords.any {
                uiState.widget.description?.lowercase()?.contains(it.lowercase()) == true
            }) {
            R.string.description_cannot_contain_bad_words
        } else {
            -1
        }

        val optionError = uiState.options.filter { option ->
            option.text.isNullOrBlank().not() && badWords.any {
                option.text?.lowercase()?.contains(it.lowercase()) == true
            }
        }.map { it.id }

        val allowCreate =
            uiState.widget.title.isNotBlank() && uiState.options.size in minOptions..maxOptions && optionError.isEmpty() && titleError == -1 && descError == -1
        uiState.copy(
            titleError = titleError,
            descError = descError,
            optionError = optionError,
            allowCreate = allowCreate,
            countries = countries,
            categories = categories,
            minAge = getCreateWidgetRemoteConfigUseCase().minAge,
            maxAge = getCreateWidgetRemoteConfigUseCase().maxAge,
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CreateWidgetUiState())
}