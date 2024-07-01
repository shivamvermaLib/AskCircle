package com.ask.app.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "polls")
data class Poll(
    @PrimaryKey val id: String = "",
    val creatorId: String = "",
    val groupId: String? = null,
    val title: String = "",
    val description: String? = null,
    val endAt: Long? = null,
    val startAt: Long = System.currentTimeMillis(),
    val resultsPublic: Boolean = true,
    val votingLimit: Int = 0,
    val isVotingClosed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {

    @Entity(tableName = "poll-options")
    data class Option(
        @PrimaryKey val id: String = "",
        val pollId: String = "",
        val text: String? = null,
        val imageUrl: String? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    ) {
        @Entity(tableName = "poll-option-votes")
        data class Vote(
            @PrimaryKey val id: String = "",
            val userId: String = "",
            val optionId: String = "",
            val votedAt: Long = System.currentTimeMillis(),
            val createdAt: Long = System.currentTimeMillis(),
            val updatedAt: Long = System.currentTimeMillis()
        )
    }

    @Entity("target-audiences")
    data class TargetAudience(
        @PrimaryKey val id: String = "",
        val pollId: String = "",
        val gender: Gender? = null,
        val location: Location? = null,
        val ageRange: AgeRange? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    ) {
        enum class Gender { MALE, FEMALE }

        @Serializable
        data class Location(
            val country: String? = null,
            val state: String? = null,
            val city: String? = null
        )

        @Serializable
        data class AgeRange(
            val min: Int = 0,
            val max: Int = 0
        )
    }
}

data class PollWithOptionsAndVotesForTargetAudience(
    @Embedded val poll: Poll,
    @Relation(
        parentColumn = "id",
        entityColumn = "pollId",
        entity = Poll.Option::class
    ) val options: List<OptionWithVotes>,
    @Relation(
        parentColumn = "id",
        entityColumn = "pollId",
        entity = Poll.TargetAudience::class
    )
    val targetAudience: Poll.TargetAudience,
    @Relation(
        parentColumn = "creatorId",
        entityColumn = "id"
    )
    val user: User
) {
    data class OptionWithVotes(
        @Embedded val option: Poll.Option,
        @Relation(
            parentColumn = "id",
            entityColumn = "optionId"
        ) val votes: List<Poll.Option.Vote>
    )
}

class PollConverters {
    @TypeConverter
    fun stringToLocation(locationString: String?): Poll.TargetAudience.Location? {
        return locationString?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun locationToString(location: Poll.TargetAudience.Location?): String? {
        return location?.let { Json.encodeToString(location) }
    }

    @TypeConverter
    fun stringToAgeRange(ageRangeString: String?): Poll.TargetAudience.AgeRange? {
        return ageRangeString?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun ageRangeToString(ageRange: Poll.TargetAudience.AgeRange?): String? {
        return ageRange?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun liatToString(map: List<String>): String {
        return Json.encodeToString(map)
    }

    @TypeConverter
    fun stringToList(string: String): List<String> {
        return Json.decodeFromString(string)
    }
}

