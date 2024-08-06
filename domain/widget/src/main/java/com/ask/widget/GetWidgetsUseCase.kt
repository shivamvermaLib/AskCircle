package com.ask.widget

import androidx.paging.PagingData
import com.ask.core.DISPATCHER_DEFAULT
import com.ask.core.RemoteConfigRepository
import com.ask.user.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class GetWidgetsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val widgetRepository: WidgetRepository,
    private val remoteConfigRepository: RemoteConfigRepository,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(filterWithLastVotedEmptyOptionsFlow: Flow<Triple<Filter, List<String>, Long>>): Flow<PagingData<WidgetWithOptionsAndVotesForTargetAudience>> {
        val adMobIndexList = remoteConfigRepository.dashBoardAdMobIndexList()
        val currentTimeFlow = flow {
            while (true) {
                emit(System.currentTimeMillis())
                delay(TimeUnit.MINUTES.toMillis(remoteConfigRepository.getRefreshTimerInMinutesForDashboard()))
            }
        }
        return combine(
            filterWithLastVotedEmptyOptionsFlow, currentTimeFlow
        ) { filterWithLastVotedEmptyOptions, currentTime ->
            filterWithLastVotedEmptyOptions to if (filterWithLastVotedEmptyOptions.third > currentTime) filterWithLastVotedEmptyOptions.third else currentTime
        }.flatMapLatest { filterWithLastVotedEmptyOptionsWithCurrentTime ->
            when (filterWithLastVotedEmptyOptionsWithCurrentTime.first.first) {
                Filter.Latest -> widgetRepository.getWidgets(
                    userRepository.getCurrentUserId(),
                    filterWithLastVotedEmptyOptionsWithCurrentTime.second,
                    remoteConfigRepository.getDashBoardPageSize()
                )

                Filter.MostVoted -> widgetRepository.getMostVotedWidgets(
                    userRepository.getCurrentUserId(), remoteConfigRepository.getDashBoardPageSize()
                )

                Filter.MyWidgets -> widgetRepository.getUserWidgets(
                    userId = userRepository.getCurrentUserId(),
                    remoteConfigRepository.getDashBoardPageSize()
                )

                Filter.BookmarkedWidget -> widgetRepository.getBookmarkedWidgets(
                    userRepository.getCurrentUserId(), remoteConfigRepository.getDashBoardPageSize()
                )

                Filter.Trending -> widgetRepository.getTrendingWidgets(
                    userRepository.getCurrentUserId(), remoteConfigRepository.getDashBoardPageSize()
                )
            }.mapWithComputePagingData(
                userRepository.getCurrentUserId(),
                adMobIndexList,
                filterWithLastVotedEmptyOptionsWithCurrentTime.first.second
            ).flowOn(dispatcher)
        }
    }
}

