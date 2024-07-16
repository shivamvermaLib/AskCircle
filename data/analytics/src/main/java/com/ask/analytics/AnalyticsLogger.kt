package com.ask.analytics

import android.os.Bundle
import com.ask.user.Gender
import com.ask.widget.Widget
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import javax.inject.Inject

class AnalyticsLogger @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {

    fun screenOpenEvent(screenName: String) {
        firebaseAnalytics.logEvent(SCREEN_OPEN) {
            param(SCREEN_NAME, screenName)
        }
    }

    fun createWidgetEvent(
        widgetType: Widget.WidgetType,
        hasDescription: Boolean,
        optionSize: Int,
        isImageWidget: Boolean,
        genderFilter: Widget.GenderFilter,
        locations: List<String>,
        minAge: Int,
        maxAge: Int
    ) {
        firebaseAnalytics.logEvent(CREATING_WIDGET, Bundle().apply {
            putString(WIDGET_TYPE, widgetType.name)
            putBoolean(HAS_DESCRIPTION, hasDescription)
            putInt(OPTION_SIZE, optionSize)
            putBoolean(IS_IMAGE_WIDGET, isImageWidget)
            putString(GENDER_FILTER, genderFilter.name)
            putStringArray(LOCATIONS, locations.toTypedArray())
            putInt(MIN_AGE, minAge)
            putInt(MAX_AGE, maxAge)
        })
    }

    fun createdWidgetEvent(
        widgetId: String,
        widgetType: Widget.WidgetType,
        hasDescription: Boolean,
        optionSize: Int,
        isImageWidget: Boolean,
        genderFilter: Widget.GenderFilter,
        locations: List<String>,
        minAge: Int,
        maxAge: Int
    ) {
        firebaseAnalytics.logEvent(CREATED_WIDGET, Bundle().apply {
            putString(WIDGET_ID, widgetId)
            putString(WIDGET_TYPE, widgetType.name)
            putBoolean(HAS_DESCRIPTION, hasDescription)
            putInt(OPTION_SIZE, optionSize)
            putBoolean(IS_IMAGE_WIDGET, isImageWidget)
            putString(GENDER_FILTER, genderFilter.name)
            putStringArray(LOCATIONS, locations.toTypedArray())
            putInt(MIN_AGE, minAge)
            putInt(MAX_AGE, maxAge)
        })
    }

    fun voteWidgetEvent(widgetId: String, optionId: String, userId: String, screenName: String) {
        firebaseAnalytics.logEvent(VOTE_WIDGET, Bundle().apply {
            putString(WIDGET_ID, widgetId)
            putString(OPTION_ID, optionId)
            putString(USER_ID, userId)
            putString(SCREEN_NAME, screenName)
        })
    }

    fun votedWidgetEvent(widgetId: String, optionId: String, userId: String, screenName: String) {
        firebaseAnalytics.logEvent(VOTED_WIDGET, Bundle().apply {
            putString(WIDGET_ID, widgetId)
            putString(OPTION_ID, optionId)
            putString(USER_ID, userId)
            putString(SCREEN_NAME, screenName)
        })
    }

    fun widgetFilterTypeEvent(filterType: String) {
        firebaseAnalytics.logEvent(WIDGET_FILTER_TYPE, Bundle().apply {
            putString(FILTER, filterType)
        })
    }

    fun updateProfileEvent(
        gender: Gender?,
        age: Int?,
        location: String?,
        hasProfilePic: Boolean,
        hasEmail: Boolean,
    ) {
        firebaseAnalytics.logEvent(UPDATE_PROFILE, Bundle().apply {
            putString(GENDER_FILTER, gender?.name)
            age?.let { putInt(MIN_AGE, it) }
            putString(LOCATIONS, location)
            putBoolean(HAS_PROFILE_PIC, hasProfilePic)
            putBoolean(HAS_EMAIL, hasEmail)
        })
    }

    fun profileUpdatedEvent(
        gender: Gender?,
        age: Int?,
        location: String?,
        hasProfilePic: Boolean,
        hasEmail: Boolean,
    ) {
        firebaseAnalytics.logEvent(UPDATED_PROFILE, Bundle().apply {
            putString(GENDER_FILTER, gender?.name)
            age?.let { putInt(MIN_AGE, it) }
            putString(LOCATIONS, location)
            putBoolean(HAS_PROFILE_PIC, hasProfilePic)
            putBoolean(HAS_EMAIL, hasEmail)
        })
    }

    fun syncUsersAndWidgetsEventDuration(time: Long) {
        firebaseAnalytics.logEvent(SYNC_USERS_AND_WIDGETS, Bundle().apply {
            putLong(SYNC_USERS_AND_WIDGETS_DURATION, time)
        })
    }

    fun remoteConfigFetchEvent(it: Boolean) {
        firebaseAnalytics.logEvent(REMOTE_CONFIG_FETCH, Bundle().apply {
            putBoolean(SUCCESS, it)
        })
    }

    fun refreshTriggerEvent(
        refreshCountServer: Long,
        refreshCountLocal: Long,
        currentUserId: String
    ) {
        firebaseAnalytics.logEvent(REFRESH_COUNT_EVENT, Bundle().apply {
            putLong(REFRESH_COUNT_SERVER, refreshCountServer)
            putLong(REFRESH_COUNT_LOCAL, refreshCountLocal)
            putString(USER_ID, currentUserId)
        })
    }


    companion object {
        const val SCREEN_OPEN = "screen_open"
        const val SCREEN_NAME = "screen_name"
        const val CREATING_WIDGET = "widget_creating"
        const val CREATED_WIDGET = "widget_created"
        const val WIDGET_TYPE = "widget_type"
        const val HAS_DESCRIPTION = "has_description"
        const val OPTION_SIZE = "option_size"
        const val IS_IMAGE_WIDGET = "is_image_widget"
        const val GENDER_FILTER = "gender_filter"
        const val LOCATIONS = "locations"
        const val MIN_AGE = "min_age"
        const val MAX_AGE = "max_age"
        const val WIDGET_ID = "widget_id"
        const val OPTION_ID = "option_id"
        const val USER_ID = "user_id"
        const val VOTE_WIDGET = "widget_voting"
        const val VOTED_WIDGET = "widget_voted"
        const val FILTER = "filter"
        const val WIDGET_FILTER_TYPE = "widget_filter_type"
        const val UPDATE_PROFILE = "update_profile"
        const val UPDATED_PROFILE = "profile_updated"
        const val SYNC_USERS_AND_WIDGETS = "sync_users_and_widgets"
        const val HAS_PROFILE_PIC = "has_profile_pic"
        const val HAS_EMAIL = "has_email"
        const val SYNC_USERS_AND_WIDGETS_DURATION = "sync_users_and_widgets_duration"
        const val REMOTE_CONFIG_FETCH = "remote_config_fetch"
        const val SUCCESS = "success"
        const val REFRESH_COUNT_EVENT = "refresh_count_event"
        const val REFRESH_COUNT_SERVER = "refresh_count_server"
        const val REFRESH_COUNT_LOCAL = "refresh_count_local"
    }
}