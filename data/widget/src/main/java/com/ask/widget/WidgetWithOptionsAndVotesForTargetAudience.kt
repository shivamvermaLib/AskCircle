package com.ask.widget

import android.text.format.DateUtils
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.ask.core.ALL
import com.ask.core.EMPTY
import com.ask.core.ID
import com.ask.core.toSearchNeededField
import com.ask.user.User
import com.google.firebase.database.Exclude
import kotlinx.serialization.Serializable

@Serializable
data class WidgetWithOptionsAndVotesForTargetAudience(
    @Embedded val widget: Widget,
    @Relation(
        parentColumn = ID,
        entityColumn = WIDGET_ID,
        entity = Widget.Option::class
    ) val options: List<OptionWithVotes>,
    @Relation(
        parentColumn = ID,
        entityColumn = WIDGET_ID,
        entity = Widget.TargetAudienceGender::class
    )
    val targetAudienceGender: Widget.TargetAudienceGender,
    @Relation(
        parentColumn = ID,
        entityColumn = WIDGET_ID,
        entity = Widget.TargetAudienceLocation::class
    )
    val targetAudienceLocations: List<Widget.TargetAudienceLocation>,
    @Relation(
        parentColumn = ID,
        entityColumn = WIDGET_ID,
        entity = Widget.TargetAudienceAgeRange::class
    )
    val targetAudienceAgeRange: Widget.TargetAudienceAgeRange,
    @Relation(
        parentColumn = CREATOR_ID,
        entityColumn = ID
    )
    @get:Exclude
    val user: User,
    @Relation(
        parentColumn = ID,
        entityColumn = WIDGET_ID,
        entity = Widget.WidgetCategory::class
    )
    val categories: List<Widget.WidgetCategory>,
    @get:Exclude
    var isBookmarked: Boolean,
    @Relation(
        parentColumn = ID,
        entityColumn = WIDGET_ID,
        entity = Widget.WidgetComment::class
    )
    val comments: List<Widget.WidgetComment>
) {
    @get:Exclude
    @Ignore
    var isCreatorOfTheWidget: Boolean = false

    @get:Exclude
    @Ignore
    var showVotes: Boolean = false

    @get:Exclude
    @Ignore
    val isImageOnly = options.all { it.option.imageUrl != null && it.option.text == null }

    @get:Exclude
    @Ignore
    val isTextOnly = options.all { it.option.text != null && it.option.imageUrl == null }

    @get:Exclude
    @Ignore
    val widgetTotalVotes = options.map { it.votes }.flatten().size

    @get:Exclude
    @Ignore
    val votesCountFormat = widgetTotalVotes.prettyCount()

    @get:Exclude
    @Ignore
    val lastVotedAtFormat =
        options.map { it.votes }.flatten().maxByOrNull { it.votedAt }?.votedAt?.let {
            DateUtils.getRelativeTimeSpanString(
                it,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString().uppercase()
        }

    @get:Exclude
    @Ignore
    var lastVotedAtOptional: String = ""

    @get:Exclude
    @Ignore
    var showAdMob = false

    @get:Exclude
    @Ignore
    var isLastVotedWidget = false

    @get:Exclude
    @Ignore
    val isAllowedVoting: Boolean =
        widget.startAt < System.currentTimeMillis() && (widget.endAt == null || widget.endAt > System.currentTimeMillis())

    @get:Exclude
    @Ignore
    val isWidgetNotStarted: Boolean = widget.startAt > System.currentTimeMillis()

    @get:Exclude
    @Ignore
    val isWidgetEnd: Boolean = widget.endAt != null && widget.endAt < System.currentTimeMillis()

    @Serializable
    data class OptionWithVotes(
        @Embedded val option: Widget.Option,
        @Relation(
            parentColumn = ID,
            entityColumn = OPTION_ID
        ) val votes: List<Widget.Option.Vote>,
    ) {
        @get:Exclude
        @Ignore
        var didUserVoted: Boolean = false

        @get:Exclude
        @Ignore
        val totalVotes = votes.size

        @get:Exclude
        @Ignore
        var votesPercent: Float = 0f

        @get:Exclude
        @Ignore
        var votesPercentFormat: String = EMPTY
    }

    fun setupData(
        userId: String,
        showAds: Boolean,
        lastVotedOption: String
    ): WidgetWithOptionsAndVotesForTargetAudience {
        return this.copy(
            options = options.map { optionWithVotes ->
                optionWithVotes.apply {
                    didUserVoted = userId in optionWithVotes.votes.map { it.userId }
                    votesPercent =
                        if (totalVotes > 0 && widgetTotalVotes > 0)
                            (totalVotes.toFloat() / widgetTotalVotes.toFloat()) * 100
                        else 0f
                    votesPercentFormat = votesPercent.toPercentage()
                }
            }
        ).apply {
            showVotes = options.any { it.votes.isNotEmpty() }
                && (widget.widgetResult == Widget.WidgetResult.ALWAYS ||
                (widget.widgetResult == Widget.WidgetResult.AFTER_VOTE && options.any { it.didUserVoted })
                || (widget.widgetResult == Widget.WidgetResult.TIME_END && isWidgetEnd)
                )

            isCreatorOfTheWidget = userId == widget.creatorId
            showAdMob = showAds
            lastVotedAtOptional = lastVotedOption
        }
    }

    fun generateCombinationsForWidget(): List<String> {
        val locationCombinations = mutableListOf<String>()

        val genderName = targetAudienceGender.gender.name.toSearchNeededField()
        val marriageStatusName =
            targetAudienceGender.marriageStatusFilter.name.toSearchNeededField()
        val educationName = targetAudienceGender.educationFilter.name.toSearchNeededField()
        val occupationName = targetAudienceGender.occupationFilter.name.toSearchNeededField()
        if (targetAudienceLocations.isNotEmpty()) {
            for (location in targetAudienceLocations) {
                val country = location.country.toSearchNeededField()
                val state = location.state.toSearchNeededField { country != null }
                val city = location.city.toSearchNeededField { state != null }

                locationCombinations.add(buildString {
                    if (country != null) {
                        append("country:$country")
                    } else {
                        append("country:$ALL")
                    }
                    if (state != null) {
                        append(",")
                        append("state:$state")
                    } else {
                        append(",")
                        append("state:$ALL")
                    }
                    if (city != null) {
                        append(",")
                        append("city:$city")
                    } else {
                        append(",")
                        append("city:$ALL")
                    }
                })
            }
        } else {
            locationCombinations.add("country:$ALL,state:$ALL,city:$ALL")
        }

        val ageRangeCombination = mutableListOf<String>()
        // Generate combinations for each age in the range
        for (age in targetAudienceAgeRange.min..targetAudienceAgeRange.max) {
            locationCombinations.forEach { locationCombination ->
                ageRangeCombination.add(buildString {
                    append(locationCombination)
                    append(",")
                    if (age > 0)
                        append("age:$age")
                    else
                        append("age:$ALL")
                })
            }
        }


        val genderCombination = mutableListOf<String>()

        ageRangeCombination.forEach {
            genderCombination.add(buildString {
                append(it)
                append(",")
                append("gender:$genderName")
            })
        }


        val marriageCombination = mutableSetOf<String>()
        if (marriageStatusName != null) {

            genderCombination.forEach { locationCombination ->
                marriageCombination.add(buildString {
                    append(locationCombination)
                    append(",")
                    append("marriage:$marriageStatusName")
                })
            }

        } else {
            marriageCombination.addAll(genderCombination)
        }

        val educationCombination = mutableSetOf<String>()
        if (educationName != null) {

            marriageCombination.forEach { locationCombination ->
                educationCombination.add(buildString {
                    append(locationCombination)
                    append(",")
                    append("education:$educationName")
                })
            }

        } else {
            educationCombination.addAll(marriageCombination)
        }

        val occupationCombination = mutableSetOf<String>()
        if (occupationName != null) {

            marriageCombination.forEach { locationCombination ->
                occupationCombination.add(buildString {
                    append(locationCombination)
                    append(",")
                    append("occupation:$occupationName")
                })
            }

        } else {
            occupationCombination.addAll(educationCombination)
        }

        val categoriesCombination = mutableListOf<String>()
        occupationCombination.forEach {
            if (categories.isNotEmpty()) {
                categories.forEach { widgetCategory ->
                    val category = widgetCategory.category.toSearchNeededField()
                    val subCategory = widgetCategory.subCategory.toSearchNeededField()


                    if (subCategory != null && category != null) {
                        categoriesCombination.add(buildString {
                            append(it)
                            append(",")
                            append("category:$category")
                            append(",")
                            append("subCategory:$subCategory")
                        })
                        categoriesCombination.add(buildString {
                            append(it)
                            append(",")
                            append("category:$category")
                        })
                    } else if (category != null) {
                        categoriesCombination.add(buildString {
                            append(it)
                            append(",")
                            append("category:$category")
                        })
                    }
                }
            } else {
                categoriesCombination.add(buildString {
                    append(it)
                    append(",")
                    append("category:$ALL")
                    append(",")
                    append("subCategory:$ALL")
                })
            }
        }


        val hasEmailCombinations = mutableListOf<String>()
        categoriesCombination.forEach {
            hasEmailCombinations.add(buildString {
                append(it)
                append(",")
                append("anonymous:${widget.allowAnonymous}")
            })
        }

        hasEmailCombinations.add("creator:${widget.creatorId}")
        return hasEmailCombinations.distinct()
    }

    fun toWidgetWithOptionsAndVoteCountAndCommentCount(): WidgetWithOptionsAndVoteCountAndCommentCount {
        return WidgetWithOptionsAndVoteCountAndCommentCount(
            widget = widget,
            options = options.map {
                WidgetWithOptionsAndVoteCountAndCommentCount.OptionWithVoteCount(
                    option = it.option,
                    voteCount = it.votes.size,
                    didUserVote = it.didUserVoted
                )
            },
            commentCount = comments.size,
            lastVotedAt = options.map { it.votes }.flatten().maxByOrNull { it.votedAt }?.votedAt,
            user = user,
            isBookmarked = isBookmarked,
        ).apply {
            this.isCreatorOfTheWidget = isCreatorOfTheWidget
            this.showVotes = showVotes
            this.showAdMob = showAdMob
            this.lastVotedAtOptional = lastVotedAtOptional
            this.isLastVotedWidget = isLastVotedWidget
        }
    }
}