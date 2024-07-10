package com.ask.app.ui.screens.create

import com.ask.app.EMPTY
import com.ask.app.MAX_AGE_RANGE
import com.ask.app.MIN_AGE_RANGE
import com.ask.app.data.models.Country
import com.ask.app.data.models.User
import com.ask.app.data.models.Widget
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience

data class CreateWidgetUiState(
    val title: String = EMPTY,
    val titleError: String = EMPTY,
    val desc: String = EMPTY,
    val descError: String = EMPTY,
    val optionType: WidgetOptionType = WidgetOptionType.Text,
    val options: List<Widget.Option> = listOf(
        Widget.Option(text = EMPTY),
        Widget.Option(text = EMPTY),
    ),
    val targetAudienceGender: Widget.TargetAudienceGender = Widget.TargetAudienceGender(gender = Widget.GenderFilter.ALL),
    val targetAudienceLocations: List<Widget.TargetAudienceLocation> = emptyList(),
    val countries: List<Country> = emptyList(),
    val allowCreate: Boolean = false,
    val minAge: Int = MIN_AGE_RANGE,
    val maxAge: Int = MAX_AGE_RANGE,
    val targetAudienceAgeRange: Widget.TargetAudienceAgeRange = Widget.TargetAudienceAgeRange(
        min = minAge, max = maxAge
    ),
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
