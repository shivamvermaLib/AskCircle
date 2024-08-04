package com.ask.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSharedPreference @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = APP_SHARED_PREFERENCE
    )

//    private val sharedPreference =
//        context.getSharedPreferences(APP_SHARED_PREFERENCE, Context.MODE_PRIVATE)

    private val TABLE_UPDATED_TIME = stringPreferencesKey(AppSharedPreference.TABLE_UPDATED_TIME)
    private val REFRESH_COUNT = longPreferencesKey(AppSharedPreference.REFRESH_COUNT)
    private val DB_VERSION = intPreferencesKey(AppSharedPreference.DB_VERSION)
    private val WIDGET_BOOKMARK = stringSetPreferencesKey(AppSharedPreference.WIDGET_BOOKMARK)
    private val AI_SEARCH_PROMPT = stringSetPreferencesKey(AppSharedPreference.AI_SEARCH_PROMPT)

    suspend fun setUpdatedTime(updatedTime: UpdatedTime) {
//        sharedPreference.edit().putString(TABLE_UPDATED_TIME, Json.encodeToString(updatedTime))
//            .apply()
        context.userPreferencesDataStore.edit { preferences ->
            preferences[TABLE_UPDATED_TIME] = Json.encodeToString(updatedTime)
        }
    }

    suspend fun getUpdatedTime(): UpdatedTime {
//        sharedPreference.getString(TABLE_UPDATED_TIME, null)?.let {
//            Json.decodeFromString<UpdatedTime>(it)
//        } ?: UpdatedTime()
        return context.userPreferencesDataStore.data.firstOrNull()?.let {
            it[TABLE_UPDATED_TIME]?.let { json -> Json.decodeFromString(json) }
        } ?: UpdatedTime()
    }

    suspend fun getRefreshCount(): Long {
//        sharedPreference.getLong(REFRESH_COUNT, 0)
        return context.userPreferencesDataStore.data.firstOrNull()?.let {
            it[REFRESH_COUNT]?.toLong()
        } ?: 0
    }

    suspend fun setRefreshCount(count: Long) {
//        sharedPreference.edit().putLong(REFRESH_COUNT, count).apply()
        context.userPreferencesDataStore.edit { preferences ->
            preferences[REFRESH_COUNT] = count
        }
    }

    suspend fun getDbVersion(): Int {
        //sharedPreference.getInt(DB_VERSION, 1)
        return context.userPreferencesDataStore.data.firstOrNull()?.let {
            it[DB_VERSION]?.toInt()
        } ?: 1
    }

    suspend fun setDbVersion(version: Int) {
//        sharedPreference.edit().putInt(DB_VERSION, version).apply()
        context.userPreferencesDataStore.edit { preferences ->
            preferences[DB_VERSION] = version
        }
    }


    suspend fun setAiSearchPrompt(search: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[AI_SEARCH_PROMPT] = getAiSearchPrompt() + search
        }
    }

    suspend fun getAiSearchPrompt(): Set<String> {
        return context.userPreferencesDataStore.data.firstOrNull()?.let {
            return it[AI_SEARCH_PROMPT] ?: emptySet()
        } ?: emptySet()
    }


    suspend fun clearData() {
//        sharedPreference.edit().clear().apply()
        context.userPreferencesDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        const val APP_SHARED_PREFERENCE = "app_shared_preference"
        const val REFRESH_COUNT = "Refresh count"
        const val DB_VERSION = "DB version"
        const val WIDGET_BOOKMARK = "Widget bookmark"
        const val TABLE_UPDATED_TIME = "updated_time"
        const val AI_SEARCH_PROMPT = "ai_search_prompt"
    }
}