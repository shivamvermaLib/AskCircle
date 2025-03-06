package com.ask.user

import com.ask.core.DISPATCHER_IO
import com.ask.core.FirebaseDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class FeedbackRepository @Inject constructor(
    @Named(TABLE_FEEDBACK) private val feedbackDataSource: FirebaseDataSource<Feedback>,
    @Named(DISPATCHER_IO) private val dispatcher: CoroutineDispatcher
) {
    suspend fun submitFeedback(feedback: Feedback) = withContext(dispatcher) {
        feedbackDataSource.addItem(feedback)
    }
}