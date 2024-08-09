package com.ask.settings

import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen() {

}

@Composable
private fun SettingsScreen(state: SettingState) {

}


data class SettingState(
    val darkMode: Boolean = false,
    val version: String = "",
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val about: String = "",
    val error: String? = null,
    val termsAndConditions: String = "",
) {
    data class NotificationSettings(
        val notifications: Boolean = false,
        val newWidgetNotification: Boolean = false,
        val voteOnYourWidgetNotification: Boolean = false,
        val widgetResultNotification: Boolean = false,
        val widgetTimeEndNotification: Boolean = false,
        val votingReminderNotification: Boolean = false,
    )
}