package com.ask.app.ui.screens.home.profile

import com.ask.app.EMPTY
import com.ask.app.MAX_AGE_RANGE
import com.ask.app.MIN_AGE_RANGE
import com.ask.app.data.models.Country
import com.ask.app.data.models.Gender

data class ProfileUiState(
    val name: String = EMPTY,
    val nameError: String = EMPTY,
    val email: String = EMPTY,
    val emailError: String = EMPTY,
    val profilePic: String? = null,
    val age: Int? = null,
    val gender: Gender? = null,
    val country: String = EMPTY,
    val countries: List<Country> = emptyList(),
    val allowUpdate: Boolean = false,
    val profileLoading: Boolean = false,
    val error: String? = null,
    val minAgeRange: Int = MIN_AGE_RANGE,
    val maxAgeRange: Int = MAX_AGE_RANGE
)