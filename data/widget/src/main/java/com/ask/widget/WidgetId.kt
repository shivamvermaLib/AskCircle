package com.ask.widget

import java.util.UUID

data class WidgetId(
    val id: String = UUID.randomUUID().toString(),
    val widgetIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)