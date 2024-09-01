package com.ask.profile

import android.content.Context
import com.ask.category.CategoryWithSubCategory
import com.ask.common.MAX_AGE_RANGE
import com.ask.common.MIN_AGE_RANGE
import com.ask.core.EMPTY
import com.ask.country.Country
import com.ask.user.Education
import com.ask.user.Gender
import com.ask.user.MarriageStatus
import com.ask.user.Occupation
import com.ask.user.User

data class ProfileUiState(
    val user: User = User(),
    val nameError: Int = -1,
    val emailError: Int = -1,
    val country: String = EMPTY,
    val countries: List<Country> = emptyList(),
    val allowUpdate: Boolean = false,
    val profileLoading: Boolean = false,
    val error: String? = null,
    val minAgeRange: Int = MIN_AGE_RANGE,
    val maxAgeRange: Int = MAX_AGE_RANGE,
    val categories: List<CategoryWithSubCategory> = emptyList(),
    val userCategories: List<User.UserCategory> = emptyList(),
    val googleLoginLoading: Boolean = false
)

sealed interface ProfileUiEvent {
    data class UpdateName(val name: String) : ProfileUiEvent
    data class UpdateMaritalStatus(val status: MarriageStatus) : ProfileUiEvent
    data class UpdateEmail(val email: String) : ProfileUiEvent
    data class UpdateGender(val gender: Gender) : ProfileUiEvent
    data class UpdateAge(val age: Int) : ProfileUiEvent
    data class UpdateCountry(val country: String) : ProfileUiEvent
    data class UpdateProfilePic(val path: String) : ProfileUiEvent
    data class UpdateCategories(val categories: List<User.UserCategory>) : ProfileUiEvent
    data class ConnectWithGoogle(val context: Context) : ProfileUiEvent
    data class UpdateEducation(val education: Education) : ProfileUiEvent
    data class UpdateOccupation(val occupation: Occupation) : ProfileUiEvent
}