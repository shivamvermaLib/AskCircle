package com.ask.widget

import androidx.paging.PagingData
import androidx.paging.map
import com.ask.core.RemoteConfigRepository
import com.ask.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetWidgetsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
    private val remoteConfigRepository: RemoteConfigRepository,
) {
    operator fun invoke(
        filter: Filter,
        lastVotedEmptyOptions: List<String>
    ): Flow<PagingData<WidgetWithOptionsAndVotesForTargetAudience>> {
        val adMobIndexList = remoteConfigRepository.dashBoardAdMobIndexList()
        return when (filter) {
            Filter.Latest -> widgetRepository.getWidgets(remoteConfigRepository.getDashBoardPageSize())
            Filter.Trending -> widgetRepository.getTrendingWidgets(remoteConfigRepository.getDashBoardPageSize())
            Filter.MyWidgets -> widgetRepository.getUserWidgets(
                userId = userRepository.getCurrentUserId(),
                remoteConfigRepository.getDashBoardPageSize()
            )
        }.mapWithComputePagingData(userRepository.getCurrentUserId())
            .map {
                var index = 0
                it.map { widgetWithOptionsAndVotesForTargetAudience ->
                    widgetWithOptionsAndVotesForTargetAudience.apply {
                        showAdMob = adMobIndexList.contains(index)
                        lastVotedAtOptional = lastVotedEmptyOptions.random()
                    }.also {
                        index++
                    }
                }
            }
    }
}

