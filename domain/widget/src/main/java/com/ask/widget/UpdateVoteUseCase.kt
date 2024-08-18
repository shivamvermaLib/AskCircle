package com.ask.widget

import com.ask.analytics.AnalyticsLogger
import com.ask.core.AppSharedPreference
import com.ask.core.DISPATCHER_DEFAULT
import com.ask.user.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class UpdateVoteUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository,
    private val analyticsLogger: AnalyticsLogger,
    private val userRepository: UserRepository,
    private val sharedPreference: AppSharedPreference,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(widgetId: String, optionId: String) =
        withContext(dispatcher) {
            analyticsLogger.voteWidgetEvent(
                widgetId,
                optionId,
                userRepository.getCurrentUserId(),
            )
            widgetRepository.vote(widgetId, optionId, userRepository.getCurrentUserId())
                .also {
                    val lastUpdatedTime = sharedPreference.getUpdatedTime()
                    sharedPreference.setUpdatedTime(
                        lastUpdatedTime.copy(
                            voteTime = System.currentTimeMillis(),
                        )
                    )
                    analyticsLogger.votedWidgetEvent(
                        widgetId,
                        optionId,
                        userRepository.getCurrentUserId(),
                    )
                }
        }
}