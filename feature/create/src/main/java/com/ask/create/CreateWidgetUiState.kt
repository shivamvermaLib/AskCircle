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
    val widget: Widget = Widget(),
    val titleError: Int = -1,
    val descError: Int = -1,
    val optionType: WidgetOptionType = WidgetOptionType.Text,
    val options: List<Widget.Option> = listOf(
        Widget.Option(text = EMPTY),
        Widget.Option(text = EMPTY),
    ),
    val targetAudienceGender: Widget.TargetAudienceGender = Widget.TargetAudienceGender(),
    val targetAudienceLocations: List<Widget.TargetAudienceLocation> = emptyList(),
    val countries: List<Country> = emptyList(),
    val allowCreate: Boolean = false,
    val minAge: Int = MIN_AGE_RANGE,
    val maxAge: Int = MAX_AGE_RANGE,
    val targetAudienceAgeRange: Widget.TargetAudienceAgeRange = Widget.TargetAudienceAgeRange(
        min = 0,
        max = 0
    ),
    val widgetCategories: List<Widget.WidgetCategory> = emptyList(),
    val categories: List<CategoryWithSubCategory> = emptyList(),
    val error: Int = -1,
    val maxYearAllowed: Int = 0,
    val optionError: List<String> = emptyList(),
) {


    enum class WidgetOptionType { Text, Image }

    fun toWidgetWithOptionsAndVotesForTargetAudience() =
        WidgetWithOptionsAndVotesForTargetAudience(
            widget,
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
            false,
        )
}


sealed interface CreateWidgetUiEvent {
    data object CreateWidgetEvent : CreateWidgetUiEvent
    data class TitleChangedEvent(val title: String) : CreateWidgetUiEvent
    data class DescChangedEvent(val desc: String) : CreateWidgetUiEvent
    data class OptionTypeChangedEvent(val optionType: CreateWidgetUiState.WidgetOptionType) :
        CreateWidgetUiEvent

    data object BackEvent : CreateWidgetUiEvent
    data class OptionChangedEvent(val index: Int, val option: Widget.Option) : CreateWidgetUiEvent
    data object AddOptionEvent : CreateWidgetUiEvent
    data class RemoveOptionEvent(val index: Int) : CreateWidgetUiEvent
    data class GenderChangedEvent(val gender: Widget.GenderFilter) : CreateWidgetUiEvent
    data class MinAgeChangedEvent(val minAge: Int) : CreateWidgetUiEvent
    data class MaxAgeChangedEvent(val maxAge: Int) : CreateWidgetUiEvent
    data class SelectCountryEvent(val country: Country) : CreateWidgetUiEvent
    data class RemoveCountryEvent(val country: Country) : CreateWidgetUiEvent
    data class SelectCategoryWidgetEvent(val categories: List<Widget.WidgetCategory>) :
        CreateWidgetUiEvent

    data class StartTimeChangedEvent(val startTime: Long) : CreateWidgetUiEvent
    data class EndTimeChangedEvent(val endTime: Long?) : CreateWidgetUiEvent
    data class ErrorEvent(val error: Int) : CreateWidgetUiEvent
    data class AllowAnonymousEvent(val allowAnonymous: Boolean) : CreateWidgetUiEvent
    data class WidgetResultChangedEvent(val result: Widget.WidgetResult) : CreateWidgetUiEvent
    data class AllowMultipleSelection(val allow: Boolean) : CreateWidgetUiEvent
    data class UpdateMarriageStatusFilterEvent(val marriageStatusFilterEvent: Widget.MarriageStatusFilter) :
        CreateWidgetUiEvent

    data class UpdateEducationFilterEvent(val filter: Widget.EducationFilter) :
        CreateWidgetUiEvent

    data class UpdateOccupationFilterEvent(val occupationFilter: Widget.OccupationFilter) :
        CreateWidgetUiEvent
}