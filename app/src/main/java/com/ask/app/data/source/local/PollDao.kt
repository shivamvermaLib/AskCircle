package com.ask.app.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ask.app.data.models.Poll
import com.ask.app.data.models.PollWithOptionsAndVotesForTargetAudience

@Dao
interface PollDao {
    @Transaction
    @Query("select * from polls")
    suspend fun getPolls(): List<PollWithOptionsAndVotesForTargetAudience>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoll(
        poll: Poll,
        options: List<Poll.Option>,
        targetAudience: Poll.TargetAudience,
        votes: List<Poll.Option.Vote>
    )

    @Transaction
    @Query("select * from polls where id = :id")
    suspend fun getPollById(id: String): PollWithOptionsAndVotesForTargetAudience?

    @Delete
    suspend fun deletePoll(poll: Poll)

    @Delete
    suspend fun deletePollOptions(vararg options: Poll.Option)

    @Delete
    suspend fun deletePollTargetAudience(vararg targetAudience: Poll.TargetAudience)

    @Delete
    suspend fun deletePollOptionVotes(vararg vote: Poll.Option.Vote)

}