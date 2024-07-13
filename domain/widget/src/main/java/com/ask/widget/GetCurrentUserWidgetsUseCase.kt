package com.ask.widget

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserWidgetsUseCase @Inject constructor(
    private val userRepository: com.ask.user.UserRepository,
    private val widgetRepository: WidgetRepository
) {

    operator fun invoke(): Flow<List<WidgetWithOptionsAndVotesForTargetAudience>> {
        return userRepository.getCurrentUserId().let {
            widgetRepository.getUserWidgets(it)
                .mapWithCompute(it)
        }
    }
}
