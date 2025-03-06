package com.ask.core

import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RemoteConfigRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    suspend fun fetchInit(): Boolean {
        return if (isRemoteConfigActivate().not()) {
            init()
        } else {
            isRemoteConfigActivate()
        }
    }

    private suspend fun init(): Boolean = suspendCoroutine { cont ->
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                cont.resume(it)
            }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private suspend fun isRemoteConfigActivate(): Boolean = suspendCoroutine { cont ->
        remoteConfig.activate()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    fun fetchLiveInit(key: String) = callbackFlow {
        fetchInit()
        trySend(Unit)
        val listener = remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                if (configUpdate.updatedKeys.contains(key)) {
                    remoteConfig.activate().addOnCompleteListener {
                        trySend(Unit)
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                this@callbackFlow.cancel(message = error.message ?: "Unknown Error", cause = error)
            }
        })
        awaitClose {
            listener.remove()
        }
    }

    fun getAgeRangeMin() = remoteConfig.getLong(AGE_RANGE_MIN).toInt()
    fun getAgeRangeMax() = remoteConfig.getLong(AGE_RANGE_MAX).toInt()
    fun getSyncTimeInMinutes() = remoteConfig.getLong(SYNC_TIME_IN_MINUTES)
    fun getMaxOptionSize() = remoteConfig.getLong(MAX_OPTION_SIZE)
    fun getMaxYearAllowed() = remoteConfig.getLong(MAX_YEAR_ALLOWED)
    fun refreshCountServer() = remoteConfig.getLong(FULL_REFRESH)
    fun dashBoardAdMobIndexList() =
        Json.decodeFromString<List<Int>>(remoteConfig.getString(DASHBOARD_AD_MOB_INDEXES))

    fun getDashBoardPageSize() = remoteConfig.getLong(DASHBOARD_PAGE_SIZE).toInt()
    fun getInitWidgetDataSize() = remoteConfig.getLong(INIT_WIDGET_DATA_SIZE).toInt()
    fun getRefreshTimerInMinutesForDashboard() =
        remoteConfig.getLong(REFRESH_TIMER_IN_MINUTES_FOR_DASHBOARD)

    fun maintenanceMode() = remoteConfig.getBoolean(MAINTENANCE_MODE)

    companion object {
        const val AGE_RANGE_MIN = "min_age_range"
        const val AGE_RANGE_MAX = "max_age_range"
        const val SYNC_TIME_IN_MINUTES = "sync_time_in_minutes"
        const val MAX_OPTION_SIZE = "max_option_size"
        const val FULL_REFRESH = "full_refresh"
        const val DASHBOARD_AD_MOB_INDEXES = "dashboard_ad_mob_index"
        const val DASHBOARD_PAGE_SIZE = "dashboard_page_size"
        const val INIT_WIDGET_DATA_SIZE = "init_widget_data_size"
        const val MAX_YEAR_ALLOWED = "max_year_allowed"
        const val REFRESH_TIMER_IN_MINUTES_FOR_DASHBOARD = "refresh_timer_in_minutes_for_dashboard"
        const val MAINTENANCE_MODE = "maintenance_mode"
    }

}