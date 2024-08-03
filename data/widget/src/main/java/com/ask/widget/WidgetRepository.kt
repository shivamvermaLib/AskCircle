package com.ask.widget


import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.ask.core.DISPATCHER_IO
import com.ask.core.DOT
import com.ask.core.FirebaseDataSource
import com.ask.core.FirebaseOneDataSource
import com.ask.core.FirebaseStorageSource
import com.ask.core.IMAGE_SPLIT_FACTOR
import com.ask.core.ImageSizeType
import com.ask.core.UNDERSCORE
import com.ask.core.UpdatedTime
import com.ask.core.checkIfFirebaseUrl
import com.ask.core.fileNameWithExtension
import com.ask.core.getAllImages
import com.ask.core.isUpdateRequired
import com.ask.user.UserWithLocationCategory
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
    @Named(DISPATCHER_IO) private val dispatcher: CoroutineDispatcher
) {

    fun getUserWidgets(userId: String, limit: Int) = Pager(config = PagingConfig(pageSize = limit),
        pagingSourceFactory = { widgetDao.getUserWidgets(userId) }).flow

    fun getWidgets(currentUserId: String, currentTime: Long, limit: Int) =
        Pager(config = PagingConfig(pageSize = limit),
            pagingSourceFactory = { widgetDao.getWidgets(currentUserId, currentTime) }).flow

    fun getMostVotedWidgets(currentUserId: String, limit: Int) =
        Pager(config = PagingConfig(pageSize = limit),
            pagingSourceFactory = { widgetDao.getMostVotedWidgets(currentUserId) }).flow

    fun getTrendingWidgets(currentUserId: String, limit: Int) =
        Pager(config = PagingConfig(pageSize = limit),
            pagingSourceFactory = { widgetDao.getTrendingWidgets(currentUserId) }).flow

    fun getBookmarkedWidgets(currentUserId: String, limit: Int) =
        Pager(config = PagingConfig(pageSize = limit),
            pagingSourceFactory = { widgetDao.getBookmarkedWidgets(currentUserId) }).flow

    suspend fun createWidget(
        widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
        currentUserId: String,
        getExtension: (String) -> String,
        getByteArrays: suspend (String) -> Map<ImageSizeType, ByteArray>,
        preloadImages: suspend (List<String>) -> Unit
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
                            imageUrl = when (optionWithVotes.option.imageUrl != null) {
                                true -> getByteArrays(optionWithVotes.option.imageUrl).map {
                                    async {
                                        pollOptionStorageSource.upload(
                                            "${optionWithVotes.option.id}$UNDERSCORE${it.key.name}$DOT${
                                                getExtension(optionWithVotes.option.imageUrl)
                                            }", it.value
                                        )
                                    }
                                }.awaitAll().joinToString(separator = IMAGE_SPLIT_FACTOR)

                                else -> when (optionWithVotes.option.imageUrl?.checkIfFirebaseUrl()) {
                                    true -> optionWithVotes.option.imageUrl
                                    else -> null
                                }
                            }
                        )
                    )
                })
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
            }.let { list ->
                list + async {
                    preloadImages(createdPollWithOptionsAndVotesForTargetAudience.options.map { it.option.imageUrl.getAllImages() }.flatten())
                } + async {
                    widgetDao.insertWidget(createdPollWithOptionsAndVotesForTargetAudience.widget,
                        createdPollWithOptionsAndVotesForTargetAudience.targetAudienceGender,
                        createdPollWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange,
                        createdPollWithOptionsAndVotesForTargetAudience.targetAudienceLocations,
                        createdPollWithOptionsAndVotesForTargetAudience.options.map { it.option },
                        createdPollWithOptionsAndVotesForTargetAudience.options.map { it.votes }
                            .flatten())
                } + async {
                    widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
                        updatedTime.copy(widgetTime = System.currentTimeMillis())
                    }
                }
            }
        }.awaitAll()


        widgetDao.getWidgetById(pollId, currentUserId)
            ?: throw Exception("Unable to get created Poll from Local")
    }

    suspend fun deleteWidget(widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience) =
        withContext(dispatcher) {
            widgetWithOptionsAndVotesForTargetAudience.options.forEach { optionWithVotes ->
                optionWithVotes.option.imageUrl?.let {
                    if (it.checkIfFirebaseUrl()) {
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
        currentUserId: String,
        lastUpdatedTime: UpdatedTime,
        searchCombinations: Set<String>,
        fetchUsersDetails: suspend (List<String>) -> List<UserWithLocationCategory>,
        preloadImages: suspend (List<String>) -> Unit,
        onNotification: (NotificationType) -> Unit,
    ) = withContext(dispatcher) {
        val widgetIds = searchCombinations.map {
            async {
                widgetIdDataSource.getItemOrNull(it)
            }
        }.awaitAll().asSequence().filterNotNull().distinct().map { it.widgetIds }.flatten()
            .distinct().toList()

        var widgetWithOptionsAndVotesForTargetAudiences =
            widgetIds.map { async { widgetDataSource.getItem(it) } }.awaitAll()
                .filter { widgetWithOptionsAndVotesForTargetAudience ->
                    widgetWithOptionsAndVotesForTargetAudience.options.any { (it.option.imageUrl != null && it.option.text == null) || (it.option.imageUrl == null && it.option.text != null) }
                }

        val users =
            fetchUsersDetails(widgetWithOptionsAndVotesForTargetAudiences.map { it.widget.creatorId })

        widgetWithOptionsAndVotesForTargetAudiences =
            widgetWithOptionsAndVotesForTargetAudiences.map { widget ->
                widget.copy(
                    user = users.find { it.user.id == widget.widget.creatorId }!!.user
                )
            }
        val totalWidgetsCount = widgetDao.getWidgetsCount()
        val totalUserWidgetVoteCount = widgetDao.getUserWidgetsVoteCount(currentUserId)
        val widgetIdsNotVotedByUser = widgetDao.getWidgetIdsOnWhichUserNotVoted(currentUserId)
        awaitAll(async {
            fetchUsersDetails(widgetWithOptionsAndVotesForTargetAudiences.filter { it.isWidgetEnd }
                .map { widgetWithOptionsAndVotesForTargetAudience ->
                    widgetWithOptionsAndVotesForTargetAudience.options.map { optionWithVotes ->
                        optionWithVotes.votes.map { it.userId }
                    }.flatten()
                }.flatten())
        }, async {
            preloadImages(widgetWithOptionsAndVotesForTargetAudiences.map { it ->
                it.options.map { it.option.imageUrl.getAllImages() }.flatten()
            }.flatten())
        }, async {
            widgetDao.insertWidgets(widgetWithOptionsAndVotesForTargetAudiences.map { it.widget },
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
        })
        if (widgetWithOptionsAndVotesForTargetAudiences.size > totalWidgetsCount) {
            onNotification(NotificationType.NEW_WIDGETS)
        }
        if (totalUserWidgetVoteCount < widgetWithOptionsAndVotesForTargetAudiences.map { it.options.map { it.votes.size } }
                .flatten().size) {
            onNotification(NotificationType.USER_VOTED_ON_YOUR_WIDGET)
        }
        if (widgetIdsNotVotedByUser.isNotEmpty()) {
            onNotification(NotificationType.USER_NOT_VOTED_ON_WIDGET_REMINDER)
        }

//        return@withContext widgetWithOptionsAndVotesForTargetAudiences.any { it.widget.creatorId != currentUserId && it.widget.createdAt > lastUpdatedTime.widgetTime && lastUpdatedTime.widgetTime > 0 }
    }

    suspend fun getWidgetDetails(
        widgetId: String,
        currentUserId: String,
        fetchUsersDetails: suspend (List<String>) -> List<UserWithLocationCategory>,
        preloadImages: suspend (List<String>) -> Unit
    ): WidgetWithOptionsAndVotesForTargetAudience = withContext(dispatcher) {
        (widgetDao.getWidgetById(widgetId, currentUserId)
            ?: widgetDataSource.getItem(widgetId)).let { widgetWithOptionsAndVotesForTargetAudience ->
            widgetWithOptionsAndVotesForTargetAudience.copy(
                user = fetchUsersDetails(
                    listOf(widgetWithOptionsAndVotesForTargetAudience.widget.creatorId)
                ).find { it.user.id == widgetWithOptionsAndVotesForTargetAudience.widget.creatorId }!!.user
            ).also { widgetWithOptionsAndVotesForTargetAudience1 ->
                if (widgetWithOptionsAndVotesForTargetAudience1.isWidgetEnd) {
                    fetchUsersDetails(widgetWithOptionsAndVotesForTargetAudience1.options.map { optionWithVotes -> optionWithVotes.votes.map { it.userId } }
                        .flatten())
                }
                val imagesPreload = async {
                    preloadImages(widgetWithOptionsAndVotesForTargetAudience1.options.map { it.option.imageUrl.getAllImages() }
                        .flatten())
                }
                val widgetInsertion = async {
                    widgetDao.insertWidget(widgetWithOptionsAndVotesForTargetAudience1.widget,
                        widgetWithOptionsAndVotesForTargetAudience1.targetAudienceGender,
                        widgetWithOptionsAndVotesForTargetAudience1.targetAudienceAgeRange,
                        widgetWithOptionsAndVotesForTargetAudience1.targetAudienceLocations,
                        widgetWithOptionsAndVotesForTargetAudience1.options.map { it.option },
                        widgetWithOptionsAndVotesForTargetAudience1.options.map { it.votes }
                            .flatten())
                }
                awaitAll(imagesPreload, widgetInsertion)
            }
        }
    }

    suspend fun vote(widgetId: String, optionId: String, userId: String) = withContext(dispatcher) {
        /*widgetDao.getWidgetById(widgetId, userId)?.let { widgetWithOptionsAndVotesForTargetAudience ->
                var removeVote: Widget.Option.Vote? = null
                widgetWithOptionsAndVotesForTargetAudience.copy(
                    options = widgetWithOptionsAndVotesForTargetAudience.options.map { optionWithVotes ->
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
                    }
                ).let { widgetWithOptionsAndVotesForTargetAudience1 ->
                    removeVote?.let { widgetDao.deleteVote(it) }
                    widgetDao.insertVotes(widgetWithOptionsAndVotesForTargetAudience1.options.map { it.votes }.flatten())
                }
            }*/

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
            widgetDao.insertVotes(widgetWithOptionsAndVotesForTargetAudience.options.map { it.votes }
                .flatten())
        }.also {
            widgetUpdateTimeOneDataSource.updateItemFromTransaction { updatedTime ->
                updatedTime.copy(voteTime = System.currentTimeMillis())
            }
        }
    }

    suspend fun clearAll() = withContext(dispatcher) {
        widgetDao.clearData()
    }

    suspend fun startStopVoting(widgetId: String, isStart: Boolean) = withContext(dispatcher) {
        widgetDataSource.updateItemFromTransaction(widgetId) { widgetWithOptionsAndVotesForTargetAudience1 ->
            if (isStart) {
                widgetWithOptionsAndVotesForTargetAudience1.copy(
                    widget = widgetWithOptionsAndVotesForTargetAudience1.widget.copy(
                        startAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                widgetWithOptionsAndVotesForTargetAudience1.copy(
                    widget = widgetWithOptionsAndVotesForTargetAudience1.widget.copy(
                        endAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }.also {
            widgetDao.insertWidget(it.widget)
        }
    }
}

enum class NotificationType {
    NEW_WIDGETS, USER_VOTED_ON_YOUR_WIDGET, USER_NOT_VOTED_ON_WIDGET_REMINDER,
}