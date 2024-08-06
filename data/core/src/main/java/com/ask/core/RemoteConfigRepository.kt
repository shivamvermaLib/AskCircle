package com.ask.core

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RemoteConfigRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    suspend fun fetchInit(): Boolean = suspendCoroutine { cont ->
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                cont.resume(it)
            }
            .addOnFailureListener { cont.resumeWithException(it) }
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
    fun getRefreshTimerInMinutesForDashboard() = remoteConfig.getLong(REFRESH_TIMER_IN_MINUTES_FOR_DASHBOARD)

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
    }

}