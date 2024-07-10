package com.ask.app.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.ask.app.data.models.Widget
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {
    @Transaction
    @Query("select * from widgets order by createdAt desc")
    fun getWidgets(): Flow<List<WidgetWithOptionsAndVotesForTargetAudience>>

    @Transaction
    @Query("select * from widgets where creatorId = :userId order by createdAt desc")
    fun getUserWidgets(userId: String): Flow<List<WidgetWithOptionsAndVotesForTargetAudience>>

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

    @Transaction
    @Query("select * from widgets where id = :id")
    suspend fun getWidgetById(id: String): WidgetWithOptionsAndVotesForTargetAudience?

    @Delete
    suspend fun deleteWidget(widget: Widget)

}