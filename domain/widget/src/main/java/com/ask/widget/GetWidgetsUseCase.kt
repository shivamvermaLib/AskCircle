package com.ask.widget

import androidx.paging.PagingData
import com.ask.core.DISPATCHER_DEFAULT
import com.ask.core.RemoteConfigRepository
import com.ask.user.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Named

class GetWidgetsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
    private val remoteConfigRepository: RemoteConfigRepository,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(filterWithLastVotedEmptyOptionsFlow: Flow<Pair<Filter, List<String>>>): Flow<PagingData<WidgetWithOptionsAndVotesForTargetAudience>> {
        val adMobIndexList = remoteConfigRepository.dashBoardAdMobIndexList()
        return filterWithLastVotedEmptyOptionsFlow.flatMapLatest { filterWithLastVotedEmptyOptions ->
            when (filterWithLastVotedEmptyOptions.first) {
                Filter.Latest -> widgetRepository.getWidgets(
                    userRepository.getCurrentUserId(),
                    remoteConfigRepository.getDashBoardPageSize()
                )

                Filter.Trending -> widgetRepository.getTrendingWidgets(
                    userRepository.getCurrentUserId(),
                    remoteConfigRepository.getDashBoardPageSize()
                )

                Filter.MyWidgets -> widgetRepository.getUserWidgets(
                    userId = userRepository.getCurrentUserId(),
                    remoteConfigRepository.getDashBoardPageSize()
                )

                Filter.BookmarkedWidget -> widgetRepository.getBookmarkedWidgets(
                    userRepository.getCurrentUserId(),
                    remoteConfigRepository.getDashBoardPageSize()
                )
            }.mapWithComputePagingData(
                userRepository.getCurrentUserId(),
                adMobIndexList,
                filterWithLastVotedEmptyOptions
            ).flowOn(dispatcher)
        }
    }
}

