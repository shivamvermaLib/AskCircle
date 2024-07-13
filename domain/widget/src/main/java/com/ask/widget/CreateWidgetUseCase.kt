package com.ask.widget

import com.ask.user.UserRepository
import javax.inject.Inject

class CreateWidgetUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
) {

    suspend operator fun invoke(
        widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
        getExtension: (String) -> String,
        getByteArray: (String) -> ByteArray
    ): WidgetWithOptionsAndVotesForTargetAudience {
        return widgetRepository.createWidget(
            widgetWithOptionsAndVotesForTargetAudience.copy(
                widget = widgetWithOptionsAndVotesForTargetAudience.widget.copy(
                    creatorId = userRepository.getCurrentUserId()
                )
            ),
            getExtension,
            getByteArray
        )
    }
}