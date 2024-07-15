package com.ask.widget

import com.ask.user.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWidgetsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
) {
    operator fun invoke(filterType: FilterType): Flow<List<WidgetWithOptionsAndVotesForTargetAudience>> {
        return when (filterType) {
            FilterType.Latest -> widgetRepository.getWidgets()
            FilterType.Trending -> widgetRepository.getTrendingWidgets()
            FilterType.MyWidgets -> widgetRepository.getUserWidgets(userId = userRepository.getCurrentUserId())
        }.mapWithCompute(userRepository.getCurrentUserId())
    }
}
