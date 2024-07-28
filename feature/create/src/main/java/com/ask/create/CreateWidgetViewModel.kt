package com.ask.create

import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.category.GetCategoryUseCase
import com.ask.common.BaseViewModel
import com.ask.common.GetCreateWidgetRemoteConfigUseCase
import com.ask.common.combine
import com.ask.core.EMPTY
import com.ask.country.Country
import com.ask.country.GetCountryUseCase
import com.ask.widget.Widget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CreateWidgetViewModel @Inject constructor(
    getCountryUseCase: GetCountryUseCase,
    getCategoriesUseCase: GetCategoryUseCase,
    getCreateWidgetRemoteConfigUseCase: GetCreateWidgetRemoteConfigUseCase,
    analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {
    private val minOptions = 2
    private val maxOptions = getCreateWidgetRemoteConfigUseCase().maxOptionSize
    private val _titleFlow = MutableStateFlow(EMPTY)
    private val _titleErrorFlow = MutableStateFlow(EMPTY)
    private val _descFlow = MutableStateFlow(EMPTY)
    private val _descErrorFlow = MutableStateFlow(EMPTY)
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
    private val _errorFlow = MutableStateFlow<String?>(null)

    fun setTitle(title: String) {
        _titleFlow.value = title
        _titleErrorFlow.value = if (title.isBlank()) {
            "Title is required"
        } else {
            ""
        }
    }

    fun setDesc(desc: String) {
        _descFlow.value = desc
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
        _options.value = _options.value.toMutableList().apply {
            this[index] = option
        }.toList()
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
    ) { title, titleError, desc, descError, optionType, options, gender, targetAudienceAgeRange, locations, countries, categories, widgetCategories, startAt, endAt, error ->
        val allowCreate = title.isNotBlank() && options.isNotEmpty() && options.size in 2..4
            && ((optionType == CreateWidgetUiState.WidgetOptionType.Text && options.all { !it.text.isNullOrBlank() }) || (optionType == CreateWidgetUiState.WidgetOptionType.Image && options.all { !it.imageUrl.isNullOrBlank() }))
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
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CreateWidgetUiState())
}