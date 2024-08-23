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
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
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
        _uiStateFlow.update {
            when (event) {
                CreateWidgetUiEvent.AddOptionEvent -> addOption(it)
                is CreateWidgetUiEvent.DescChangedEvent -> it.copy(
                    widget = it.widget.copy(
                        description = event.desc
                    )
                )

                is CreateWidgetUiEvent.EndTimeChangedEvent -> if (event.endTime == null) {
                    it.copy(widget = it.widget.copy(endAt = null))
                } else if (event.endTime >= it.widget.startAt) {
                    it.copy(widget = it.widget.copy(endAt = event.endTime))
                } else {
                    it
                }

                is CreateWidgetUiEvent.ErrorEvent -> it.copy(error = event.error)
                is CreateWidgetUiEvent.GenderChangedEvent -> it.copy(
                    targetAudienceGender = it.targetAudienceGender.copy(
                        gender = event.gender
                    )
                )

                is CreateWidgetUiEvent.MaxAgeChangedEvent -> it.copy(
                    targetAudienceAgeRange = it.targetAudienceAgeRange.copy(
                        max = event.maxAge,
                        min = if (it.targetAudienceAgeRange.min > event.maxAge) {
                            event.maxAge
                        } else {
                            it.targetAudienceAgeRange.min
                        }
                    )
                )

                is CreateWidgetUiEvent.MinAgeChangedEvent -> it.copy(
                    targetAudienceAgeRange = it.targetAudienceAgeRange.copy(
                        min = event.minAge,
                        max = if (it.targetAudienceAgeRange.max < event.minAge) {
                            event.minAge
                        } else {
                            it.targetAudienceAgeRange.max
                        }
                    )
                )

                is CreateWidgetUiEvent.OptionChangedEvent -> it.copy(
                    options = it.options.toMutableList().apply {
                        this[event.index] = event.option
                    }.toList()
                )

                is CreateWidgetUiEvent.OptionTypeChangedEvent -> when (event.optionType) {
                    CreateWidgetUiState.WidgetOptionType.Text -> {
                        it.copy(
                            options = listOf(
                                Widget.Option(text = EMPTY), Widget.Option(text = EMPTY)
                            )
                        )
                    }

                    CreateWidgetUiState.WidgetOptionType.Image -> {
                        it.copy(
                            options = listOf(
                                Widget.Option(imageUrl = EMPTY), Widget.Option(imageUrl = EMPTY)
                            )
                        )
                    }
                }

                is CreateWidgetUiEvent.RemoveCountryEvent -> it.copy(
                    targetAudienceLocations = it.targetAudienceLocations.toMutableList().apply {
                        removeIf { it.country == event.country.name }
                    }.toList()
                )

                is CreateWidgetUiEvent.RemoveOptionEvent -> it.copy(
                    options = if (it.options.size in (minOptions + 1)..maxOptions) {
                        it.options.toMutableList().apply {
                            removeAt(event.index)
                        }.toList()
                    } else it.options
                )

                is CreateWidgetUiEvent.SelectCategoryWidgetEvent -> it.copy(widgetCategories = event.categories)
                is CreateWidgetUiEvent.SelectCountryEvent -> it.copy(
                    targetAudienceLocations = it.targetAudienceLocations + Widget.TargetAudienceLocation(
                        country = event.country.name
                    )
                )

                is CreateWidgetUiEvent.StartTimeChangedEvent -> onStartTimeChange(event, it)
                is CreateWidgetUiEvent.TitleChangedEvent -> it.copy(widget = it.widget.copy(title = event.title))
                is CreateWidgetUiEvent.AllowAnonymousEvent -> it.copy(
                    widget = it.widget.copy(
                        allowAnonymous = event.allowAnonymous
                    )
                )

                is CreateWidgetUiEvent.WidgetResultChangedEvent -> it.copy(
                    widget = it.widget.copy(
                        widgetResult = event.result
                    )
                )

                is CreateWidgetUiEvent.AllowMultipleSelection -> it.copy(
                    widget = it.widget.copy(
                        allowMultipleSelection = event.allow
                    )
                )

                else -> {
                    it
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

    fun setWidget(widget: WidgetWithOptionsAndVotesForTargetAudience) {
        _uiStateFlow.update { widgetUiState ->
            widgetUiState.copy(
                widget = widget.widget,
                optionType = if (widget.options.any { it.option.text == null && it.option.imageUrl != null }) CreateWidgetUiState.WidgetOptionType.Image else CreateWidgetUiState.WidgetOptionType.Text,
                options = widget.options.map { it.option },
                targetAudienceGender = widget.targetAudienceGender,
                targetAudienceAgeRange = widget.targetAudienceAgeRange,
                targetAudienceLocations = widget.targetAudienceLocations,
                widgetCategories = widget.categories,
                error = -1,
            )
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CreateWidgetUiState())
}