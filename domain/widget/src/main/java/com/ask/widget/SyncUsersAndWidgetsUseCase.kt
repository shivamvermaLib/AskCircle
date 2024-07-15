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

    suspend operator fun invoke(
        isConnected: Boolean = true,
        preloadImages: suspend (List<String>) -> Unit
    ): Boolean {
        if (!isConnected) {
            if (userRepository.getCurrentUserOptional() == null) {
                throw Exception("User not signed in")
            }
            return false
        }

        val lastUpdatedTime = sharedPreference.getUpdatedTime()
        if (widgetRepository.doesSyncRequired(lastUpdatedTime)) {
            val time = System.currentTimeMillis()

            val sendNotification = userRepository.createUser().let { userWithLocation ->
                generateCombinationsForUsers(
                    userWithLocation.user.gender,
                    userWithLocation.user.age,
                    userWithLocation.userLocation,
                    userWithLocation.user.id
                ).let { list ->
                    widgetRepository.syncWidgetsFromServer(
                        userRepository.getCurrentUserId(),
                        lastUpdatedTime,
                        list,
                        { userId ->
                            userRepository.getUser(userId)
                                .also {
                                    if (it.profilePic.isNullOrBlank().not()) {
                                        preloadImages(listOf(it.profilePic!!))
                                    }
                                }
                        },
                        preloadImages
                    ).also {
                        countryRepository.syncCountries()
                    }
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
            return sendNotification
        } else {
            println("Sync not required")
            return false
        }
    }
}