package com.ask.widget

import android.text.format.DateUtils
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ask.core.ALL
import com.ask.core.EMPTY
import com.ask.core.ID
import com.ask.core.toSearchNeededField
import com.ask.core.toTimeAgo
import com.ask.user.User
import com.google.firebase.database.Exclude
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = TABLE_WIDGETS, foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = [ID],
            childColumns = [CREATOR_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = [CREATOR_ID])]
)
data class Widget(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val creatorId: String = EMPTY,
    val title: String = EMPTY,
    val description: String? = null,
    val widgetType: WidgetType = WidgetType.Poll,
    val startAt: Long = System.currentTimeMillis(),
    val endAt: Long? = null,
    val allowAnonymous: Boolean = true,
    val widgetResult: WidgetResult = WidgetResult.ALWAYS,
    val allowMultipleSelection: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {

    @get:Exclude
    @Ignore
    var startAtFormat: String = startAt.toTimeAgo()

    enum class WidgetType {
        Poll,
        Quiz
    }

    enum class WidgetResult {
        AFTER_VOTE, ALWAYS, TIME_END
    }

    @Serializable
    @Entity(
        tableName = TABLE_WIDGET_OPTIONS, foreignKeys = [
            ForeignKey(
                entity = Widget::class,
                parentColumns = [ID],
                childColumns = [WIDGET_ID],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index(value = [WIDGET_ID])]
    )
    data class Option(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val widgetId: String = EMPTY,
        val text: String? = null,
        val imageUrl: String? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    ) {

        @Serializable
        @Entity(
            tableName = TABLE_WIDGET_OPTION_VOTES, foreignKeys = [
                ForeignKey(
                    entity = Option::class,
                    parentColumns = [ID],
                    childColumns = [OPTION_ID],
                    onDelete = ForeignKey.CASCADE
                )
            ],
            indices = [Index(value = [OPTION_ID])]
        )
        data class Vote(
            @PrimaryKey val id: String = UUID.randomUUID().toString(),
            val userId: String = EMPTY,
            val optionId: String = EMPTY,
            val votedAt: Long = System.currentTimeMillis(),
            val createdAt: Long = System.currentTimeMillis(),
            val updatedAt: Long = System.currentTimeMillis()
        )
    }

    @Serializable
    @Entity(
        TABLE_TARGET_AUDIENCE_LOCATIONS, foreignKeys = [
            ForeignKey(
                entity = Widget::class,
                parentColumns = [ID],
                childColumns = [WIDGET_ID],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index(value = [WIDGET_ID])]
    )
    data class TargetAudienceLocation(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val widgetId: String = EMPTY,
        val country: String? = null,
        val state: String? = null,
        val city: String? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

    @Serializable
    @Entity(
        TABLE_TARGET_AUDIENCE_AGE_RANGES, foreignKeys = [
            ForeignKey(
                entity = Widget::class,
                parentColumns = [ID],
                childColumns = [WIDGET_ID],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index(value = [WIDGET_ID])]
    )
    data class TargetAudienceAgeRange(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val widgetId: String = EMPTY,
        val min: Int = 0,
        val max: Int = 0,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

    @Serializable
    @Entity(
        TABLE_TARGET_AUDIENCE_GENDERS, foreignKeys = [
            ForeignKey(
                entity = Widget::class,
                parentColumns = [ID],
                childColumns = [WIDGET_ID],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index(value = [WIDGET_ID])]
    )
    data class TargetAudienceGender(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val widgetId: String = EMPTY,
        val gender: GenderFilter = GenderFilter.ALL,
        val marriageStatusFilter: MarriageStatusFilter = MarriageStatusFilter.ALL,
        val educationFilter: EducationFilter = EducationFilter.ALL,
        val occupationFilter: OccupationFilter = OccupationFilter.ALL,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

    enum class GenderFilter { ALL, MALE, FEMALE }
    enum class MarriageStatusFilter { ALL, SINGLE, MARRIED, DIVORCED, WIDOW }
    enum class EducationFilter { ALL, PRIMARY, SECONDARY, HIGH_SCHOOL, UNDER_GRADUATE, POST_GRADUATE, DOC_OR_PHD }
    enum class OccupationFilter { ALL, EMPLOYED, SELF_EMPLOYED, UNEMPLOYED, RETIRED }

    @Serializable
    @Entity(
        tableName = TABLE_WIDGET_CATEGORIES, foreignKeys = [
            ForeignKey(
                entity = Widget::class,
                parentColumns = [ID],
                childColumns = [WIDGET_ID],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index(value = [WIDGET_ID])]
    )
    data class WidgetCategory(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val widgetId: String = EMPTY,
        val category: String = EMPTY,
        val subCategory: String = EMPTY,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )
}

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
    var isBookmarked: Boolean
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
        if(targetAudienceLocations.isNotEmpty()){
            for (location in targetAudienceLocations) {
                val country = location.country.toSearchNeededField()
                val state = location.state.toSearchNeededField { country != null }
                val city = location.city.toSearchNeededField { state != null }

                locationCombinations.add(buildString {
                    if (country != null) {
                        append("country:$country")
                    }else{
                        append("country:$ALL")
                    }
                    if (state != null) {
                        append(",")
                        append("state:$state")
                    }else{
                        append(",")
                        append("state:$ALL")
                    }
                    if (city != null) {
                        append(",")
                        append("city:$city")
                    }else{
                        append(",")
                        append("city:$ALL")
                    }
                })
            }
        }else{
            locationCombinations.add("country:$ALL,state:$ALL,city:$ALL")
        }

        val ageRangeCombination = mutableListOf<String>()
        // Generate combinations for each age in the range
        for (age in targetAudienceAgeRange.min..targetAudienceAgeRange.max) {
            locationCombinations.forEach { locationCombination ->
                ageRangeCombination.add(buildString {
                    append(locationCombination)
                    append(",")
                    if(age>0)
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
            if(categories.isNotEmpty()){
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
            }else{
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
}

data class WidgetId(
    val id: String = UUID.randomUUID().toString(),
    val widgetIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)