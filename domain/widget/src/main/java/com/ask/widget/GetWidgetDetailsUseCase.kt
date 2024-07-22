package com.ask.widget

import com.ask.user.UserRepository
import javax.inject.Inject

class GetWidgetDetailsUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        widgetId: String,
        lastVotedEmptyOptions: List<String>,
        preloadImages: suspend (List<String>) -> Unit
    ): WidgetWithOptionsAndVotesForTargetAudience {
        return widgetRepository.getWidgetDetails(
            widgetId,
            userRepository.getCurrentUserId(),
            { userId ->
                userRepository.getUser(userId, true, preloadImages)
            },
            preloadImages
        )
            .setupData(userRepository.getCurrentUserId(), true, lastVotedEmptyOptions.random())
    }

}