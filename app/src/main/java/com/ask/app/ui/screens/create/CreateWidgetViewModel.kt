package com.ask.app.ui.screens.create

import androidx.lifecycle.viewModelScope
import com.ask.app.data.models.Country
import com.ask.app.data.models.Widget
import com.ask.app.data.repository.CountryRepository
import com.ask.app.ui.screens.utils.BaseViewModel
import com.ask.app.utils.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CreateWidgetViewModel @Inject constructor(
    private val countryRepository: CountryRepository
) : BaseViewModel() {

    private val _titleFlow = MutableStateFlow("")
    private val _titleErrorFlow = MutableStateFlow("")
    private val _descFlow = MutableStateFlow("")
    private val _descErrorFlow = MutableStateFlow("")
    private val _optionType = MutableStateFlow(CreateWidgetUiState.WidgetOptionType.Text)
    private val _options = MutableStateFlow(
        listOf(
            Widget.Option(text = ""),
            Widget.Option(text = ""),
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
                _options.value = listOf(Widget.Option(text = ""), Widget.Option(text = ""))
            }

            CreateWidgetUiState.WidgetOptionType.Image -> {
                _options.value = listOf(Widget.Option(imageUrl = ""), Widget.Option(imageUrl = ""))
            }
        }
        _optionType.value = type
    }

    fun addOption() {
        val optionType = _optionType.value
        when (optionType) {
            CreateWidgetUiState.WidgetOptionType.Text -> {
                _options.value += Widget.Option(text = "")
            }

            CreateWidgetUiState.WidgetOptionType.Image -> {
                _options.value += Widget.Option(imageUrl = "")
            }
        }
    }

    fun updateOption(index: Int, option: Widget.Option) {
        _options.value = _options.value.toMutableList().apply {
            this[index] = option
        }.toList()
    }

    fun removeOption(index: Int) {
        _options.value = _options.value.toMutableList().apply {
            removeAt(index)
        }.toList()
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
        val allowCreate = title.isNotBlank() && options.isNotEmpty() && options.size >= 2
            && ((optionType == CreateWidgetUiState.WidgetOptionType.Text && options.all { !it.text.isNullOrBlank() }) || (optionType == CreateWidgetUiState.WidgetOptionType.Image && options.all { !it.imageUrl.isNullOrBlank() }))
        CreateWidgetUiState(
            title,
            titleError,
            desc,
            descError,
            optionType,
            options,
            gender,
            ageRange,
            locations,
            countries,
            allowCreate,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CreateWidgetUiState())
}