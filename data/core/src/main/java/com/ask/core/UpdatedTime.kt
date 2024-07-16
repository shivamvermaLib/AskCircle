package com.ask.core

import kotlinx.serialization.Serializable

@Serializable
data class UpdatedTime(
    val widgetTime: Long = 0,
    val voteTime: Long = 0,
    val profileTime: Long = 0,
)

fun isUpdateRequired(updatedTime: UpdatedTime, lastUpdatedTime: UpdatedTime): Boolean {
    return updatedTime.widgetTime >= lastUpdatedTime.widgetTime || updatedTime.voteTime >= lastUpdatedTime.voteTime || updatedTime.profileTime >= lastUpdatedTime.profileTime
}

