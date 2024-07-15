package com.ask.core

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RemoteConfigRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {

    suspend fun fetchInit(): Boolean = suspendCoroutine { cont ->
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    fun getAgeRangeMin() = remoteConfig.getLong(AGE_RANGE_MIN)
    fun getAgeRangeMax() = remoteConfig.getLong(AGE_RANGE_MAX)
    fun getSyncTimeInMinutes() = remoteConfig.getLong(SYNC_TIME_IN_MINUTES)
    fun getMaxOptionSize() = remoteConfig.getLong(MAX_OPTION_SIZE)

    companion object {
        const val AGE_RANGE_MIN = "min_age_range"
        const val AGE_RANGE_MAX = "max_age_range"
        const val SYNC_TIME_IN_MINUTES = "sync_time_in_minutes"
        const val MAX_OPTION_SIZE = "max_option_size"
    }


}