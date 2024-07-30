package com.ask.widget

import com.ask.analytics.AnalyticsLogger
import com.ask.category.CategoryRepository
import com.ask.core.AppSharedPreference
import com.ask.core.DISPATCHER_DEFAULT
import com.ask.core.RemoteConfigRepository
import com.ask.core.UpdatedTime
import com.ask.country.CountryRepository
import com.ask.user.UserRepository
import com.ask.user.generateCombinationsForUsers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
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
    @Named("db_version") private val dbVersion: Int,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        isConnected: Boolean = true,
        preloadImages: suspend (List<String>) -> Unit,
        onProgress: (Float) -> Unit,
    ): Boolean = withContext(dispatcher) {
        if (!isConnected) {
            if (userRepository.getCurrentUserOptional() == null) {
                throw Exception("User not signed in")
            }
            return@withContext false
        }
        onProgress(0.2f)
        remoteConfigRepository.fetchInit()
        val refreshCountServer = remoteConfigRepository.refreshCountServer()
        val refreshCountLocal = sharedPreference.getRefreshCount()
        var refreshNeeded = false
        val lastUpdatedTime = sharedPreference.getUpdatedTime()
        val lastDbVersion = sharedPreference.getDbVersion()
        if (userRepository.getCurrentUserOptional() == null) {
            refreshNeeded = true
        } else if (refreshCountServer != refreshCountLocal || lastDbVersion != dbVersion) {
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
        onProgress(0.38f)
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
                    onProgress(0.56f)
                    widgetRepository.syncWidgetsFromServer(
                        userRepository.getCurrentUserId(),
                        lastUpdatedTime,
                        list,
                        {
                            userRepository.getUserDetailList(it, true, preloadImages)
                        },
                        preloadImages
                    ).also {
                        onProgress(0.8f)
                        countryRepository.syncCountries()
                        categoryRepository.syncCategories()
                    }
                }
            }
            onProgress(0.9f)
            val duration = System.currentTimeMillis() - time
            println("Duration for Sync: $duration")
            analyticsLogger.syncUsersAndWidgetsEventDuration(duration)
            sharedPreference.setRefreshCount(refreshCountServer)
            sharedPreference.setDbVersion(dbVersion)
            sharedPreference.setUpdatedTime(
                UpdatedTime(
                    widgetTime = System.currentTimeMillis(),
                    voteTime = System.currentTimeMillis(),
                    profileTime = System.currentTimeMillis()
                )
            )
            onProgress(1f)
            return@withContext sendNotification
        } else {
            onProgress(1f)
            return@withContext false
        }
    }
}

