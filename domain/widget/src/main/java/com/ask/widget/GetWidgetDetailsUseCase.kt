package com.ask.widget

import android.graphics.Color
import com.ask.core.DISPATCHER_DEFAULT
import com.ask.core.RemoteConfigRepository
import com.ask.country.CountryRepository
import com.ask.user.Gender
import com.ask.user.UserRepository
import com.ask.user.UserWithLocationCategory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import kotlin.random.Random

class GetWidgetDetailsUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository,
    private val userRepository: UserRepository,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val countryRepository: CountryRepository,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        widgetId: String,
        unSpecifiedText: String,
        maleText: String,
        femaleText: String,
        lastVotedEmptyOptions: List<String>,
        preloadImages: suspend (List<String>) -> Unit
    ): WidgetDetailsWithResult = withContext(dispatcher) {
        val widgetWithOptionsAndVotesForTargetAudience = widgetRepository.getWidgetDetails(
            widgetId, userRepository.getCurrentUserId(), { userIds ->
                userRepository.getUserDetailList(userIds, true, preloadImages)
            }, preloadImages
        ).setupData(userRepository.getCurrentUserId(), true, lastVotedEmptyOptions.random())
        if (widgetWithOptionsAndVotesForTargetAudience.isWidgetEnd) {
            WidgetDetailsWithResult(
                widgetWithOptionsAndVotesForTargetAudience = widgetWithOptionsAndVotesForTargetAudience
            )
        } else {
            val ageMin = remoteConfigRepository.getAgeRangeMin()
            val ageMax = remoteConfigRepository.getAgeRangeMax()
            val totalVotes = widgetWithOptionsAndVotesForTargetAudience.widgetTotalVotes
            val userIds =
                widgetWithOptionsAndVotesForTargetAudience.options.map { optionWithVotes -> optionWithVotes.votes.map { it.userId } }
                    .flatten()
            val voteUsers = userRepository.getUserDetailList(userIds, false, preloadImages)
            val optionVotesMap: Map<String, List<UserWithLocationCategory>> =
                widgetWithOptionsAndVotesForTargetAudience.options.mapIndexed { index, optionWithVotes ->
                    ((optionWithVotes.option.text ?: optionWithVotes.option.imageUrl)
                        ?: "Option $index") to optionWithVotes.votes.map { vote -> voteUsers.find { vote.userId == it.user.id }!! }
                }.toMap()
            val genderFilter =
                widgetWithOptionsAndVotesForTargetAudience.targetAudienceGender.gender
            val min =
                widgetWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.min.takeIf { it > 0 }
                    ?: ageMin
            val max =
                widgetWithOptionsAndVotesForTargetAudience.targetAudienceAgeRange.max.takeIf { it > 0 }
                    ?: ageMax
            val countries =
                ((widgetWithOptionsAndVotesForTargetAudience.targetAudienceLocations.mapNotNull { it.country }
                    .takeIf { it.isNotEmpty() }) ?: (countryRepository.getCountryList()
                    .map { it.name }))
            WidgetDetailsWithResult(
                widgetWithOptionsAndVotesForTargetAudience = widgetWithOptionsAndVotesForTargetAudience,
                widgetResults = mapOf(
                    "Result (By Votes)" to widgetWithOptionsAndVotesForTargetAudience.options.mapIndexed { index, optionWithVotes ->
                        WidgetDetailsWithResult.WidgetResult(
                            (index + 1).toString(),
                            optionWithVotes.totalVotes,
                            Color.argb(
                                255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)
                            ),
                            360 * optionWithVotes.totalVotes.toFloat() / totalVotes.toFloat(),
                            optionWithVotes.votesPercentFormat
                        )
                    },
                    "Result (By Gender)" to getWidgetResultListForGender(
                        maleText,
                        femaleText,
                        unSpecifiedText,
                        genderFilter,
                        voteUsers
                    ),
                    "Result (By Age)" to getWidgetResultListForAge(
                        min,
                        max,
                        unSpecifiedText,
                        voteUsers
                    ),
                    "Result (By Location)" to getWidgetResultListForLocation(
                        countries,
                        unSpecifiedText,
                        voteUsers
                    )
                ),
                widgetOptionResults = optionVotesMap.map { entry ->
                    val (key, users) = entry
                    key to mapOf(
                        "By Gender" to getWidgetResultListForGender(
                            maleText,
                            femaleText,
                            unSpecifiedText,
                            genderFilter,
                            users
                        ),
                        "By Age" to getWidgetResultListForAge(min, max, unSpecifiedText, users),
                        "By Location" to getWidgetResultListForLocation(
                            countries,
                            unSpecifiedText,
                            users
                        )
                    )
                }.toMap(),
            )
        }
    }

    private fun getWidgetResultListForGender(
        maleText: String,
        femaleText: String,
        unSpecifiedText: String,
        genderFilter: Widget.GenderFilter,
        voteUsers: List<UserWithLocationCategory>
    ): List<WidgetDetailsWithResult.WidgetResult> {
        return if (genderFilter == Widget.GenderFilter.ALL) {
            listOf(
                getVotesWithPercent(
                    maleText, voteUsers
                ) { it.user.gender != null && it.user.gender == Gender.MALE },
                getVotesWithPercent(
                    femaleText,
                    voteUsers,
                ) {
                    it.user.gender != null && it.user.gender == Gender.FEMALE
                },
                getVotesWithPercent(
                    unSpecifiedText,
                    voteUsers,
                ) {
                    it.user.gender == null
                },
            ).filter { it.count > 0 }
        } else {
            emptyList()
        }
    }

    private fun getWidgetResultListForAge(
        min: Int,
        max: Int,
        unSpecifiedText: String,
        voteUsers: List<UserWithLocationCategory>
    ): List<WidgetDetailsWithResult.WidgetResult> {
        return (min..max).map { age ->
            getVotesWithPercent(
                age.toString(), voteUsers
            ) {
                it.user.age != null && it.user.age == age
            }
        }.filter { it.count > 0 } + listOf(getVotesWithPercent(
            unSpecifiedText, voteUsers
        ) {
            it.user.age == null
        })
    }

    private fun getWidgetResultListForLocation(
        countries: List<String>,
        unSpecifiedText: String,
        voteUsers: List<UserWithLocationCategory>
    ): List<WidgetDetailsWithResult.WidgetResult> {
        return countries.map { country ->
            getVotesWithPercent(
                country, voteUsers
            ) {
                it.userLocation.country != null && it.userLocation.country == country
            }

        }.filter { it.count > 0 } + listOf(getVotesWithPercent(
            unSpecifiedText, voteUsers
        ) {
            it.userLocation.country == null
        })
    }

    private fun getVotesWithPercent(
        title: String,
        voteUsers: List<UserWithLocationCategory>,
        predicateForFilter: (UserWithLocationCategory) -> Boolean
    ): WidgetDetailsWithResult.WidgetResult {
        val votes = voteUsers.filter(predicateForFilter).size
        val votePercent =
            if (votes > 0 && voteUsers.isNotEmpty()) (votes.toFloat() / voteUsers.size.toFloat()) * 100
            else 0f
        return WidgetDetailsWithResult.WidgetResult(
            title, votes, Color.argb(
                255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)
            ), 360 * votes.toFloat() / voteUsers.size.toFloat(), votePercent.toPercentage()
        )
    }
}

