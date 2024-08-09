package com.ask.settings

import com.ask.core.NotificationSettings
import com.ask.user.User
import com.ask.user.UserWithLocationCategory

data class SettingState(
    val darkMode: Boolean = false,
    val version: String = "",
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val about: String = "",
    val error: String? = null,
    val termsAndConditions: String = "",
    val user: UserWithLocationCategory = UserWithLocationCategory(
        user = User(),
        userCategories = emptyList(),
        userLocation = User.UserLocation(),
        userWidgetBookmarks = emptyList()
    ),
)

sealed class SettingEvent {
    data class ToggleNewWidgetNotification(val newWidgetNotification: Boolean) : SettingEvent()
    data class ToggleVoteOnYourWidgetNotification(val voteOnYourWidgetNotification: Boolean) :
        SettingEvent()

    data class ToggleWidgetResultNotification(val widgetResultNotification: Boolean) :
        SettingEvent()

    data class ToggleWidgetTimeEndNotification(val widgetTimeEndNotification: Boolean) :
        SettingEvent()

    data class ToggleVotingReminderNotification(val votingReminderNotification: Boolean) :
        SettingEvent()
}
