package com.ask.widget

import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

fun Flow<List<WidgetWithOptionsAndVotesForTargetAudience>>.mapWithCompute(
    userId: String,
    adMobIndexList: List<Int>,
    filterWithLastVotedEmptyOptions: Pair<Any, List<String>>
) =
    this.map { widgetWithOptionsAndVotesForTargetAudiences ->
        var index = 0
        widgetWithOptionsAndVotesForTargetAudiences
            .filter { it -> it.options.any { (it.option.imageUrl != null && it.option.text == null) || (it.option.imageUrl == null && it.option.text != null) } }
            .map { widgetWithOptionsAndVotesForTargetAudience ->
                widgetWithOptionsAndVotesForTargetAudience.setupData(
                    userId,
                    adMobIndexList.contains(index),
                    filterWithLastVotedEmptyOptions.second.random()
                ).also { index++ }
            }
    }

fun Flow<PagingData<WidgetWithOptionsAndVotesForTargetAudience>>.mapWithComputePagingData(
    userId: String,
    adMobIndexList: List<Int>,
    lastVotedEmptyOptions: List<String>
) = this.map { widgetWithOptionsAndVotesForTargetAudiences ->
    var index = 0
    widgetWithOptionsAndVotesForTargetAudiences
        .filter { it -> it.options.any { (it.option.imageUrl != null && it.option.text == null) || (it.option.imageUrl == null && it.option.text != null) } }
        .map { widgetWithOptionsAndVotesForTargetAudience ->
            widgetWithOptionsAndVotesForTargetAudience.setupData(
                userId,
                adMobIndexList.contains(index),
                lastVotedEmptyOptions.random()
            ).also { index++ }
        }
}


fun Float.toPercentage(): String {
    return String.format(Locale.getDefault(), "%.1f", this)
}