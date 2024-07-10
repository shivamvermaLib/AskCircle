package com.ask.app.domain

import com.ask.app.data.repository.UserRepository
import com.ask.app.data.repository.WidgetRepository
import com.ask.app.data.utils.mapWithCompute
import javax.inject.Inject

class GetWidgetsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
) {
    operator fun invoke() = widgetRepository.getWidgets()
        .mapWithCompute(userRepository.getCurrentUserId())
}
