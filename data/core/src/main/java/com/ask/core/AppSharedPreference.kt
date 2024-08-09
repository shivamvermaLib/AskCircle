package com.ask.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSharedPreference @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = APP_SHARED_PREFERENCE
    )

    private val tableUpdatedTime = stringPreferencesKey(TABLE_UPDATED_TIME)
    private val refreshCount = longPreferencesKey(REFRESH_COUNT)
    private val dbVersion = intPreferencesKey(DB_VERSION)
    private val widgetBookmarks = stringSetPreferencesKey(WIDGET_BOOKMARK)
    private val aiSearchPrompt = stringSetPreferencesKey(AI_SEARCH_PROMPT)
    private val newWidgetNotifications =
        booleanPreferencesKey(NEW_WIDGET_NOTIFICATION)
    private val voteOnWidgetNotifications =
        booleanPreferencesKey(VOTE_ON_WIDGET_NOTIFICATION)
    private val widgetResultNotifications =
        booleanPreferencesKey(WIDGET_RESULT_NOTIFICATION)
    private val widgetTimeEndNotification =
        booleanPreferencesKey(WIDGET_TIME_END_NOTIFICATION)
    private val votingReminderNotification =
        booleanPreferencesKey(VOTING_REMINDER_NOTIFICATION)


    suspend fun setUpdatedTime(updatedTime: UpdatedTime) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[tableUpdatedTime] = Json.encodeToString(updatedTime)
        }
    }

    suspend fun getUpdatedTime(): UpdatedTime {
        return context.userPreferencesDataStore.data.firstOrNull()?.let {
            it[tableUpdatedTime]?.let { json -> Json.decodeFromString(json) }
        } ?: UpdatedTime()
    }

    suspend fun getRefreshCount(): Long {
        return context.userPreferencesDataStore.data.firstOrNull()?.let {
            it[refreshCount]?.toLong()
        } ?: 0
    }

    suspend fun setRefreshCount(count: Long) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[refreshCount] = count
        }
    }

    suspend fun getDbVersion(): Int {
        return context.userPreferencesDataStore.data.firstOrNull()?.let {
            it[dbVersion]?.toInt()
        } ?: 1
    }

    suspend fun setDbVersion(version: Int) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[dbVersion] = version
        }
    }

    suspend fun setAiSearchPrompt(search: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[aiSearchPrompt] = getAiSearchPrompt() + search
        }
    }

    suspend fun getAiSearchPrompt(): Set<String> {
        return context.userPreferencesDataStore.data.firstOrNull()?.let {
            return it[aiSearchPrompt] ?: emptySet()
        } ?: emptySet()
    }

    suspend fun clearData() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun getNotificationsFlow(): Flow<NotificationSettings> {
        return context.userPreferencesDataStore.data.map {
            NotificationSettings(
                newWidgetNotification = it[newWidgetNotifications] ?: false,
                voteOnYourWidgetNotification = it[voteOnWidgetNotifications] ?: false,
                widgetResultNotification = it[widgetResultNotifications] ?: false,
                widgetTimeEndNotification = it[widgetTimeEndNotification] ?: false,
                votingReminderNotification = it[votingReminderNotification] ?: false
            )
        }
    }

    suspend fun setNewWidgetNotification(newWidgetNotification: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[newWidgetNotifications] = newWidgetNotification
        }
    }

    suspend fun setVoteOnWidgetNotification(voteOnWidgetNotification: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[voteOnWidgetNotifications] = voteOnWidgetNotification
        }
    }

    suspend fun setWidgetResultNotification(widgetResultNotification: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[widgetResultNotifications] = widgetResultNotification
        }
    }

    suspend fun setWidgetTimeEndNotification(notification: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[widgetTimeEndNotification] = notification
        }
    }

    suspend fun setVotingReminderNotification(notification: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[votingReminderNotification] = notification
        }
    }

    companion object {
        const val APP_SHARED_PREFERENCE = "app_shared_preference"
        const val REFRESH_COUNT = "Refresh count"
        const val DB_VERSION = "DB version"
        const val WIDGET_BOOKMARK = "Widget bookmark"
        const val TABLE_UPDATED_TIME = "updated_time"
        const val AI_SEARCH_PROMPT = "ai_search_prompt"
        const val NEW_WIDGET_NOTIFICATION = "new_widget_notification"
        const val VOTE_ON_WIDGET_NOTIFICATION = "vote_on_widget_notification"
        const val WIDGET_RESULT_NOTIFICATION = "widget_result_notification"
        const val WIDGET_TIME_END_NOTIFICATION = "widget_time_end_notification"
        const val VOTING_REMINDER_NOTIFICATION = "voting_reminder_notification"
    }
}

data class NotificationSettings(
    val newWidgetNotification: Boolean = false,
    val voteOnYourWidgetNotification: Boolean = false,
    val widgetResultNotification: Boolean = false,
    val widgetTimeEndNotification: Boolean = false,
    val votingReminderNotification: Boolean = false,
)