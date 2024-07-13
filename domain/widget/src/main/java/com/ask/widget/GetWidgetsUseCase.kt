package com.ask.widget

import com.ask.user.UserRepository
import com.ask.widget.WidgetRepository
import com.ask.widget.mapWithCompute
import javax.inject.Inject

class GetWidgetsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
) {
    operator fun invoke() = widgetRepository.getWidgets()
        .mapWithCompute(userRepository.getCurrentUserId())
}
