package com.ask.profile

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.category.GetCategoryUseCase
import com.ask.common.BaseViewModel
import com.ask.common.GetAllBadWordsUseCase
import com.ask.common.GetCreateWidgetRemoteConfigUseCase
import com.ask.common.combine
import com.ask.common.googleLogin
import com.ask.core.EMPTY
import com.ask.country.GetCountryUseCase
import com.ask.user.GetCurrentProfileUseCase
import com.ask.user.GoogleLoginUseCase
import com.ask.user.User
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getCurrentProfileUseCase: GetCurrentProfileUseCase,
    getCountryUseCase: GetCountryUseCase,
    getCreateWidgetRemoteConfigUseCase: GetCreateWidgetRemoteConfigUseCase,
    getCategoryUseCase: GetCategoryUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    getAllBadWordsUseCase: GetAllBadWordsUseCase,
    analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {
    private val _badWordsListFlow =
        getAllBadWordsUseCase().map { it.map { badWord -> badWord.english } }

    private val _currentUserFlow = getCurrentProfileUseCase()
        .catch {
            it.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(it)
            _errorFlow.value = it.message
        }
    private val _userFlow = MutableStateFlow(User())
    private val _countriesFlow = getCountryUseCase()
    private val _ageRange = getCreateWidgetRemoteConfigUseCase()
    private val _categories = getCategoryUseCase()

    private val _userLocationCountryFlow = MutableStateFlow(EMPTY)
    private val _profileLoadingFlow = MutableStateFlow(false)
    private val _userCategoriesFlow = MutableStateFlow(listOf<User.UserCategory>())
    private val _googleLoginLoading = MutableStateFlow(false)
    private val _errorFlow = MutableStateFlow<String?>(null)

    val uiStateFlow = combine(
        _countriesFlow,
        _userFlow,
        _userLocationCountryFlow,
        _profileLoadingFlow,
        _errorFlow,
        _userCategoriesFlow,
        _categories,
        _googleLoginLoading,
        _badWordsListFlow
    ) { countries, user, country, profileLoading, error, userCategories, categories, googleLoginLoading, badWords ->
        val nameError =
            if (user.name.isBlank())
                R.string.name_is_required
            else if (badWords.any { user.name.lowercase().contains(it.lowercase()) })
                R.string.name_cannot_contain_bad_words
            else -1
        val emailError =
            if (user.email.isNullOrBlank()
                    .not() && isValidEmail(user.email!!).not()
            ) R.string.email_is_not_valid
            else -1
        val allowUpdate = nameError == -1 && emailError == -1
        ProfileUiState(
            user = user,
            nameError = nameError,
            emailError = emailError,
            country = country,
            countries = countries,
            allowUpdate = allowUpdate,
            profileLoading = profileLoading,
            error = error,
            minAgeRange = _ageRange.minAge,
            maxAgeRange = _ageRange.maxAge,
            userCategories = userCategories,
            categories = categories,
            googleLoginLoading = googleLoginLoading
        )
    }.catch {
        it.printStackTrace()
        _errorFlow.value = it.message
        FirebaseCrashlytics.getInstance().recordException(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    init {
        viewModelScope.launch {
            _currentUserFlow.collect { userWithLocationCategory ->
                _userFlow.value = userWithLocationCategory.user
                _userLocationCountryFlow.value =
                    userWithLocationCategory.userLocation.country ?: EMPTY
                _userCategoriesFlow.value = userWithLocationCategory.userCategories
            }
        }
    }

    fun setError(error: String?) {
        _errorFlow.value = error
    }

    private fun connectWithGoogle(context: Context) {
        safeApiCall({
            _googleLoginLoading.value = true
        }, {
            googleLogin(context)?.let { credential ->
                googleLoginUseCase(credential.idToken, false)
            }
            _googleLoginLoading.value = false
        }, {
            setError(it)
            _googleLoginLoading.value = false
        })
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.UpdateMaritalStatus -> _userFlow.update { it.copy(marriageStatus = event.status) }
            is ProfileUiEvent.UpdateName -> _userFlow.update { it.copy(name = event.name) }
            is ProfileUiEvent.ConnectWithGoogle -> connectWithGoogle(event.context)
            is ProfileUiEvent.UpdateAge -> _userFlow.update { it.copy(age = event.age) }
            is ProfileUiEvent.UpdateCategories -> _userCategoriesFlow.value = event.categories
            is ProfileUiEvent.UpdateCountry -> _userLocationCountryFlow.value = event.country
            is ProfileUiEvent.UpdateEmail -> _userFlow.update { it.copy(email = event.email) }
            is ProfileUiEvent.UpdateGender -> _userFlow.update { it.copy(gender = event.gender) }
            is ProfileUiEvent.UpdateProfilePic -> _userFlow.update { it.copy(profilePic = event.path) }
            is ProfileUiEvent.UpdateEducation -> _userFlow.update { it.copy(education = event.education) }
            is ProfileUiEvent.UpdateOccupation -> _userFlow.update { it.copy(occupation = event.occupation) }
        }
    }

}