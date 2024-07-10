package com.ask.app.domain

import com.ask.app.data.models.generateCombinationsForUsers
import com.ask.app.data.repository.CountryRepository
import com.ask.app.data.repository.UserRepository
import com.ask.app.data.repository.WidgetRepository
import javax.inject.Inject

class SyncUsersAndWidgetsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
    private val countryRepository: CountryRepository,
) {

    suspend operator fun invoke(checkUser: Boolean = false) {
        if (checkUser && userRepository.getCurrentUserOptional() != null) return
        userRepository.createUser().let { userWithLocation ->
            generateCombinationsForUsers(
                userWithLocation.user.gender,
                userWithLocation.user.age,
                userWithLocation.userLocation,
                userWithLocation.user.id
            ).let {
                widgetRepository.syncWidgetsFromServer(it) { userId ->
                    userRepository.getUser(userId)!!
                }
                countryRepository.syncCountries()
            }
        }
    }
}