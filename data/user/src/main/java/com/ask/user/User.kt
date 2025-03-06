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
import com.ask.core.ID
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
) {
    fun generateCombinationsForUsers(): Set<String> {
        val country = userLocation.country.toSearchNeededField()
        val state = userLocation.state.toSearchNeededField()
        val city = userLocation.city.toSearchNeededField()
        val genderName = user.gender?.name.toSearchNeededField()
        val marriageStatusName = user.marriageStatus?.name.toSearchNeededField()
        val educationName = user.education?.name.toSearchNeededField()
        val occupationName = user.occupation?.name.toSearchNeededField()

        // Generate location-based combinations
        val locationCombinations = mutableListOf<String>()
        if (country != null) {
            if (state != null) {
                if (city != null) {
                    locationCombinations.add(buildString {
                        append("country:$country")
                        append(",")
                        append("state:$state")
                        append(",")
                        append("city:$city")
                    })
                }
                locationCombinations.add(buildString {
                    append("country:$country")
                    append(",")
                    append("state:$state")
                    append(",")
                    append("city:$ALL")
                })
            }
            locationCombinations.add(buildString {
                append("country:$country")
                append(",")
                append("state:$ALL")
                append(",")
                append("city:$ALL")
            })
        }

        locationCombinations.add(buildString {
            append("country:$ALL")
            append(",")
            append("state:$ALL")
            append(",")
            append("city:$ALL")
        })
        locationCombinations.removeAll { it.isEmpty() }


        val ageCombinations = mutableListOf<String>()
        locationCombinations.forEach { locationCombination ->
            if (user.age != null && user.age > 0) {
                val ageStr = user.age.toString()
                ageCombinations.add(buildString {
                    append(locationCombination)
                    append(",")
                    append("age:$ageStr")
                })
            }
            ageCombinations.add(buildString {
                append(locationCombination)
                append(",")
                append("age:$ALL")
            })


        }

        ageCombinations.removeAll { it.isEmpty() }

        // Generate gender-based combinations
        val genderCombination = mutableSetOf<String>()

        ageCombinations.forEach { locationCombination ->
            genderName?.let {
                genderCombination.add(buildString {
                    append(locationCombination)
                    append(",")
                    append("gender:$genderName")
                })
            }
            genderCombination.add(buildString {
                append(locationCombination)
                append(",")
                append("gender:$ALL")
            })
        }


        genderCombination.removeAll { it.isEmpty() }

        val marriageCombination = mutableSetOf<String>()

        genderCombination.forEach { locationCombination ->
            marriageStatusName?.let {
                marriageCombination.add(buildString {
                    append(locationCombination)
                    append(",")
                    append("marriage:$marriageStatusName")
                }
                )
            }
            marriageCombination.add(buildString {

                append(locationCombination)
                append(",")
                append("marriage:$ALL")

            })
        }


        val educationCombination = mutableSetOf<String>()
        marriageCombination.forEach { locationCombination ->
            educationName?.let {
                educationCombination.add(buildString {
                    append(locationCombination)
                    append(",")
                    append("education:$educationName")
                })
            }
            educationCombination.add(buildString {

                append(locationCombination)
                append(",")
                append("education:$ALL")
            })
        }


        val occupationCombination = mutableSetOf<String>()

        marriageCombination.forEach { locationCombination ->
            occupationName?.let {
                occupationCombination.add(buildString {
                    append(locationCombination)
                    append(",")
                    append("occupation:$occupationName")
                })
            }
            occupationCombination.add(buildString {

                append(locationCombination)
                append(",")
                append("occupation:$ALL")
            })
        }


        val categoriesCombination = mutableSetOf<String>()

        occupationCombination.forEach {
            userCategories.forEach { userCategory ->
                val category = userCategory.category.toSearchNeededField()
                val subCategory = userCategory.subCategory.toSearchNeededField()

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
            categoriesCombination.add(buildString {
                append(it)
                append(",")
                append("category:$ALL")
                append(",")
                append("subCategory:$ALL")
            })
        }


        val hasEmailCombination = mutableSetOf<String>()
        categoriesCombination.forEach {
            if (user.email.isNullOrBlank()) {
                hasEmailCombination.add(buildString {
                    append(it)
                    append(",")
                    append("anonymous:${user.email.isNullOrBlank()}")
                })
            } else {
                hasEmailCombination.add(buildString {
                    append(it)
                    append(",")
                    append("anonymous:${user.email.isBlank()}")
                })
                hasEmailCombination.add(buildString {
                    append(it)
                    append(",")
                    append("anonymous:${user.email.isBlank().not()}")
                })
            }

        }
        hasEmailCombination.add(user.id)

        return hasEmailCombination
    }
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