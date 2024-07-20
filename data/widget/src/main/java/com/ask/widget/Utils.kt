package com.ask.widget

import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

fun Flow<List<WidgetWithOptionsAndVotesForTargetAudience>>.mapWithCompute(userId: String) =
    this.map { widgetWithOptionsAndVotesForTargetAudiences ->
        widgetWithOptionsAndVotesForTargetAudiences
            .filter { it -> it.options.any { (it.option.imageUrl != null && it.option.text == null) || (it.option.imageUrl == null && it.option.text != null) } }
            .map { widgetWithOptionsAndVotesForTargetAudience ->
                widgetWithOptionsAndVotesForTargetAudience.copy(
                    options = widgetWithOptionsAndVotesForTargetAudience.options.map { optionWithVotes ->
                        optionWithVotes.apply {
                            didUserVoted = userId in optionWithVotes.votes.map { it.userId }
                            votesPercent =
                                if (totalVotes > 0 && widgetWithOptionsAndVotesForTargetAudience.widgetTotalVotes > 0)
                                    (totalVotes.toFloat() / widgetWithOptionsAndVotesForTargetAudience.widgetTotalVotes.toFloat()) * 100
                                else 0f
                            votesPercentFormat = votesPercent.toPercentage()
                        }
                    }
                ).apply {
                    hasVotes = options.any { it.votes.isNotEmpty() }
                    isCreatorOfTheWidget = userId == widget.creatorId
                }
            }
    }

fun Flow<PagingData<WidgetWithOptionsAndVotesForTargetAudience>>.mapWithComputePagingData(userId: String) =
    this.map { widgetWithOptionsAndVotesForTargetAudiences ->
        widgetWithOptionsAndVotesForTargetAudiences
            .filter { it -> it.options.any { (it.option.imageUrl != null && it.option.text == null) || (it.option.imageUrl == null && it.option.text != null) } }
            .map { widgetWithOptionsAndVotesForTargetAudience ->
                widgetWithOptionsAndVotesForTargetAudience.copy(
                    options = widgetWithOptionsAndVotesForTargetAudience.options.map { optionWithVotes ->
                        optionWithVotes.apply {
                            didUserVoted = userId in optionWithVotes.votes.map { it.userId }
                            votesPercent =
                                if (totalVotes > 0 && widgetWithOptionsAndVotesForTargetAudience.widgetTotalVotes > 0)
                                    (totalVotes.toFloat() / widgetWithOptionsAndVotesForTargetAudience.widgetTotalVotes.toFloat()) * 100
                                else 0f
                            votesPercentFormat = votesPercent.toPercentage()
                        }
                    }
                ).apply {
                    hasVotes = options.any { it.votes.isNotEmpty() }
                    isCreatorOfTheWidget = userId == widget.creatorId
                }
            }
    }


fun Float.toPercentage(): String {
    return String.format(Locale.getDefault(), "%.1f", this)
}