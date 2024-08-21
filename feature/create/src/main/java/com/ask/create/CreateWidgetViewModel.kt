package com.ask.create

import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.category.GetCategoryUseCase
import com.ask.common.BaseViewModel
import com.ask.common.GetAllBadWordsUseCase
import com.ask.common.GetCreateWidgetRemoteConfigUseCase
import com.ask.common.combine
import com.ask.core.EMPTY
import com.ask.country.Country
import com.ask.country.GetCountryUseCase
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val _titleFlow = MutableStateFlow(EMPTY)
    private val _descFlow = MutableStateFlow(EMPTY)
    private val _optionType = MutableStateFlow(CreateWidgetUiState.WidgetOptionType.Text)
    private val _options = MutableStateFlow(
        listOf(
            Widget.Option(text = EMPTY),
            Widget.Option(text = EMPTY),
        )
    )
    private val _targetAudienceGender = MutableStateFlow(Widget.TargetAudienceGender())
    private val _targetAudienceAgeRange = MutableStateFlow(Widget.TargetAudienceAgeRange())
    private val _targetAudienceLocations =
        MutableStateFlow(emptyList<Widget.TargetAudienceLocation>())
    private val _selectedWidgetCategories = MutableStateFlow(emptyList<Widget.WidgetCategory>())
    private val _countriesFlow = getCountryUseCase()
    private val _categoriesFlow = getCategoriesUseCase()
    private val _startAtFlow = MutableStateFlow(System.currentTimeMillis())
    private val _endAtFlow = MutableStateFlow<Long?>(null)
    private val _errorFlow = MutableStateFlow(-1)
    private val _anonymousVoting = MutableStateFlow(true)

    fun onEvent(event: CreateWidgetUiEvent) {
        viewModelScope.launch {
            when (event) {
                CreateWidgetUiEvent.AddOptionEvent -> addOption()
                is CreateWidgetUiEvent.DescChangedEvent -> setDesc(event.desc)
                is CreateWidgetUiEvent.EndTimeChangedEvent -> onEndTimeChange(event.endTime)
                is CreateWidgetUiEvent.ErrorEvent -> setError(event.error)
                is CreateWidgetUiEvent.GenderChangedEvent -> setGender(event.gender)
                is CreateWidgetUiEvent.MaxAgeChangedEvent -> setMaxAge(event.maxAge)
                is CreateWidgetUiEvent.MinAgeChangedEvent -> setMinAge(event.minAge)
                is CreateWidgetUiEvent.OptionChangedEvent -> updateOption(event.index, event.option)
                is CreateWidgetUiEvent.OptionTypeChangedEvent -> setOptionType(event.optionType)
                is CreateWidgetUiEvent.RemoveCountryEvent -> removeLocation(event.country)
                is CreateWidgetUiEvent.RemoveOptionEvent -> removeOption(event.index)
                is CreateWidgetUiEvent.SelectCategoryWidgetEvent -> selectCategoryWidget(event.categories)
                is CreateWidgetUiEvent.SelectCountryEvent -> addLocation(event.country)
                is CreateWidgetUiEvent.StartTimeChangedEvent -> onStartTimeChange(event.startTime)
                is CreateWidgetUiEvent.TitleChangedEvent -> setTitle(event.title)
                is CreateWidgetUiEvent.AllowAnonymousEvent -> _anonymousVoting.value =
                    event.allowAnonymous

                else -> {
                    println("Event not handled")
                }
            }
        }
    }

    private fun setTitle(title: String) {
        _titleFlow.value = title
    }

    private fun setDesc(desc: String) {
        _descFlow.value = desc
    }

    private fun setOptionType(type: CreateWidgetUiState.WidgetOptionType) {
        when (type) {
            CreateWidgetUiState.WidgetOptionType.Text -> {
                _options.value = listOf(Widget.Option(text = EMPTY), Widget.Option(text = EMPTY))
            }

            CreateWidgetUiState.WidgetOptionType.Image -> {
                _options.value =
                    listOf(Widget.Option(imageUrl = EMPTY), Widget.Option(imageUrl = EMPTY))
            }
        }
        _optionType.value = type
    }

    private fun addOption() {
        val optionType = _optionType.value
        if (_options.value.size in minOptions..<maxOptions) {
            when (optionType) {
                CreateWidgetUiState.WidgetOptionType.Text -> {
                    _options.value += Widget.Option(text = EMPTY)
                }

                CreateWidgetUiState.WidgetOptionType.Image -> {
                    _options.value += Widget.Option(imageUrl = EMPTY)
                }
            }
        }
    }

    private fun updateOption(index: Int, option: Widget.Option) {
        viewModelScope.launch {
            _options.value = _options.value.toMutableList().apply {
                this[index] = option
            }.toList()
        }
    }

    private fun removeOption(index: Int) {
        if (_options.value.size in (minOptions + 1)..maxOptions) {
            _options.value = _options.value.toMutableList().apply {
                removeAt(index)
            }.toList()
        }
    }

    private fun setGender(genderFilter: Widget.GenderFilter) {
        _targetAudienceGender.value = _targetAudienceGender.value.copy(gender = genderFilter)
    }

    private fun setMinAge(minAge: Int) {
        _targetAudienceAgeRange.value = _targetAudienceAgeRange.value.copy(
            min = minAge,
            max = if (_targetAudienceAgeRange.value.max < minAge) {
                minAge
            } else {
                _targetAudienceAgeRange.value.max
            }
        )
    }

    private fun setMaxAge(maxAge: Int) {
        _targetAudienceAgeRange.value = _targetAudienceAgeRange.value.copy(
            max = maxAge,
            min = if (_targetAudienceAgeRange.value.min > maxAge) {
                maxAge
            } else {
                _targetAudienceAgeRange.value.min
            }
        )
    }

    private fun addLocation(country: Country) {
        _targetAudienceLocations.value += Widget.TargetAudienceLocation(country = country.name)
    }

    private fun removeLocation(country: Country) {
        _targetAudienceLocations.value = _targetAudienceLocations.value.toMutableList().apply {
            removeIf { it.country == country.name }
        }.toList()
    }

    private fun selectCategoryWidget(widgetCategories: List<Widget.WidgetCategory>) {
        _selectedWidgetCategories.value = widgetCategories
    }

    private fun onStartTimeChange(startAt: Long) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (startAt >= calendar.timeInMillis) {
            _startAtFlow.value = startAt
            if (_endAtFlow.value != null && _endAtFlow.value!! < startAt) {
                val calendar2 = Calendar.getInstance().apply {
                    timeInMillis = startAt
                    add(Calendar.DATE, 1)
                }
                _endAtFlow.value = calendar2.timeInMillis
            }
        }
    }

    private fun onEndTimeChange(endAt: Long?) {
        if (endAt == null) {
            _endAtFlow.value = null
        } else if (endAt >= _startAtFlow.value) {
            _endAtFlow.value = endAt
        }
    }

    fun setWidget(widget: WidgetWithOptionsAndVotesForTargetAudience) {
        _titleFlow.value = widget.widget.title
        _selectedWidgetCategories.value = widget.categories
        _descFlow.value = widget.widget.description ?: EMPTY
        _optionType.value =
            if (widget.options.any { it.option.text == null && it.option.imageUrl != null }) CreateWidgetUiState.WidgetOptionType.Image else CreateWidgetUiState.WidgetOptionType.Text
        _options.value = widget.options.map { it.option }
        _targetAudienceGender.value = widget.targetAudienceGender
        _targetAudienceAgeRange.value = widget.targetAudienceAgeRange
        _targetAudienceLocations.value = widget.targetAudienceLocations
        _startAtFlow.value = widget.widget.startAt
        _endAtFlow.value = widget.widget.endAt
        _errorFlow.value = -1
    }

    private fun setError(id: Int) {
        _errorFlow.value = id
    }


    val uiStateFlow = combine(
        _titleFlow,
        _descFlow,
        _optionType,
        _options,
        _targetAudienceGender,
        _targetAudienceAgeRange,
        _targetAudienceLocations,
        _countriesFlow,
        _categoriesFlow,
        _selectedWidgetCategories,
        _startAtFlow,
        _endAtFlow,
        _errorFlow,
        _badWordsListFlow,
        _anonymousVoting
    ) { title, desc, optionType, options, gender, targetAudienceAgeRange, locations, countries, categories, widgetCategories, startAt, endAt, error, badWords, anonymousVoting ->
        val titleError = if (title.isBlank()) {
            R.string.title_is_required
        } else if (badWords.contains(title.lowercase())) {
            R.string.title_cannot_contain_bad_words
        } else {
            -1
        }
        val descError = if (badWords.contains(desc.lowercase())) {
            R.string.description_cannot_contain_bad_words
        } else {
            -1
        }

        val optionError = options.filter {
            it.text.isNullOrBlank().not() && badWords.contains(it.text!!.lowercase())
        }.map { it.id }

        val allowCreate =
            title.isNotBlank() && options.size in minOptions..maxOptions && optionError.isEmpty() && titleError == -1 && descError == -1
        CreateWidgetUiState(
            title,
            titleError,
            desc,
            descError,
            optionType,
            options,
            gender,
            locations.distinctBy { it.country },
            countries,
            allowCreate,
            targetAudienceAgeRange = targetAudienceAgeRange,
            minAge = getCreateWidgetRemoteConfigUseCase().minAge,
            maxAge = getCreateWidgetRemoteConfigUseCase().maxAge,
            categories = categories,
            widgetCategories = widgetCategories,
            startTime = startAt,
            endTime = endAt,
            error = error,
            optionError = optionError,
            allowAnonymous = anonymousVoting
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CreateWidgetUiState())
}