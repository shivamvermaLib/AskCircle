package com.ask.user

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ask.core.ALL
import com.ask.core.EMPTY
import com.ask.core.ID
import com.ask.core.UNDERSCORE
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = TABLE_USERS)
data class User(
    @PrimaryKey val id: String = EMPTY,
    val email: String? = null,
    val name: String = ANONYMOUS_USER,
    val bio: String? = null,
    val profilePic: String? = null,
    val age: Int? = null,
    val gender: Gender? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    @Entity(
        tableName = TABLE_USER_LOCATIONS,
        foreignKeys = [
            ForeignKey(
                entity = User::class,
                parentColumns = [ID],
                childColumns = [USER_ID],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index(value = [USER_ID])]
    )
    data class UserLocation(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val userId: String = EMPTY,
        val country: String? = null,
        val state: String? = null,
        val city: String? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )
}

data class UserWithLocation(
    @Embedded
    val user: User,
    @Relation(
        parentColumn = ID,
        entityColumn = USER_ID,
        entity = User.UserLocation::class
    )
    val userLocation: User.UserLocation,
)

fun generateCombinationsForUsers(
    gender: Gender?,
    age: Int?,
    location: User.UserLocation,
    userId: String
): Set<String> {
    val country = location.country?.lowercase()
    val state = location.state?.lowercase()
    val city = location.city?.lowercase()
    val genderName = gender?.name?.lowercase()

    // Generate location-based combinations
    val locationCombinations = mutableListOf<String>()
    if (country != null) {
        if (state != null) {
            if (city != null) {
                locationCombinations.add("${country}$UNDERSCORE${state}$UNDERSCORE${city}")
            }
            locationCombinations.add("${country}$UNDERSCORE${state}")
        }
        locationCombinations.add(country)
    }

    // Generate combinations for each age in the range
    val ageCombinations = mutableListOf<String>()
    if (age != null && age > 0) {
        val ageStr = age.toString()
        if (locationCombinations.isEmpty()) {
            ageCombinations.add(ageStr)
        } else {
            locationCombinations.forEach { locationCombination ->
                ageCombinations.add("${locationCombination}$UNDERSCORE$ageStr")

            }
        }
    } else {
        ageCombinations.addAll(locationCombinations)
    }

    // Generate gender-based combinations
    val genderCombination = mutableSetOf<String>()
    if (genderName != null) {
        if (ageCombinations.isEmpty()) {
            genderCombination.add(genderName)
            genderCombination.add(ALL)
        } else {
            ageCombinations.forEach { locationCombination ->
                genderCombination.add("${locationCombination}$UNDERSCORE$genderName")
                genderCombination.add("${locationCombination}$UNDERSCORE$ALL")
            }

        }
    } else {
        if (ageCombinations.isEmpty()) {
            genderCombination.add(ALL)
        } else {
            ageCombinations.forEach { locationCombination ->
                genderCombination.add("${locationCombination}$UNDERSCORE$ALL")
            }

        }
    }
    genderCombination.add(ALL)
    genderCombination.add(userId)
    return genderCombination
}