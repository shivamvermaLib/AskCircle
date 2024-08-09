package com.ask.create

import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.category.GetCategoryUseCase
import com.ask.common.BaseViewModel
import com.ask.common.CheckModerationUseCase
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CreateWidgetViewModel @Inject constructor(
    getCountryUseCase: GetCountryUseCase,
    getCategoriesUseCase: GetCategoryUseCase,
    getCreateWidgetRemoteConfigUseCase: GetCreateWidgetRemoteConfigUseCase,
    private val checkModerationUseCase: CheckModerationUseCase,
    analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {
    private val minOptions = 2
    private val maxOptions = getCreateWidgetRemoteConfigUseCase().maxOptionSize
    private val _titleFlow = MutableStateFlow(EMPTY)
    private val _titleErrorFlow = MutableStateFlow(-1)
    private val _descFlow = MutableStateFlow(EMPTY)
    private val _descErrorFlow = MutableStateFlow(-1)
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
    private val _optionErrorFlow = MutableStateFlow(emptyList<String>())

    fun setTitle(title: String) {
        viewModelScope.launch {
            _titleFlow.value = title
            _titleErrorFlow.value = if (title.isBlank()) {
                R.string.title_is_required
            } else if (checkModerationUseCase.invoke(title)) {
                R.string.title_cannot_contain_bad_words
            } else {
                -1
            }
        }
    }

    fun setDesc(desc: String) {
        viewModelScope.launch {
            _descFlow.value = desc
            if (checkModerationUseCase.invoke(desc)) {
                _descErrorFlow.value = R.string.description_cannot_contain_bad_words
            } else {
                _descErrorFlow.value = -1
            }
        }
    }

    fun setOptionType(type: CreateWidgetUiState.WidgetOptionType) {
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

    fun addOption() {
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

    fun updateOption(index: Int, option: Widget.Option) {
        viewModelScope.launch {
            if (option.text.isNullOrBlank().not()) {
                if (checkModerationUseCase.invoke(option.text!!)) {
                    _optionErrorFlow.value += option.id
                    setError(R.string.text_cannot_contain_bad_words)
                } else {
                    _optionErrorFlow.value -= option.id
                    setError(-1)
                }
            }
            _options.value = _options.value.toMutableList().apply {
                this[index] = option
            }.toList()
        }
    }

    fun removeOption(index: Int) {
        if (_options.value.size in (minOptions + 1)..maxOptions) {
            _options.value = _options.value.toMutableList().apply {
                removeAt(index)
            }.toList()
        }
    }

    fun setGender(genderFilter: Widget.GenderFilter) {
        _targetAudienceGender.value = _targetAudienceGender.value.copy(gender = genderFilter)
    }

    fun setMinAge(minAge: Int) {
        _targetAudienceAgeRange.value = _targetAudienceAgeRange.value.copy(
            min = minAge,
            max = if (_targetAudienceAgeRange.value.max < minAge) {
                minAge
            } else {
                _targetAudienceAgeRange.value.max
            }
        )
    }

    fun setMaxAge(maxAge: Int) {
        _targetAudienceAgeRange.value = _targetAudienceAgeRange.value.copy(
            max = maxAge,
            min = if (_targetAudienceAgeRange.value.min > maxAge) {
                maxAge
            } else {
                _targetAudienceAgeRange.value.min
            }
        )
    }

    fun addLocation(country: Country) {
        _targetAudienceLocations.value += Widget.TargetAudienceLocation(country = country.name)
    }

    fun removeLocation(country: Country) {
        _targetAudienceLocations.value = _targetAudienceLocations.value.toMutableList().apply {
            removeIf { it.country == country.name }
        }.toList()
    }

    fun selectCategoryWidget(widgetCategories: List<Widget.WidgetCategory>) {
        _selectedWidgetCategories.value = widgetCategories
    }

    fun onStartTimeChange(startAt: Long) {
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

    fun onEndTimeChange(endAt: Long?) {
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

    fun setError(id: Int) {
        _errorFlow.value = id
    }

    val uiStateFlow = combine(
        _titleFlow,
        _titleErrorFlow,
        _descFlow,
        _descErrorFlow,
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
        _optionErrorFlow
    ) { title, titleError, desc, descError, optionType, options, gender, targetAudienceAgeRange, locations, countries, categories, widgetCategories, startAt, endAt, error, optionError ->
        val textOptionValid = (optionType == CreateWidgetUiState.WidgetOptionType.Text && options.all {
            !it.text.isNullOrBlank() && checkModerationUseCase(
                it.text!!
            ).not()
        })
        val imageOptionValid = (optionType == CreateWidgetUiState.WidgetOptionType.Image && options.all { !it.imageUrl.isNullOrBlank() })
        val allowCreate = title.isNotBlank() && options.isNotEmpty() && options.size in 2..4
            && (textOptionValid || imageOptionValid)
        CreateWidgetUiState(
            title,
            titleError,
            desc,
            descError,
            optionType,
            options,
            gender,
            locations,
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
            optionError = optionError
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CreateWidgetUiState())
}