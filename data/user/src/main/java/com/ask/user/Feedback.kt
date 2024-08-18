package com.ask.user

data class Feedback(
    val id: String,
    val text: String,
    val feedbackType: FeedbackType
)

enum class FeedbackType {
    SAD, NORMAL, HAPPY
}
