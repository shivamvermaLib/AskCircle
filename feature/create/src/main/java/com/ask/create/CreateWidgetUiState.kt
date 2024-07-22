package com.ask.create

import com.ask.category.CategoryWithSubCategory
import com.ask.common.MAX_AGE_RANGE
import com.ask.common.MIN_AGE_RANGE
import com.ask.core.EMPTY
import com.ask.country.Country
import com.ask.user.User
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience

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
    val widgetCategories: List<Widget.WidgetCategory> = emptyList(),
    val categories: List<CategoryWithSubCategory> = emptyList()
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
            User(),
            widgetCategories,
            false
        )

}
