package com.ask.user

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.ask.core.ALL
import com.ask.core.EMPTY
import com.ask.core.HAS_EMAIL
import com.ask.core.ID
import com.ask.core.UNDERSCORE
import com.ask.core.toSearchNeededField
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
@Entity(tableName = TABLE_USERS)
@TypeConverters(StringListConverter::class)
data class User(
    @PrimaryKey val id: String = EMPTY,
    val email: String? = null,
    val name: String = ANONYMOUS_USER,
    val bio: String? = null,
    val profilePic: String? = null,
    val age: Int? = null,
    val gender: Gender? = null,
    val marriageStatus: MarriageStatus? = null,
    val education: Education? = null,
    val occupation: Occupation? = null,
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

    @Serializable
    @Entity(
        tableName = TABLE_USER_CATEGORY,
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
    data class UserCategory(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val userId: String = EMPTY,
        val category: String = EMPTY,
        val subCategory: String = EMPTY,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

    @Serializable
    @Entity(
        tableName = TABLE_USER_WIDGET_BOOKMARK,
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
    data class UserWidgetBookmarks(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        val userId: String = EMPTY,
        val widgetId: String = EMPTY,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )
}

data class UserWithLocationCategory(
    @Embedded
    val user: User,
    @Relation(
        parentColumn = ID,
        entityColumn = USER_ID,
        entity = User.UserLocation::class
    )
    val userLocation: User.UserLocation,
    @Relation(
        parentColumn = ID,
        entityColumn = USER_ID,
        entity = User.UserCategory::class
    )
    val userCategories: List<User.UserCategory>,
    @Relation(
        parentColumn = ID,
        entityColumn = USER_ID,
        entity = User.UserWidgetBookmarks::class
    )
    val userWidgetBookmarks: List<User.UserWidgetBookmarks>
)

fun generateCombinationsForUsers(
    user: User,
    location: User.UserLocation,
    userId: String,
    userCategories: List<User.UserCategory>,
    hasEmail: Boolean,
): Set<String> {
    val country = location.country.toSearchNeededField()
    val state = location.state.toSearchNeededField()
    val city = location.city.toSearchNeededField()
    val genderName = user.gender?.name.toSearchNeededField()
    val marriageStatusName = user.marriageStatus?.name.toSearchNeededField()
    val educationName = user.education?.name.toSearchNeededField()
    val occupationName = user.occupation?.name.toSearchNeededField()

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
    if (user.age != null && user.age > 0) {
        val ageStr = user.age.toString()
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
    if (ageCombinations.isEmpty()) {
        genderName?.let { genderCombination.add(it) }
        genderCombination.add(ALL)
    } else {
        ageCombinations.forEach { locationCombination ->
            genderName?.let { genderCombination.add("${locationCombination}$UNDERSCORE$genderName") }
            genderCombination.add("${locationCombination}$UNDERSCORE$ALL")
        }
    }

    val marriageCombination = mutableSetOf<String>()
    if (marriageStatusName != null) {
        if (genderCombination.isEmpty()) {
            marriageCombination.add(marriageStatusName)
            marriageCombination.add(ALL)
        } else {
            genderCombination.forEach { locationCombination ->
                marriageCombination.add("${locationCombination}$UNDERSCORE$marriageStatusName")
            }
        }
    }

    val educationCombination = mutableSetOf<String>()
    if (educationName != null) {
        if (marriageCombination.isEmpty()) {
            educationCombination.add(educationName)
            educationCombination.add(ALL)
        } else {
            marriageCombination.forEach { locationCombination ->
                educationCombination.add("${locationCombination}$UNDERSCORE$educationName")
            }
        }
    }

    val occupationCombination = mutableSetOf<String>()
    if (occupationName != null) {
        if (marriageCombination.isEmpty()) {
            occupationCombination.add(occupationName)
            occupationCombination.add(ALL)
        } else {
            marriageCombination.forEach { locationCombination ->
                occupationCombination.add("${locationCombination}$UNDERSCORE$occupationName")
            }
        }
    }

    val categoriesCombination = mutableSetOf<String>()
    if (userCategories.isEmpty()) {
        categoriesCombination.addAll(occupationCombination)
    } else {
        userCategories.forEach { userCategory ->
            val category = userCategory.category.toSearchNeededField()
            val subCategory = userCategory.subCategory.toSearchNeededField()
            occupationCombination.forEach {
                if (subCategory != null && category != null) {
                    categoriesCombination.add("$it$UNDERSCORE${category}$UNDERSCORE${subCategory}")
                    categoriesCombination.add("$it$UNDERSCORE${category}")
                } else if (category != null) {
                    categoriesCombination.add("$it$UNDERSCORE${category}")
                }
            }
            if (subCategory != null && category != null) {
                categoriesCombination.add("$ALL$UNDERSCORE${category}$UNDERSCORE${subCategory}")
                categoriesCombination.add("$ALL$UNDERSCORE${category}")
            } else if (category != null) {
                categoriesCombination.add("$ALL$UNDERSCORE${category}")
            }
        }
    }

    val hasEmailCombination = mutableSetOf<String>()
    if (hasEmail) {
        categoriesCombination.forEach {
            hasEmailCombination.add("$it$UNDERSCORE$HAS_EMAIL")
        }
        hasEmailCombination.add("$ALL$UNDERSCORE$HAS_EMAIL")
    } else {
        hasEmailCombination.addAll(categoriesCombination)
    }
    hasEmailCombination.add(ALL)
    hasEmailCombination.add(userId)

    return hasEmailCombination
}

class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return Json.decodeFromString<List<String>>(value)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Json.encodeToString(list)
    }
}