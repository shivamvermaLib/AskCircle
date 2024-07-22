package com.ask.widget

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface WidgetDao {
    @Transaction
    @Query("select *, id in (select widgetId from `user-widget-bookmark` where userId = :currentUserId) AS isBookmarked from widgets order by createdAt desc")
    fun getWidgets(currentUserId: String): PagingSource<Int, WidgetWithOptionsAndVotesForTargetAudience>

    @Transaction
    @Query("select *, id in (select widgetId from `user-widget-bookmark` where userId = :userId) AS isBookmarked from widgets where creatorId = :userId order by createdAt desc")
    fun getUserWidgets(userId: String): PagingSource<Int, WidgetWithOptionsAndVotesForTargetAudience>

    @Transaction
    @Query(
        "SELECT *, id in (select widgetId from `user-widget-bookmark` where userId = :userId) AS isBookmarked FROM widgets\n" +
            "LEFT JOIN (\n" +
            "    SELECT widgetId, SUM(vote_count) AS total_votes\n" +
            "    FROM (\n" +
            "        SELECT optionid, COUNT(*) AS vote_count\n" +
            "        FROM `widget-option-votes`\n" +
            "        GROUP BY optionid\n" +
            "    ) AS option_votes\n" +
            "    LEFT JOIN `widgets-options` ON option_votes.optionid = `widgets-options`.id\n" +
            "    GROUP BY widgetId\n" +
            ") AS votes ON widgets.id = votes.widgetId\n" +
            "ORDER BY votes.total_votes DESC"
    )
    fun getTrendingWidgets(userId: String): PagingSource<Int, WidgetWithOptionsAndVotesForTargetAudience>

    @Upsert
    suspend fun insertWidget(
        widget: Widget,
        targetAudienceGender: Widget.TargetAudienceGender,
        targetAudienceAgeRange: Widget.TargetAudienceAgeRange,
        targetAudienceLocation: List<Widget.TargetAudienceLocation>,
        options: List<Widget.Option>,
        voteList: List<Widget.Option.Vote>
    )

    @Upsert
    suspend fun insertWidgets(
        widgets: List<Widget>,
        targetAudienceGender: List<Widget.TargetAudienceGender>,
        targetAudienceAgeRange: List<Widget.TargetAudienceAgeRange>,
        targetAudienceLocation: List<Widget.TargetAudienceLocation>,
        options: List<Widget.Option>,
        voteList: List<Widget.Option.Vote>
    )

    @Upsert
    suspend fun insertVotes(voteList: List<Widget.Option.Vote>)

    @Delete
    suspend fun deleteVote(vote: Widget.Option.Vote)

    @Transaction
    @Query("select *, id in (select widgetId from `user-widget-bookmark` where userId = :currentUserId) AS isBookmarked from widgets where id = :id")
    suspend fun getWidgetById(
        id: String,
        currentUserId: String
    ): WidgetWithOptionsAndVotesForTargetAudience?

    @Delete
    suspend fun deleteWidget(widget: Widget)

    @Query("DELETE FROM widgets")
    suspend fun clearData()

}