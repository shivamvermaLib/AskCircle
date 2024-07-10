package com.ask.app.ui.screens.create

import com.ask.app.data.models.Country
import com.ask.app.data.models.User
import com.ask.app.data.models.Widget
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience

data class CreateWidgetUiState(
    val title: String = "",
    val titleError: String = "",
    val desc: String = "",
    val descError: String = "",
    val optionType: WidgetOptionType = WidgetOptionType.Text,
    val options: List<Widget.Option> = listOf(
        Widget.Option(text = ""),
        Widget.Option(text = ""),
    ),
    val targetAudienceGender: Widget.TargetAudienceGender = Widget.TargetAudienceGender(gender = Widget.GenderFilter.ALL),
    val targetAudienceAgeRange: Widget.TargetAudienceAgeRange = Widget.TargetAudienceAgeRange(
        min = 16, max = 99
    ),
    val targetAudienceLocations: List<Widget.TargetAudienceLocation> = emptyList(),
    val countries: List<Country> = emptyList(),
    val allowCreate: Boolean = false,
) {
    enum class WidgetOptionType { Text, Image }

    fun toWidgetWithOptionsAndVotesForTargetAudience() =
        WidgetWithOptionsAndVotesForTargetAudience(
            Widget(title = title, description = desc),
            options.map {
                WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                    it,
                    emptyList()
                )
            },
            targetAudienceGender,
            targetAudienceLocations,
            targetAudienceAgeRange,
            User()
        )

}
