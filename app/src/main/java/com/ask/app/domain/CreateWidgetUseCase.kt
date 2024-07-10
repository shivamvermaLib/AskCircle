package com.ask.app.domain

import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.app.data.repository.UserRepository
import com.ask.app.data.repository.WidgetRepository
import javax.inject.Inject

class CreateWidgetUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
) {

    suspend operator fun invoke(
        widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
        getExtension: (String) -> String,
        getByteArray: (String) -> ByteArray
    ) {
        widgetRepository.createWidget(
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