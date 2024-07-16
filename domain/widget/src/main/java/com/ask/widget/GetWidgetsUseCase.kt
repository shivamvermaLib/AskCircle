package com.ask.widget

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
    operator fun invoke(filterType: FilterType): Flow<List<WidgetWithOptionsAndVotesForTargetAudience>> {
        val adMobIndexList = remoteConfigRepository.dashBoardAdMobIndexList()
        return when (filterType) {
            FilterType.Latest -> widgetRepository.getWidgets()
            FilterType.Trending -> widgetRepository.getTrendingWidgets()
            FilterType.MyWidgets -> widgetRepository.getUserWidgets(userId = userRepository.getCurrentUserId())
        }.mapWithCompute(userRepository.getCurrentUserId())
            .map {
                it.mapIndexed { index, widgetWithOptionsAndVotesForTargetAudience ->
                    widgetWithOptionsAndVotesForTargetAudience.apply {
                        showAdMob = adMobIndexList.contains(index)
                    }
                }
            }
    }
}
