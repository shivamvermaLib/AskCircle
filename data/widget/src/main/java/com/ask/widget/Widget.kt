package com.ask.widget

import android.text.format.DateUtils
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ask.core.EMPTY
import com.ask.core.ID
import com.ask.core.UNDERSCORE
import com.google.firebase.database.Exclude
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = TABLE_WIDGETS, foreignKeys = [
        ForeignKey(
            entity = com.ask.user.User::class,
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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {

    fun isCreatorOfTheWidget(userId: String): Boolean {
        return userId == creatorId
    }

    @get:Exclude
    @Ignore
    var startAtFormat: String = DateUtils.getRelativeTimeSpanString(
        startAt,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()

    enum class WidgetType { Poll, Quiz }

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
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

    enum class GenderFilter { ALL, MALE, FEMALE }
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
    val user: com.ask.user.User
) {
    @get:Exclude
    @Ignore
    var isCreatorOfTheWidget: Boolean = false

    @get:Exclude
    @Ignore
    var hasVotes: Boolean = false

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
}

data class WidgetId(
    val id: String = UUID.randomUUID().toString(),
    val widgetIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

fun generateCombinationsForWidget(
    gender: Widget.TargetAudienceGender,
    ageRange: Widget.TargetAudienceAgeRange,
    locations: List<Widget.TargetAudienceLocation>,
    userId: String
): List<String> {
    val locationCombinations = mutableListOf<String>()

    val genderName = gender.gender.name.lowercase()
    for (location in locations) {
        val country = location.country?.takeIf { it.isNotBlank() }?.lowercase()
        val state = location.state?.takeIf { it.isNotBlank() && country != null }?.lowercase()
        val city = location.city?.takeIf { it.isNotBlank() && state != null }?.lowercase()

        if (country != null) {
            if (state != null) {
                if (city != null) {
                    locationCombinations.add("${country}$UNDERSCORE${state}$UNDERSCORE${city}")
                } else {
                    locationCombinations.add("${country}$UNDERSCORE${state}")
                }
            } else {
                locationCombinations.add(country)
            }
        }
    }

    val ageRangeCombination = mutableListOf<String>()
    // Generate combinations for each age in the range
    for (age in ageRange.min..ageRange.max) {
        if (age > 0) {
            if (locationCombinations.isEmpty()) {
                ageRangeCombination.add("$age")
            } else {
                locationCombinations.forEach { locationCombination ->
                    ageRangeCombination.add("${locationCombination}$UNDERSCORE$age")
                }
            }
        } else {
            ageRangeCombination.addAll(locationCombinations)
        }
    }

    val genderCombination = mutableListOf<String>()
    if (ageRangeCombination.isEmpty()) {
        genderCombination.add(genderName)
    } else {
        ageRangeCombination.forEach {
            genderCombination.add("${it}$UNDERSCORE$genderName")
        }
    }
    genderCombination.add(userId)
    return genderCombination
}