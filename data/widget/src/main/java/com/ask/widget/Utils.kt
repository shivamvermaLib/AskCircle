package com.ask.widget

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
                                if (totalVotes > 0 && widgetWithOptionsAndVotesForTargetAudience.widgetTotalVotes > 0) String.format(
                                    Locale.getDefault(),
                                    "%.1f",
                                    ((totalVotes / widgetWithOptionsAndVotesForTargetAudience.widgetTotalVotes) * 100).toFloat()
                                ) else "0.0"
                        }
                    }
                ).apply {
                    isCreatorOfTheWidget =
                        userId == widgetWithOptionsAndVotesForTargetAudience.widget.creatorId
                }
            }
    }
