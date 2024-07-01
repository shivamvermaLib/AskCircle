package com.ask.app.data.repository

import com.ask.app.checkIfUrl
import com.ask.app.data.models.Poll
import com.ask.app.data.models.PollWithOptionsAndVotesForTargetAudience
import com.ask.app.data.models.UserWithSearchFields
import com.ask.app.data.source.local.PollDao
import com.ask.app.data.source.remote.FirebaseDataSource
import com.ask.app.data.source.remote.FirebaseStorageSource
import com.ask.app.extension
import com.ask.app.fileNameWithExtension
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class PollRepository @Inject constructor(
    private val pollDataSource: FirebaseDataSource<Poll>,
    private val pollTargetAudienceDataSource: FirebaseDataSource<Poll.TargetAudience>,
    private val pollOptionDataSource: FirebaseDataSource<Poll.Option>,
    private val pollOptionVoteDataSource: FirebaseDataSource<Poll.Option.Vote>,
    private val pollDao: PollDao,
    @Named("poll-options") private val pollOptionStorageSource: FirebaseStorageSource,
    @Named("IO") private val dispatcher: CoroutineDispatcher
) {

    suspend fun getPolls() = pollDao.getPolls()

    suspend fun createPoll(
        pollWithOptionsAndVotesForTargetAudience: PollWithOptionsAndVotesForTargetAudience
    ): PollWithOptionsAndVotesForTargetAudience = withContext(dispatcher) {
        val createdPoll = pollDataSource.addItem(pollWithOptionsAndVotesForTargetAudience.poll)
        val createdTargetAudience =
            pollTargetAudienceDataSource.addItem(
                pollWithOptionsAndVotesForTargetAudience.targetAudience.copy(
                    pollId = createdPoll.id
                )
            )
        val createdOptionsWithVotes =
            pollWithOptionsAndVotesForTargetAudience.options.map { optionWithVotes ->
                PollWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                    option = pollOptionDataSource.addItem(
                        optionWithVotes.option.copy(
                            pollId = createdPoll.id,
                        )
                    ).also {
                        pollOptionDataSource.updateItem(
                            it.copy(
                                imageUrl = when (it.imageUrl != null && it.imageUrl.checkIfUrl()
                                    .not()) {
                                    true -> pollOptionStorageSource.upload(
                                        "${it.id}.${it.imageUrl.extension()}", it.imageUrl
                                    )

                                    else -> when (it.imageUrl?.checkIfUrl()) {
                                        true -> it.imageUrl
                                        else -> null
                                    }
                                }
                            )
                        )
                    },
                    votes = optionWithVotes.votes.map { pollOptionVoteDataSource.addItem(it) })
            }
        val createdOptions = createdOptionsWithVotes.map { it.option }
        val createdVotes = createdOptionsWithVotes.map { it.votes }.flatten()

        pollDao.insertPoll(createdPoll, createdOptions, createdTargetAudience, createdVotes)
        pollDao.getPollById(createdPoll.id)
            ?: throw Exception("Unable to get created Poll from Room DB")
    }

    suspend fun updatePoll(pollWithOptionsAndVotesForTargetAudience: PollWithOptionsAndVotesForTargetAudience) =
        withContext(dispatcher) {
            val remoteOldPoll =
                pollDataSource.getItem(pollWithOptionsAndVotesForTargetAudience.poll.id)
            val localOldPoll = pollDao.getPollById(remoteOldPoll.id)
                ?: throw Exception("Unable to found Poll on Local DB")

            val removedPollOptions =
                localOldPoll.options.filter { optionWithVotes -> optionWithVotes.option.id !in pollWithOptionsAndVotesForTargetAudience.options.map { it.option.id } }
            removedPollOptions.forEach { optionWithVotes ->
                optionWithVotes.votes.forEach {
                    pollOptionVoteDataSource.deleteItem(it)
                }
                pollOptionDataSource.deleteItem(optionWithVotes.option)
            }

            val updatedPoll =
                pollDataSource.updateItem(pollWithOptionsAndVotesForTargetAudience.poll)
            val updatedTargetAudience = pollTargetAudienceDataSource.updateItem(
                pollWithOptionsAndVotesForTargetAudience.targetAudience.copy(pollId = updatedPoll.id)
            )

            val updatedOptionsWithVotes =
                pollWithOptionsAndVotesForTargetAudience.options.map { optionWithVotes ->
                    PollWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = pollOptionDataSource.updateItem(optionWithVotes.option.copy(pollId = updatedPoll.id)),
                        votes = optionWithVotes.votes.map { pollOptionVoteDataSource.updateItem(it) })
                }
            val updatedOptions = updatedOptionsWithVotes.map { it.option }
            val updatedVotes = updatedOptionsWithVotes.map { it.votes }.flatten()
            pollDao.insertPoll(updatedPoll, updatedOptions, updatedTargetAudience, updatedVotes)
            pollDao.getPollById(updatedPoll.id)
                ?: throw Exception("Unable to get created Poll from Room DB")
        }

    suspend fun deletePoll(pollWithOptionsAndVotesForTargetAudience: PollWithOptionsAndVotesForTargetAudience) =
        withContext(dispatcher) {
            pollWithOptionsAndVotesForTargetAudience.options.forEach { optionWithVotes ->
                optionWithVotes.votes.forEach { vote ->
                    pollOptionVoteDataSource.deleteItem(vote).also {
                        println("Vote Deleted")
                        pollDao.deletePollOptionVotes(vote)
                    }
                }
                pollOptionDataSource.deleteItem(optionWithVotes.option).also {
                    optionWithVotes.option.imageUrl?.let {
                        if (it.checkIfUrl() && it.contains("firebase")) {
                            pollOptionStorageSource.delete(it.fileNameWithExtension())
                        }
                    }
                    println("Option Deleted")
                    pollDao.deletePollOptions(optionWithVotes.option)
                }
            }
            pollTargetAudienceDataSource.deleteItem(pollWithOptionsAndVotesForTargetAudience.targetAudience)
                .also {
                    println("Poll Target Audience Deleted")
                    pollDao.deletePollTargetAudience(pollWithOptionsAndVotesForTargetAudience.targetAudience)
                }
            pollDataSource.deleteItem(pollWithOptionsAndVotesForTargetAudience.poll)
                .also {
                    println("Poll Deleted")
                    pollDao.deletePoll(pollWithOptionsAndVotesForTargetAudience.poll)
                }

        }

    suspend fun refreshPolls(userWithSearchFields: UserWithSearchFields) {
        /*var hasData = true
        var previousKey: String? = null
        while (hasData) {
            val targetAudienceList = pollTargetAudienceDataSource.getItemsByKey(
                "createdAt",
                previousKey
            ).filter { targetAudience ->
                targetAudience.gender?.let { it == userWithSearchFields.userSearchFields.gender } ?: true
                    &&
                    targetAudience.ageRange?.let { userWithSearchFields.userSearchFields.age in it.min..it.max } ?: true
                    &&
                    targetAudience.location?.let { location ->
                        (location.country?.let { it.lowercase() == userWithSearchFields.userSearchFields.location?.country?.lowercase() }
                            ?: true)
                            && (location.state?.let { it.lowercase() == userWithSearchFields.userSearchFields.location?.state?.lowercase() }
                            ?: true)
                            && (location.city?.let { it.lowercase() == userWithSearchFields.userSearchFields.location?.city?.lowercase() }
                            ?: true)
                    } ?: true
            }
            println("list:${targetAudienceList.size}")
            targetAudienceList.forEach { targetAudience ->
                val poll = pollDataSource.getItem(targetAudience.pollId)
                val options = pollOptionDataSource.queryItem("pollId", poll.id)
                pollDao.insertPoll(
                    poll,
                    options,
                    targetAudience,
                    options.map { pollOptionVoteDataSource.queryItem("optionId", it.id) }.flatten()
                )
            }
            targetAudienceList.lastOrNull().let {
                previousKey = it?.id
            }
            hasData = targetAudienceList.isNotEmpty()
        }*/


    }

    suspend fun clear() = withContext(dispatcher) {
        pollDataSource.clear()
        pollTargetAudienceDataSource.clear()
        pollOptionDataSource.clear()
        pollOptionVoteDataSource.clear()
    }
}