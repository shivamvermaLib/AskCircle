package com.ask.app.ui.screens.home.profile

import com.ask.app.data.models.Country
import com.ask.app.data.models.Gender

data class ProfileUiState(
    val name: String = "",
    val nameError: String = "",
    val email: String = "",
    val emailError: String = "",
    val profilePic: String? = null,
    val age: Int? = null,
    val gender: Gender? = null,
    val country: String = "",
    val countries: List<Country> = emptyList(),
    val allowUpdate: Boolean = false,
    val profileLoading: Boolean = false,
    val error: String? = null
)