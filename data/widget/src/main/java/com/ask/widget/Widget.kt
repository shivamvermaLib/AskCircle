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
import com.ask.core.toTimeAgo
import com.ask.user.USER_ID
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

    @Serializable
    @Entity(
        tableName = TABLE_WIDGETS_COMMENTS,
        foreignKeys = [
            ForeignKey(
                entity = Widget::class,
                parentColumns = [ID],
                childColumns = [WIDGET_ID],
                onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                entity = User::class,
                parentColumns = [ID],
                childColumns = [USER_ID],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index(value = [WIDGET_ID]), Index(value = [USER_ID])]
    )
    data class WidgetComment(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val widgetId: String = EMPTY,
        val userId: String = EMPTY,
        val comment: String = EMPTY,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )
}

data class WidgetWithOptionsAndVoteCountAndCommentCount(
    @Embedded val widget: Widget,
    val options: List<OptionWithVoteCount>,
    val commentCount: Int,
    val lastVotedAt: Long?,
    @Relation(
        parentColumn = CREATOR_ID,
        entityColumn = ID
    )
    @get:Exclude
    val user: User,
    @get:Exclude
    var isBookmarked: Boolean,
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
    val widgetTotalVotes = options.map { it.voteCount }.reduce { acc, i -> acc + i }

    @get:Exclude
    @Ignore
    val votesCountFormat = widgetTotalVotes.prettyCount()

    @get:Exclude
    @Ignore
    val lastVotedAtFormat =
        lastVotedAt?.let {
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


    fun setupData(
        userId: String,
        showAds: Boolean,
        lastVotedOption: String
    ): WidgetWithOptionsAndVoteCountAndCommentCount {
        return this.copy(
            options = options.map { optionWithVotes ->
                optionWithVotes.apply {
                    votesPercent =
                        if (voteCount > 0 && widgetTotalVotes > 0)
                            (voteCount.toFloat() / widgetTotalVotes.toFloat()) * 100
                        else 0f
                    votesPercentFormat = votesPercent.toPercentage()
                }
            }
        ).apply {
            showVotes = options.any { it.voteCount > 0 }
                && (widget.widgetResult == Widget.WidgetResult.ALWAYS ||
                (widget.widgetResult == Widget.WidgetResult.AFTER_VOTE && options.any { it.didUserVote })
                || (widget.widgetResult == Widget.WidgetResult.TIME_END && isWidgetEnd)
                )

            isCreatorOfTheWidget = userId == widget.creatorId
            showAdMob = showAds
            lastVotedAtOptional = lastVotedOption
        }
    }

    @Serializable
    data class OptionWithVoteCount(
        @Embedded val option: Widget.Option,
        val voteCount: Int,
        val didUserVote: Boolean
    ) {
        @get:Exclude
        @Ignore
        var votesPercent: Float = 0f

        @get:Exclude
        @Ignore
        var votesPercentFormat: String = EMPTY
    }
}