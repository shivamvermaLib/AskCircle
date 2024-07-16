package com.ask.home.profile

import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.common.GetAgeRemoteConfigUseCase
import com.ask.common.combine
import com.ask.core.EMPTY
import com.ask.country.GetCountryUseCase
import com.ask.home.isValidEmail
import com.ask.user.Gender
import com.ask.user.GetCurrentProfileUseCase
import com.ask.user.UpdateProfileUseCase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    getCountryUseCase: GetCountryUseCase,
    getAgeRemoteConfigUseCase: GetAgeRemoteConfigUseCase,
    analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {

    private val _currentUserFlow = getCurrentProfileUseCase.invoke()
        .catch {
            it.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(it)
            _errorFlow.value = it.message
        }
    private val _countriesFlow = getCountryUseCase()
    private val _ageRange = getAgeRemoteConfigUseCase()

    private val _userNameFlow = MutableStateFlow(EMPTY)
    private val _userEmailFlow = MutableStateFlow(EMPTY)
    private val _userGenderFlow = MutableStateFlow<Gender?>(null)
    private val _userAgeFlow = MutableStateFlow<Int?>(null)
    private val _userLocationCountryFlow = MutableStateFlow(EMPTY)
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
        val nameError = if (name.isBlank()) "Name is required" else EMPTY
        val emailError =
            if (email.isNotBlank() && isValidEmail(email).not()) "Email is not valid" else EMPTY
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
            error = error,
            minAgeRange = _ageRange.min,
            maxAgeRange = _ageRange.max
        )
    }.catch {
        it.printStackTrace()
        _errorFlow.value = it.message
        FirebaseCrashlytics.getInstance().recordException(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    init {
        viewModelScope.launch {
            _currentUserFlow.collect {
                _userNameFlow.value = it.user.name
                _userGenderFlow.value = it.user.gender
                _userAgeFlow.value = it.user.age
                _userLocationCountryFlow.value = it.userLocation.country ?: EMPTY
                _userPicFlow.value = it.user.profilePic
                _userEmailFlow.value = it.user.email ?: EMPTY
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

    fun onUpdate(
        getExtension: (String) -> String?,
        getBytes: (String) -> ByteArray?,
        preloadImage: suspend (String) -> Unit
    ) {
        val profile = uiStateFlow.value
        safeApiCall({
            _profileLoadingFlow.value = true
        }, {
            updateProfileUseCase.invoke(
                profile.name,
                profile.email,
                profile.gender,
                profile.age,
                profile.profilePic,
                profile.country,
                getExtension,
                getBytes,
                preloadImage
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