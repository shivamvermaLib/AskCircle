package com.ask.widget

data class WidgetDetailsWithResult(
    val widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience? = null,
    val widgetResults: Map<String, List<WidgetResult>> = emptyMap(),
    val widgetOptionResults: Map<String, Map<String, List<WidgetResult>>> = emptyMap(),
) {
    data class WidgetResult(
        val title: String,
        val count: Int,
        val color: Int,
        val floatValue: Float = 0f,
        val percent: String,
    )
}