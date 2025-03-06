package com.ask.widget

import com.ask.core.DISPATCHER_DEFAULT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class StartStopWidgetAcceptingVoteUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(widgetId: String, isStart: Boolean) = withContext(dispatcher) {
        widgetRepository.startStopVoting(widgetId, isStart)
    }
}