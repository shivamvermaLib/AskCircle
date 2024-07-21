package com.ask.widget


import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.ask.core.DOT
import com.ask.core.FirebaseDataSource
import com.ask.core.FirebaseOneDataSource
import com.ask.core.FirebaseStorageSource
import com.ask.core.IMAGE_SPLIT_FACTOR
import com.ask.core.ImageSizeType
import com.ask.core.UNDERSCORE
import com.ask.core.UpdatedTime
import com.ask.core.checkIfUrl
import com.ask.core.fileNameWithExtension
import com.ask.core.getAllImages
import com.ask.core.isUpdateRequired
import com.ask.user.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class WidgetRepository @Inject constructor(
    private val widgetDataSource: FirebaseDataSource<WidgetWithOptionsAndVotesForTargetAudience>,
    private val widgetIdDataSource: FirebaseDataSource<WidgetId>,
    private val widgetUpdateTimeOneDataSource: FirebaseOneDataSource<UpdatedTime>,
    private val widgetDao: WidgetDao,
    @Named(TABLE_WIDGETS) private val pollOptionStorageSource: FirebaseStorageSource,
    @Named("IO") private val dispatcher: CoroutineDispatcher
) {

    fun getUserWidgets(userId: String, limit: Int) =
        Pager(config = PagingConfig(pageSize = limit),
            pagingSourceFactory = { widgetDao.getUserWidgets(userId) }
        ).flow

    fun getWidgets(limit: Int) = Pager(config = PagingConfig(pageSize = limit),
        pagingSourceFactory = { widgetDao.getWidgets() }
    ).flow

    fun getTrendingWidgets(limit: Int) = Pager(config = PagingConfig(pageSize = limit),
        pagingSourceFactory = { widgetDao.getTrendingWidgets() }
    ).flow

    suspend fun createWidget(
        widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
        getExtension: (String) -> String,
        getByteArrays: suspend (String) -> Map<ImageSizeType, ByteArray>,
    ): WidgetWithOptionsAndVotesForTargetAudience = withContext(dispatcher) {
        val pollId = widgetWithOptionsAndVotesForTargetAudience.widget.id
        val createdPollWithOptionsAndVotesForTargetAudience =
            widgetDataSource.addItem(widgetWithOptionsAndVotesForTargetAudience.copy(
                targetAudienceAgeRange = widgetWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.copy(
                    widgetId = pollId
                ),
                targetAudienceGender = widgetWithOptionsAndVotesForTargetAudience.targetAudienceGender.copy(
                    widgetId = pollId
                ),
                targetAudienceLocations = widgetWithOptionsAndVotesForTargetAudience.targetAudienceLocations.filter {
                    (it.country == null && it.state == null && it.city == null).not()
                }.map {
                    it.copy(widgetId = pollId)
                },
                options = widgetWithOptionsAndVotesForTargetAudience.options.map { optionWithVotes ->
                    optionWithVotes.copy(
                        votes = optionWithVotes.votes.map { vote -> vote.copy(optionId = optionWithVotes.option.id) },
                        option = optionWithVotes.option.copy(
                            widgetId = pollId,
                            imageUrl = when (optionWithVotes.option.imageUrl != null && optionWithVotes.option.imageUrl.checkIfUrl()
                                .not()) {
                                true -> getByteArrays(optionWithVotes.option.imageUrl).map {
                                    pollOptionStorageSource.upload(
                                        "${optionWithVotes.option.id}$UNDERSCORE${it.key.name}$DOT${
                                            getExtension(optionWithVotes.option.imageUrl)
                                        }",
                                        it.value
                                    )
                                }.joinToString(separator = IMAGE_SPLIT_FACTOR)

                                else -> when (optionWithVotes.option.imageUrl?.checkIfUrl()) {
                                    true -> optionWithVotes.option.imageUrl
                                    else -> null
                                }
                            }
                        )
                    )
                })
            )
        widgetDao.insertWidget(
            createdPollWithOptionsAndVotesForTargetAudience.widget,
            createdPollWithOptionsAndVotesForTargetAudience.targetAudienceGender,
            createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange,
            createdPollWithOptionsAndVotesForTargetAudience.targetAudienceLocations,
            createdPollWithOptionsAndVotesForTargetAudience.options.map { it.option },
            createdPollWithOptionsAndVotesForTargetAudience.options.map { it.votes }.flatten()
        )

        createdPollWithOptionsAndVotesForTargetAudience.run {
            generateCombinationsForWidget(
                targetAudienceGender,
                targetAudienceAgeRange,
                targetAudienceLocations,
                createdPollWithOptionsAndVotesForTargetAudience.widget.creatorId,
                categories
            ).map {
                async {
                    val widgetId = widgetIdDataSource.getItemOrNull(it)
                    if (widgetId == null) {
                        widgetIdDataSource.addItem(WidgetId(widgetIds = listOf(pollId), id = it))
                    } else {
                        widgetIdDataSource.updateItem(widgetId.copy(widgetIds = widgetId.widgetIds + pollId))
                    }
                }
            }.awaitAll()
        }

        widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
            updatedTime.copy(widgetTime = System.currentTimeMillis())
        }

        widgetDao.getWidgetById(pollId) ?: throw Exception("Unable to get created Poll from Local")
    }

    suspend fun deleteWidget(widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience) =
        withContext(dispatcher) {
            widgetWithOptionsAndVotesForTargetAudience.options.forEach { optionWithVotes ->
                optionWithVotes.option.imageUrl?.let {
                    if (it.checkIfUrl()) {
                        pollOptionStorageSource.delete(it.fileNameWithExtension())
                    }
                }
            }
            widgetDataSource.deleteItem(widgetWithOptionsAndVotesForTargetAudience)
            widgetDao.deleteWidget(widgetWithOptionsAndVotesForTargetAudience.widget)
            widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
                updatedTime.copy(widgetTime = System.currentTimeMillis())
            }
        }

    suspend fun doesSyncRequired(lastUpdatedTime: UpdatedTime): Boolean {
        val updatedTime = widgetUpdateTimeOneDataSource.getItem() ?: UpdatedTime()
        return isUpdateRequired(updatedTime, lastUpdatedTime)
    }

    suspend fun syncWidgetsFromServer(
        initPageSize: Int = 0,
        currentUserId: String,
        lastUpdatedTime: UpdatedTime,
        searchCombinations: Set<String>,
        fetchUserDetails: suspend (String) -> User,
        preloadImages: suspend (List<String>) -> Unit
    ) = withContext(dispatcher) {
        var widgetIds = searchCombinations.map {
            async {
                widgetIdDataSource.getItemOrNull(it)
            }
        }.awaitAll().asSequence().filterNotNull().distinct().map { it.widgetIds }.flatten()
            .distinct()
        if (initPageSize > 0) {
            widgetIds = widgetIds.take(initPageSize)
        }

        val widgetWithOptionsAndVotesForTargetAudiences =
            mutableListOf<WidgetWithOptionsAndVotesForTargetAudience>()
        for (id in widgetIds) {
            val widgetWithOptionsAndVotesForTargetAudience = widgetDataSource.getItem(id)
            if (widgetWithOptionsAndVotesForTargetAudience.options.any { (it.option.imageUrl != null && it.option.text == null) || (it.option.imageUrl == null && it.option.text != null) }) {
                widgetWithOptionsAndVotesForTargetAudiences.add(
                    widgetWithOptionsAndVotesForTargetAudience.copy(
                        user = fetchUserDetails(
                            widgetWithOptionsAndVotesForTargetAudience.widget.creatorId
                        )
                    )
                )
            }
        }
        preloadImages(widgetWithOptionsAndVotesForTargetAudiences.map { it ->
            it.options.map { it.option.imageUrl.getAllImages() }.flatten()
        }.flatten())
        widgetDao.insertWidgets(
            widgetWithOptionsAndVotesForTargetAudiences.map { it.widget },
            widgetWithOptionsAndVotesForTargetAudiences.map { it.targetAudienceGender },
            widgetWithOptionsAndVotesForTargetAudiences.map { it.targetAudienceAgeRange },
            widgetWithOptionsAndVotesForTargetAudiences.map { it.targetAudienceLocations }
                .flatten(),
            widgetWithOptionsAndVotesForTargetAudiences.map { it -> it.options.map { it.option } }
                .flatten(),
            widgetWithOptionsAndVotesForTargetAudiences.map { it ->
                it.options.map { it.votes }.flatten()
            }.flatten()
        )
        return@withContext widgetWithOptionsAndVotesForTargetAudiences.any { it.widget.creatorId != currentUserId && it.widget.createdAt > lastUpdatedTime.widgetTime && lastUpdatedTime.widgetTime > 0 }
    }

    suspend fun getWidgetDetails(
        widgetId: String,
        fetchUserDetails: suspend (String) -> User,
        preloadImages: suspend (List<String>) -> Unit
    ): WidgetWithOptionsAndVotesForTargetAudience =
        withContext(dispatcher) {
            widgetDao.getWidgetById(widgetId) ?: widgetDataSource.getItem(widgetId)
                .also { widgetWithOptionsAndVotesForTargetAudience ->
                    widgetWithOptionsAndVotesForTargetAudience.copy(
                        user = fetchUserDetails(
                            widgetWithOptionsAndVotesForTargetAudience.widget.creatorId
                        )
                    ).let { widgetWithOptionsAndVotesForTargetAudience1 ->
                        preloadImages(
                            widgetWithOptionsAndVotesForTargetAudience1.options.map { it.option.imageUrl.getAllImages() }
                                .flatten()
                        )
                        widgetDao.insertWidget(
                            widgetWithOptionsAndVotesForTargetAudience1.widget,
                            widgetWithOptionsAndVotesForTargetAudience1.targetAudienceGender,
                            widgetWithOptionsAndVotesForTargetAudience1.targetAudienceAgeRange,
                            widgetWithOptionsAndVotesForTargetAudience1.targetAudienceLocations,
                            widgetWithOptionsAndVotesForTargetAudience1.options.map { it.option },
                            widgetWithOptionsAndVotesForTargetAudience1.options.map { it.votes }
                                .flatten()
                        )
                    }
                }
        }

    suspend fun vote(widgetId: String, optionId: String, userId: String) =
        withContext(dispatcher) {
            var removeVote: Widget.Option.Vote? = null
            widgetDataSource.updateItemFromTransaction(widgetId) { widgetWithOptionsAndVotesForTargetAudience ->
                widgetWithOptionsAndVotesForTargetAudience.copy(options = widgetWithOptionsAndVotesForTargetAudience.options.map { optionWithVotes ->
                    val (option, votes) = optionWithVotes
                    val mutableVotes = votes.toMutableList()
                    val index = mutableVotes.indexOfFirst { it.userId == userId }
                    if (index != -1) {
                        removeVote = mutableVotes.removeAt(index)
                        optionWithVotes.copy(votes = mutableVotes)
                    } else if (option.id == optionId) {
                        optionWithVotes.copy(
                            votes = votes + Widget.Option.Vote(
                                userId = userId, optionId = optionId
                            )
                        )
                    } else {
                        optionWithVotes
                    }
                })
            }.also { widgetWithOptionsAndVotesForTargetAudience ->
                removeVote?.let { widgetDao.deleteVote(it) }
                widgetDao.insertWidget(
                    widgetWithOptionsAndVotesForTargetAudience.widget,
                    widgetWithOptionsAndVotesForTargetAudience.targetAudienceGender,
                    widgetWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange,
                    widgetWithOptionsAndVotesForTargetAudience.targetAudienceLocations,
                    widgetWithOptionsAndVotesForTargetAudience.options.map { it.option },
                    widgetWithOptionsAndVotesForTargetAudience.options.map { it.votes }
                        .flatten()
                )
            }.also {
                widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
                    updatedTime.copy(voteTime = System.currentTimeMillis())
                }
            }
        }

    suspend fun clearAll() = withContext(dispatcher) {
        widgetDao.clearData()
    }
}