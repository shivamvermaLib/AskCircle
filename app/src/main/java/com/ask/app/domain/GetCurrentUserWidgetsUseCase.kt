package com.ask.app.domain

import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.app.data.repository.UserRepository
import com.ask.app.data.repository.WidgetRepository
import com.ask.app.data.utils.mapWithCompute
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserWidgetsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository
) {

    operator fun invoke(): Flow<List<WidgetWithOptionsAndVotesForTargetAudience>> {
        return userRepository.getCurrentUserId().let {
            widgetRepository.getUserWidgets(it)
                .mapWithCompute(it)
        }
    }
}
