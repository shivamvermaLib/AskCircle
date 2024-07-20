package com.ask.widget

import com.ask.analytics.AnalyticsLogger
import com.ask.category.CategoryRepository
import com.ask.core.AppSharedPreference
import com.ask.core.RemoteConfigRepository
import com.ask.core.UpdatedTime
import com.ask.country.CountryRepository
import com.ask.user.UserRepository
import com.ask.user.generateCombinationsForUsers
import javax.inject.Inject
import javax.inject.Named

class SyncUsersAndWidgetsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
    private val countryRepository: CountryRepository,
    private val analyticsLogger: AnalyticsLogger,
    private val sharedPreference: AppSharedPreference,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val categoryRepository: CategoryRepository,
    @Named("db_version") private val dbVersion: Int
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
        val refreshCountServer = remoteConfigRepository.refreshCountServer()
        val refreshCountLocal = sharedPreference.getRefreshCount()
        var refreshNeeded = false
        val lastUpdatedTime = sharedPreference.getUpdatedTime()
        val lastDbVersion = sharedPreference.getDbVersion()

        if (refreshCountServer != refreshCountLocal || lastDbVersion != dbVersion) {
            refreshNeeded = true
            widgetRepository.clearAll()
            userRepository.clearAll()
            countryRepository.clearAll()
            categoryRepository.clearAll()
            analyticsLogger.refreshTriggerEvent(
                refreshCountServer,
                refreshCountLocal,
                userRepository.getCurrentUserId()
            )
            sharedPreference.setDbVersion(dbVersion)
        } else if (widgetRepository.doesSyncRequired(lastUpdatedTime)) {
            refreshNeeded = true
        }
        if (refreshNeeded) {
            val time = System.currentTimeMillis()

            val sendNotification = userRepository.createUser().let { userWithLocation ->
                generateCombinationsForUsers(
                    userWithLocation.user.gender,
                    userWithLocation.user.age,
                    userWithLocation.userLocation,
                    userWithLocation.user.id,
                    userWithLocation.userCategories
                ).let { list ->
                    widgetRepository.syncWidgetsFromServer(
                        userRepository.getCurrentUserId(),
                        lastUpdatedTime,
                        list,
                        { userId ->
                            userRepository.getUser(userId,true, preloadImages)
                        },
                        preloadImages
                    ).also {
                        countryRepository.syncCountries()
                        categoryRepository.syncCategories()
                    }
                }
            }
            val duration = System.currentTimeMillis() - time
            analyticsLogger.syncUsersAndWidgetsEventDuration(duration)
            sharedPreference.setRefreshCount(refreshCountServer)
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