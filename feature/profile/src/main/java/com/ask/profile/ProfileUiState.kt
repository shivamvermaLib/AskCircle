package com.ask.profile

import com.ask.category.CategoryWithSubCategory
import com.ask.common.MAX_AGE_RANGE
import com.ask.common.MIN_AGE_RANGE
import com.ask.core.EMPTY
import com.ask.country.Country
import com.ask.user.Gender
import com.ask.user.User

data class ProfileUiState(
    val name: String = EMPTY,
    val nameError: Int = -1,
    val email: String = EMPTY,
    val emailError: Int = -1,
    val profilePic: String? = null,
    val age: Int? = null,
    val gender: Gender? = null,
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