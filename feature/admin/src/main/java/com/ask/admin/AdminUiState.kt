package com.ask.admin

import com.ask.country.Country
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience

data class AdminUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val selectedCategories: List<String> = emptyList(),
    val widgets: List<WidgetWithOptionsAndVotesForTargetAudience> = emptyList(),
    val selectedWidget: WidgetWithOptionsAndVotesForTargetAudience? = null,
    val webViewSelectedImages: List<String> = emptyList(),
    val selectedOption: Widget.Option? = null,
    val searchList: Set<String> = emptySet(),
    val selectedCountries: List<Country> = emptyList()
)