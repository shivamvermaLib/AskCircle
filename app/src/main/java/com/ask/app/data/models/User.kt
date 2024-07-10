package com.ask.app.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ask.app.ANONYMOUS_USER
import com.ask.app.ID
import com.ask.app.TABLE_USERS
import com.ask.app.TABLE_USER_LOCATIONS
import com.ask.app.TABLE_USER_WIDGETS
import com.ask.app.USER_ID
import com.ask.app.WIDGET_ID
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = TABLE_USERS)
data class User(
    @PrimaryKey val id: String = "",
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
        val userId: String = "",
        val country: String? = null,
        val state: String? = null,
        val city: String? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

    @Entity(
        tableName = TABLE_USER_WIDGETS,
        foreignKeys = [
            ForeignKey(
                entity = User::class,
                parentColumns = [ID],
                childColumns = [USER_ID],
                onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                entity = Widget::class,
                parentColumns = [ID],
                childColumns = [WIDGET_ID],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index(value = [USER_ID]), Index(value = [WIDGET_ID])]
    )
    data class UserWidget(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val userId: String = "",
        val widgetId: String = "",
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
//    @Relation(
//        parentColumn = ID,
//        entityColumn = USER_ID,
//        entity = User.UserWidget::class
//    )
//    val userWidgets: List<User.UserWidget>
)

fun generateCombinationsForUsers(
    gender: Gender?,
    age: Int?,
    location: User.UserLocation,
    userId: String
): List<String> {
    val country = location.country?.lowercase()
    val state = location.state?.lowercase()
    val city = location.city?.lowercase()
    val genderName = gender?.name?.lowercase()

    // Generate location-based combinations
    val locationCombinations = mutableListOf<String>()
    if (country != null) {
        if (state != null) {
            if (city != null) {
                locationCombinations.add("${country}_${state}_${city}")
            }
            locationCombinations.add("${country}_${state}")
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
                ageCombinations.add("${locationCombination}_$ageStr")

            }
        }
    } else {
        ageCombinations.addAll(locationCombinations)
    }

    // Generate gender-based combinations
    val genderCombination = mutableListOf<String>()
    if (genderName != null) {
        if (ageCombinations.isEmpty()) {
            genderCombination.add(genderName)
            genderCombination.add("all")
        } else {
            ageCombinations.forEach { locationCombination ->
                genderCombination.add("${locationCombination}_$genderName")
                genderCombination.add("${locationCombination}_all")
            }

        }
    } else {
        if (ageCombinations.isEmpty()) {
            genderCombination.add("all")
        } else {
            ageCombinations.forEach { locationCombination ->
                genderCombination.add("${locationCombination}_all")
            }

        }
    }
    genderCombination.add(userId)
    return genderCombination
}