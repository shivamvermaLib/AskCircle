package com.ask.app.data.repository

import com.ask.app.DOT
import com.ask.app.TABLE_WIDGETS
import com.ask.app.checkIfUrl
import com.ask.app.data.models.User
import com.ask.app.data.models.Widget
import com.ask.app.data.models.WidgetId
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.app.data.models.generateCombinationsForWidget
import com.ask.app.data.source.local.WidgetDao
import com.ask.app.data.source.remote.FirebaseDataSource
import com.ask.app.data.source.remote.FirebaseStorageSource
import com.ask.app.fileNameWithExtension
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class WidgetRepository @Inject constructor(
    private val widgetDataSource: FirebaseDataSource<WidgetWithOptionsAndVotesForTargetAudience>,
    private val widgetIdDataSource: FirebaseDataSource<WidgetId>,
    private val widgetDao: WidgetDao,
    @Named(TABLE_WIDGETS) private val pollOptionStorageSource: FirebaseStorageSource,
    @Named("IO") private val dispatcher: CoroutineDispatcher
) {

    fun getUserWidgets(userId: String) = widgetDao.getUserWidgets(userId)
        .flowOn(dispatcher)

    fun getWidgets() = widgetDao.getWidgets()
        .flowOn(dispatcher)

    suspend fun createWidget(
        widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
        getExtension: (String) -> String,
        getByteArray: (String) -> ByteArray
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
                options = widgetWithOptionsAndVotesForTargetAudience.options.map {
                    it.copy(
                        votes = it.votes.map { vote -> vote.copy(optionId = it.option.id) },
                        option = it.option.copy(
                            widgetId = pollId,
                            imageUrl = when (it.option.imageUrl != null && it.option.imageUrl.checkIfUrl()
                                .not()) {
                                true -> pollOptionStorageSource.upload(
                                    "${it.option.id}$DOT${getExtension(it.option.imageUrl)}",
                                    getByteArray(it.option.imageUrl)
                                )

                                else -> when (it.option.imageUrl?.checkIfUrl()) {
                                    true -> it.option.imageUrl
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
                createdPollWithOptionsAndVotesForTargetAudience.widget.creatorId
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
        }

    suspend fun syncWidgetsFromServer(
        searchCombinations: List<String>, fetchUserDetails: suspend (String) -> User
    ) = withContext(dispatcher) {
        val widgetIds = searchCombinations.map {
            async {
                widgetIdDataSource.getItemOrNull(it)
            }
        }.awaitAll().asSequence().filterNotNull().distinct().map { it.widgetIds }.flatten()
            .distinct()

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

        widgetDao.insertWidgets(widgetWithOptionsAndVotesForTargetAudiences.map { it.widget },
            widgetWithOptionsAndVotesForTargetAudiences.map { it.targetAudienceGender },
            widgetWithOptionsAndVotesForTargetAudiences.map { it.targetAudienceAgeRange },
            widgetWithOptionsAndVotesForTargetAudiences.map { it.targetAudienceLocations }
                .flatten(),
            widgetWithOptionsAndVotesForTargetAudiences.map { widgetWithOptionsAndVotesForTargetAudience -> widgetWithOptionsAndVotesForTargetAudience.options.map { it.option } }
                .flatten(),
            widgetWithOptionsAndVotesForTargetAudiences.map { widgetWithOptionsAndVotesForTargetAudience ->
                widgetWithOptionsAndVotesForTargetAudience.options.map { it.votes }.flatten()
            }.flatten()
        )
    }


    suspend fun vote(widgetId: String, optionId: String, userId: String) = withContext(dispatcher) {
        widgetDataSource.updateItemFromTransaction(widgetId) { widgetWithOptionsAndVotesForTargetAudience ->
            widgetWithOptionsAndVotesForTargetAudience.copy(options = widgetWithOptionsAndVotesForTargetAudience.options.map { optionWithVotes ->
                val (option, votes) = optionWithVotes
                val mutableVotes = votes.toMutableList()
                if (mutableVotes.removeIf { it.userId == userId }) {
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
        }
    }.also { widgetWithOptionsAndVotesForTargetAudience ->
        widgetDao.insertWidget(
            widgetWithOptionsAndVotesForTargetAudience.widget,
            widgetWithOptionsAndVotesForTargetAudience.targetAudienceGender,
            widgetWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange,
            widgetWithOptionsAndVotesForTargetAudience.targetAudienceLocations,
            widgetWithOptionsAndVotesForTargetAudience.options.map { it.option },
            widgetWithOptionsAndVotesForTargetAudience.options.map { it.votes }.flatten()
        )
    }
}