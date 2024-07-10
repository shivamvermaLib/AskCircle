package com.ask.app.ui.screens.home.profile

import androidx.lifecycle.viewModelScope
import com.ask.app.checkIfUrl
import com.ask.app.data.models.Gender
import com.ask.app.data.repository.CountryRepository
import com.ask.app.data.repository.UserRepository
import com.ask.app.ui.screens.utils.BaseViewModel
import com.ask.app.utils.combine
import com.ask.app.utils.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    countryRepository: CountryRepository
) : BaseViewModel() {

    private val _currentUserFlow = userRepository.getCurrentUserLive()
        .catch {
            it.printStackTrace()
            _errorFlow.value = it.message
        }
    private val _countriesFlow = countryRepository.getCountries()

    private val _userNameFlow = MutableStateFlow("")
    private val _userEmailFlow = MutableStateFlow("")
    private val _userGenderFlow = MutableStateFlow<Gender?>(null)
    private val _userAgeFlow = MutableStateFlow<Int?>(null)
    private val _userLocationCountryFlow = MutableStateFlow("")
    private val _userPicFlow = MutableStateFlow<String?>(null)
    private val _profileLoadingFlow = MutableStateFlow(false)

    private val _errorFlow = MutableStateFlow<String?>(null)

    val uiStateFlow = combine(
        _countriesFlow,
        _userNameFlow,
        _userEmailFlow,
        _userGenderFlow,
        _userAgeFlow,
        _userLocationCountryFlow,
        _userPicFlow,
        _profileLoadingFlow,
        _errorFlow
    ) { countries, name, email, gender, age, country, profilePic, profileLoading, error ->
        val nameError = if (name.isBlank()) "Name is required" else ""
        val emailError =
            if (email.isNotBlank() && isValidEmail(email).not()) "Email is not valid" else ""
        val allowUpdate = nameError.isBlank() && emailError.isBlank()
        ProfileUiState(
            name = name,
            nameError = nameError,
            email = email,
            emailError = emailError,
            gender = gender,
            age = age,
            country = country,
            profilePic = profilePic,
            countries = countries,
            allowUpdate = allowUpdate,
            profileLoading = profileLoading,
            error = error
        )
    }.catch {
        it.printStackTrace()
        _errorFlow.value = it.message
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    init {
        viewModelScope.launch {
            _currentUserFlow.collect {
                _userNameFlow.value = it.user.name
                _userGenderFlow.value = it.user.gender
                _userAgeFlow.value = it.user.age
                _userLocationCountryFlow.value = it.userLocation.country ?: ""
                _userPicFlow.value = it.user.profilePic
                _userEmailFlow.value = it.user.email ?: ""
            }
        }
    }

    fun setName(name: String) {
        _userNameFlow.value = name
    }

    fun setEmail(email: String) {
        _userEmailFlow.value = email
    }

    fun setGender(gender: Gender) {
        _userGenderFlow.value = gender
    }

    fun setAge(age: Int) {
        _userAgeFlow.value = age
    }

    fun setCountry(country: String) {
        _userLocationCountryFlow.value = country
    }

    fun setError(error: String?) {
        _errorFlow.value = error
    }

    fun onUpdate(getExtension: (String) -> String?, getBytes: (String) -> ByteArray?) {
        val profile = uiStateFlow.value
        safeApiCall({
            _profileLoadingFlow.value = true
        }, {
            val extension = profile.profilePic?.let { path ->
                path.takeIf { it.isNotBlank() && it.checkIfUrl().not() }
                    ?.let { getExtension(it) }
            }
            userRepository.updateUser(
                name = profile.name,
                email = profile.email,
                gender = profile.gender,
                age = profile.age,
                country = profile.country,
                profilePicExtension = extension,
                profileByteArray = profile.profilePic?.takeIf { it.checkIfUrl().not() }
                    ?.let { path -> getBytes(path) },
            )
            _profileLoadingFlow.value = false
        }, {
            _profileLoadingFlow.value = false
            _errorFlow.value = it
        })

    }

    fun onImageClick(path: String) {
        _userPicFlow.value = path
    }

}