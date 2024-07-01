package com.ask.app.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "",
    val email: String? = null,
    val name: String = "Anonymous User",
    val bio: String? = null,
    val profilePic: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    @Entity(tableName = "user-search-fields")
    data class UserSearchFields(
        @PrimaryKey val id: String = "",
        val userId: String = "",
        val age: Int? = null,
        val gender: Poll.TargetAudience.Gender? = null,
        val location: Poll.TargetAudience.Location? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )
}

fun Int?.orEmpty(addPrefixUnderScore: Boolean = true): String {
    return this?.let {
        when (addPrefixUnderScore) {
            true -> "_$it"
            else -> it.toString()
        }
    } ?: ""
}

data class UserWithSearchFields(
    @Embedded val user: User,
    @Relation(
        entityColumn = "userId",
        parentColumn = "id",
        entity = User.UserSearchFields::class
    )
    val userSearchFields: User.UserSearchFields
)


fun generateCombinations(
    location: Poll.TargetAudience.Location?,
    gender: Poll.TargetAudience.Gender?,
    age: Int?,
    ageRange: Poll.TargetAudience.AgeRange?
): List<String> {
    val combinations = mutableListOf("public")

    val country = location?.country?.lowercase()
    val state = location?.state?.lowercase()
    val city = location?.city?.lowercase()
    val genderName = gender?.name?.lowercase()

    // Add single field combinations
    country?.let { combinations.add(it) }
    state?.let { combinations.add(it) }
    city?.let { combinations.add(it) }
    genderName?.let { combinations.add(it) }
    age?.let { combinations.add(it.toString()) }

    val ageValues = when (ageRange) {
        null -> when (age) {
            null -> listOf()
            else -> listOf(age)
        }

        else -> (ageRange.min..ageRange.max).toList()
    }
    ageValues.forEach { combinations.add(it.toString()) }

    val parts = listOfNotNull(
        country,
        state,
        city,
        genderName
    )

    // Generate all non-empty subsets of the parts list
    for (i in 1 until (1 shl parts.size)) {
        val combination = mutableListOf<String>()
        for (j in parts.indices) {
            if ((i and (1 shl j)) != 0) {
                combination.add(parts[j])
            }
        }
        combinations.add(combination.joinToString("_"))

        // Add combinations with age values
        ageValues.forEach { combinations.add((combination + it.toString()).joinToString("_")) }
    }
    return combinations.distinct()
}
