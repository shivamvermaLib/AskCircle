package com.ask.widget

import com.ask.analytics.AnalyticsLogger
import com.ask.core.AppSharedPreference
import com.ask.core.UpdatedTime
import com.ask.country.CountryRepository
import com.ask.user.UserRepository
import com.ask.user.generateCombinationsForUsers
import javax.inject.Inject

class SyncUsersAndWidgetsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
    private val countryRepository: CountryRepository,
    private val analyticsLogger: AnalyticsLogger,
    private val sharedPreference: AppSharedPreference
) {

    suspend operator fun invoke() {
        if (widgetRepository.doesSyncRequired(sharedPreference.getUpdatedTime())) {
            val time = System.currentTimeMillis()
            userRepository.createUser().let { userWithLocation ->
                generateCombinationsForUsers(
                    userWithLocation.user.gender,
                    userWithLocation.user.age,
                    userWithLocation.userLocation,
                    userWithLocation.user.id
                ).let {
                    widgetRepository.syncWidgetsFromServer(
                        it
                    ) { userId ->
                        userRepository.getUser(userId)!!
                    }
                    countryRepository.syncCountries()
                }
            }
            val duration = System.currentTimeMillis() - time
            analyticsLogger.syncUsersAndWidgetsEventDuration(duration)
            sharedPreference.setUpdatedTime(
                UpdatedTime(
                    widgetTime = System.currentTimeMillis(),
                    voteTime = System.currentTimeMillis(),
                    profileTime = System.currentTimeMillis()
                )
            )
        } else {
            println("Sync not required")
        }
    }
}