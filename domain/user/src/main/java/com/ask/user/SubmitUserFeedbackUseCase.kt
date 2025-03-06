package com.ask.user

import com.ask.core.DISPATCHER_DEFAULT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class SubmitUserFeedbackUseCase @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(feedback: Feedback) = withContext(dispatcher) {
        feedbackRepository.submitFeedback(feedback)
    }
}