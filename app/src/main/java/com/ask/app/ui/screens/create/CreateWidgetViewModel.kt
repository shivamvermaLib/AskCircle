package com.ask.app.ui.screens.create

import androidx.lifecycle.viewModelScope
import com.ask.app.EMPTY
import com.ask.app.analytics.AnalyticsLogger
import com.ask.app.data.models.Country
import com.ask.app.data.models.Widget
import com.ask.app.data.repository.CountryRepository
import com.ask.app.remote.config.RemoteConfigRepository
import com.ask.app.ui.screens.utils.BaseViewModel
import com.ask.app.utils.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CreateWidgetViewModel @Inject constructor(
    countryRepository: CountryRepository,
    remoteConfigRepository: RemoteConfigRepository,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {
    private val minOptions = 2
    private val maxOptions = remoteConfigRepository.getMaxOptionSize()
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
    private val _countriesFlow = countryRepository.getCountries()
    private val _loading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

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
    ) { title, titleError, desc, descError, optionType, options, gender, ageRange, locations, countries ->
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
            targetAudienceAgeRange = ageRange,
            minAge = remoteConfigRepository.getAgeRangeMin().toInt(),
            maxAge = remoteConfigRepository.getAgeRangeMax().toInt()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CreateWidgetUiState())
}