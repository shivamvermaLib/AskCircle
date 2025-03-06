package com.ask.settings

import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.core.AppSharedPreference
import com.ask.user.Feedback
import com.ask.user.FeedbackType
import com.ask.user.GetCurrentProfileUseCase
import com.ask.user.SubmitUserFeedbackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSharedPreference: AppSharedPreference,
    getCurrentProfileUseCase: GetCurrentProfileUseCase,
    @Named("appVersion") appVersion: String,
    private val submitUserFeedbackUseCase: SubmitUserFeedbackUseCase,
    analyticsLogger: AnalyticsLogger
) : BaseViewModel(analyticsLogger) {

    val uiStateFlow = combine(
        appSharedPreference.getNotificationsFlow(),
        getCurrentProfileUseCase()
    ) { notificationSettings, userWithLocationCategory ->
        SettingState(
            notificationSettings = notificationSettings,
            user = userWithLocationCategory,
            version = appVersion,
            termsAndConditions = "https://firebasestorage.googleapis.com/v0/b/ask-app-36527.appspot.com/o/public%2Fprivacy_policy.html?alt=media&token=1cc041a9-4a95-43a4-a19d-2e8876903cd9",
            about = "https://firebasestorage.googleapis.com/v0/b/ask-app-36527.appspot.com/o/public%2Fabout_us.html?alt=media&token=94cd6b4f-701c-4d11-b757-f73415625c12"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SettingState())

    fun onEvent(settingEvent: SettingEvent) {
        viewModelScope.launch {
            when (settingEvent) {
                is SettingEvent.ToggleNewWidgetNotification -> appSharedPreference.setNewWidgetNotification(
                    settingEvent.newWidgetNotification
                )

                is SettingEvent.ToggleVoteOnYourWidgetNotification -> appSharedPreference.setVoteOnWidgetNotification(
                    settingEvent.voteOnYourWidgetNotification
                )

                is SettingEvent.ToggleVotingReminderNotification -> appSharedPreference.setVotingReminderNotification(
                    settingEvent.votingReminderNotification
                )

//                is SettingEvent.ToggleWidgetResultNotification -> appSharedPreference.setWidgetResultNotification(
//                    settingEvent.widgetResultNotification
//                )

                is SettingEvent.ToggleWidgetTimeEndNotification -> appSharedPreference.setWidgetTimeEndNotification(
                    settingEvent.widgetTimeEndNotification
                )

                SettingEvent.ProfileClick -> TODO()
            }
        }
    }

    fun submitFeedback(feedbackType: FeedbackType, text: String) {
        viewModelScope.launch {
            submitUserFeedbackUseCase.invoke(
                Feedback(
                    id = "",
                    text = text,
                    feedbackType = feedbackType
                )
            )
        }
    }
}