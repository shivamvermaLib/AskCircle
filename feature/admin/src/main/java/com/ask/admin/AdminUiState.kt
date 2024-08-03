package com.ask.admin

import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience

data class AdminUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val categories: List<String> = emptyList(),
    val selectedCategories: List<String> = emptyList(),
    val widgets: List<WidgetWithOptionsAndVotesForTargetAudience> = emptyList(),
    val selectedWidget: WidgetWithOptionsAndVotesForTargetAudience? = null,
    val webViewSelectedImages: List<String> = emptyList(),
    val selectedOption: Widget.Option? = null
)