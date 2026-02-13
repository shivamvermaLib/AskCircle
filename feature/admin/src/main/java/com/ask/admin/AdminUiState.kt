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

sealed interface AdminUiIntent {
    data object FetchCountries : AdminUiIntent
    data object FetchCategories : AdminUiIntent
    data class AskAI(val text: String, val number: Int) : AdminUiIntent
    data class RemoveWidget(val widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience) :
        AdminUiIntent

    data class SelectWidgetForTextToImageOption(val widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience?) :
        AdminUiIntent

    data class OnFetchImage(val json: String) : AdminUiIntent
    data class UpdateWidget(val widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience) :
        AdminUiIntent

    data class OnOptionSelected(val option: Widget.Option) : AdminUiIntent
}