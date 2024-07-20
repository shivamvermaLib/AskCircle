package com.ask.widget

import com.ask.user.UserRepository
import javax.inject.Inject

class GetWidgetDetailsUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        widgetId: String,
        preloadImages: suspend (List<String>) -> Unit
    ): WidgetWithOptionsAndVotesForTargetAudience {
        return widgetRepository.getWidgetDetails(widgetId, { userId ->
            userRepository.getUser(userId, true, preloadImages)
        }, preloadImages)
            .setupData(userRepository.getCurrentUserId())
    }

}