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
    val titleError: Int = -1,
    val desc: String = EMPTY,
    val descError: Int = -1,
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
    val categories: List<CategoryWithSubCategory> = emptyList(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val error: Int = -1,
    val maxYearAllowed: Int = 0,
    val optionError: List<String> = emptyList()
) {
    enum class WidgetOptionType { Text, Image }

    fun toWidgetWithOptionsAndVotesForTargetAudience() =
        WidgetWithOptionsAndVotesForTargetAudience(
            Widget(title = title, description = desc, startAt = startTime, endAt = endTime),
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
