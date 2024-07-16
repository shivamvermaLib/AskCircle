package com.ask.widget

import com.ask.analytics.AnalyticsLogger
import com.ask.user.UserRepository
import javax.inject.Inject

class UpdateVoteUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository,
    private val analyticsLogger: AnalyticsLogger,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(widgetId: String, optionId: String, screenName: String) {
        analyticsLogger.voteWidgetEvent(
            widgetId,
            optionId,
            userRepository.getCurrentUserId(),
            screenName
        )
        widgetRepository.vote(widgetId, optionId, userRepository.getCurrentUserId())
            .also {
                analyticsLogger.votedWidgetEvent(
                    widgetId,
                    optionId,
                    userRepository.getCurrentUserId(),
                    screenName
                )
            }
    }
}