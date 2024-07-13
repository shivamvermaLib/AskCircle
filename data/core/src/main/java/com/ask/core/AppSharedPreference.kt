package com.ask.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSharedPreference @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPreference =
        context.getSharedPreferences(APP_SHARED_PREFERENCE, Context.MODE_PRIVATE)

    fun setUpdatedTime(updatedTime: UpdatedTime) {
        sharedPreference.edit().putString(TABLE_UPDATED_TIME, Json.encodeToString(updatedTime))
            .apply()
    }

    fun getUpdatedTime(): UpdatedTime = sharedPreference.getString(TABLE_UPDATED_TIME, null)?.let {
        Json.decodeFromString<UpdatedTime>(it)
    } ?: UpdatedTime()

    companion object {
        const val APP_SHARED_PREFERENCE = "app_shared_preference"
    }
}