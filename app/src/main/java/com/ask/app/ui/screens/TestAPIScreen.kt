package com.ask.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ask.user.UserWithLocation
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.user.UserRepository
import com.ask.widget.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestAPIScreen(testAPIViewModel: TestAPIViewModel = hiltViewModel()) {
    val testAPIState by testAPIViewModel.testAPIStateFlow.collectAsState()
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Test API") }, actions = {
            IconButton(onClick = { testAPIViewModel.performTest() }) {
                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Refresh")
            }
        })
    }) {
        Column(modifier = Modifier.padding(it)) {
            for (state in testAPIState) {
                TestAPIProgressView(
                    title = when (state) {
                        is TestAPIState.CreatePollState -> "Create Poll"
                        is TestAPIState.CreateUserState -> "Create User"
                        is TestAPIState.DeletePollState -> "Delete Poll"
                        TestAPIState.InitState -> "Init"
                        is TestAPIState.UpdatePollState -> "Update Poll"
                        is TestAPIState.UpdateUserState -> "Update User"
                    },
                    desc = state.error ?: state.message ?: "",
                    loading = state.loading
                )
            }
        }
    }
}

@Composable
fun TestAPIProgressView(title: String, desc: String, loading: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = TextStyle(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.size(5.dp))
            Text(text = desc)
        }
        if (loading) CircularProgressIndicator()
    }
}

@Preview
@Composable
fun TestAPIProgressViewPreview() {
    TestAPIProgressView(title = "Title", desc = "message", loading = true)
}


@HiltViewModel
class TestAPIViewModel @Inject constructor(
    private val widgetRepository: com.ask.widget.WidgetRepository, private val userRepository: com.ask.user.UserRepository
) : ViewModel() {

    private val _createUserStateFlow = MutableStateFlow(TestAPIState.CreateUserState())
    private val _updateUserStateFlow = MutableStateFlow(TestAPIState.UpdateUserState())
    private val _createPollStateFlow = MutableStateFlow(TestAPIState.CreatePollState())
    private val _updatePollStateFlow = MutableStateFlow(TestAPIState.UpdatePollState())
    private val _deletePollStateFlow = MutableStateFlow(TestAPIState.DeletePollState())

    val testAPIStateFlow = combine(
        _createUserStateFlow,
        _updateUserStateFlow,
        _createPollStateFlow,
        _updatePollStateFlow,
        _deletePollStateFlow
    ) { createUser, updateUser, createPoll, updatePoll, deletePoll ->
        listOf(createUser, updateUser, createPoll, updatePoll, deletePoll)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(TestAPIState.InitState))

    init {
        performTest()
    }

    fun performTest() {
        viewModelScope.launch {
            // Create User
            _createUserStateFlow.value = TestAPIState.CreateUserState(loading = true)
            var currentUserWithLocation: com.ask.user.UserWithLocation? = null
//            try {
            currentUserWithLocation = userRepository.createUser()
            _createUserStateFlow.value =
                TestAPIState.CreateUserState(
                    t = currentUserWithLocation,
                    message = "New User ${currentUserWithLocation.user.id}"
                )
//            } catch (e: Exception) {
//                _createUserStateFlow.value =
//                    TestAPIState.CreateUserState(error = e.message ?: "Something went wrong")
//            }
            // Create User

            // Update User
            _updateUserStateFlow.value = TestAPIState.UpdateUserState(loading = true)
//            try {

            /*val updatedUser = userRepository.updateUser(
                currentUserWithLocation?.copy(
                    user = currentUserWithLocation.user.copy(
                        age = Random.nextInt(18, 90),
                        gender = Gender.entries.random(),
                    ),
                    userLocation = currentUserWithLocation.userLocation.copy(
                        country = "India"
                    )
                ) ?: throw Exception("User not found")
            )*/
            /*_updateUserStateFlow.value = TestAPIState.UpdateUserState(
                t = updatedUser,
                message = "User Updated successfully"
            )*/
//            } catch (e: Exception) {
//                _updateUserStateFlow.value =
//                    TestAPIState.UpdateUserState(error = e.message ?: "Something went wrong")
//            }
            // Update User

            // Create Poll
            _createPollStateFlow.value = TestAPIState.CreatePollState(loading = true)
            var createdPoll: com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience? = null
//            try {
            val widget = com.ask.widget.Widget(
                creatorId = currentUserWithLocation!!.user.id,
                title = "Test Title ${Random.nextInt()}",
            )
            val options = List(Random.nextInt(2, 6)) {
                val isText = Random.nextBoolean()
                com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                    option = com.ask.widget.Widget.Option(
                        widgetId = widget.id,
                        text = when (isText) {
                            true -> "Option $it"
                            else -> null
                        },
                        imageUrl = when (isText) {
                            false -> "https://dummyimage.com/600x400/000/fff&text=$it"
                            else -> null
                        }
                    ), votes = emptyList()
                )
            }
            val targetAudienceGender = com.ask.widget.Widget.TargetAudienceGender(
                gender = com.ask.widget.Widget.GenderFilter.entries.random(),
                widgetId = widget.id
            )
            val targetAudienceAgeRange = com.ask.widget.Widget.TargetAudienceAgeRange(
                widgetId = widget.id,
                min = when (Random.nextBoolean()) {
                    true -> Random.nextInt(18, 50)
                    else -> 0
                },
                max = when (Random.nextBoolean()) {
                    true -> Random.nextInt(51, 100)
                    else -> 0
                }
            )
            val targetAudienceLocation = listOf(
                com.ask.widget.Widget.TargetAudienceLocation(
                    widgetId = widget.id,
                    country = when (Random.nextBoolean()) {
                        true -> "India"
                        else -> null
                    }
                ),
                com.ask.widget.Widget.TargetAudienceLocation(
                    widgetId = widget.id,
                    country = "India",
                    state = when (Random.nextBoolean()) {
                        true -> "Maharashtra"
                        else -> null
                    }
                ),
                com.ask.widget.Widget.TargetAudienceLocation(
                    widgetId = widget.id,
                    country = "India",
                    state = "Maharashtra",
                    city = when (Random.nextBoolean()) {
                        true -> "Mumbai"
                        else -> null
                    }
                ),
                com.ask.widget.Widget.TargetAudienceLocation(
                    widgetId = widget.id,
                    country = "India",
                    state = "Maharashtra",
                    city = "Mumbai"
                ),
                com.ask.widget.Widget.TargetAudienceLocation(
                    widgetId = widget.id,
                    country = "India",
                    state = "Punjab"
                )
            ).shuffled().take(Random.nextInt(0, 4))

            /*createdPoll = widgetRepository.createWidget(
                WidgetWithOptionsAndVotesForTargetAudience(
                    widget = widget,
                    options = options,
                    targetAudienceGender = targetAudienceGender,
                    targetAudienceAgeRange = targetAudienceAgeRange,
                    targetAudienceLocations = targetAudienceLocation,
                    user = currentUserWithLocation!!.user
                )
            )*/
            _createPollStateFlow.value = TestAPIState.CreatePollState(
                t = createdPoll,
                message = "poll created successfully"
            )
//            } catch (e: Exception) {
//                _createPollStateFlow.value =
//                    TestAPIState.CreatePollState(error = e.message ?: "Something went wrong")
//            }
            // Create Poll


            // Update Poll
            /*_updatePollStateFlow.value = TestAPIState.UpdatePollState(loading = true)
            var updatedPoll: PollWithOptionsAndVotesForTargetAudience? = null
            try {
                if (createdPoll == null) {
                    throw Exception("Poll not created in previous step")
                }
                updatedPoll = pollRepository.updatePoll(
                    PollWithOptionsAndVotesForTargetAudience(
                        poll = createdPoll.poll.copy(
                            creatorId = currentUser!!.user.id,
                            title = "Test Updated Title ${Random.nextInt()}",
                        ), options = List(Random.nextInt(1, 4)) {
                            PollWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                                option = Poll.Option(
                                    id = "", pollId = "", text = when (Random.nextBoolean()) {
                                        true -> "Option $it"
                                        else -> null
                                    }, imageUrl = when (Random.nextBoolean()) {
                                        true -> "https://dummyimage.com/600x400/000/fff&text=$it"
                                        else -> null
                                    }
                                ), votes = emptyList()
                            )
                        },
                        targetAudience = createdPoll.targetAudience,
                        user = currentUser.user
                    )
                )
                _updatePollStateFlow.value = TestAPIState.UpdatePollState(
                    t = updatedPoll,
                    message = "Poll updated Successfully"
                )
            } catch (e: Exception) {
                _updatePollStateFlow.value =
                    TestAPIState.UpdatePollState(error = e.message ?: "Something went wrong")
            }*/
            // Update Poll

            //Delete Poll
            /* _deletePollStateFlow.value = TestAPIState.DeletePollState(loading = true)
             try {
                 if (updatedPoll == null) {
                     throw Exception("Poll not updated in previous step")
                 }
                 _deletePollStateFlow.value =
                     TestAPIState.DeletePollState(loading = true, t = updatedPoll)
                 pollRepository.deletePoll(updatedPoll)
                 _deletePollStateFlow.value =
                     TestAPIState.DeletePollState(message = "Poll deleted successfully")
             } catch (e: Exception) {
                 _deletePollStateFlow.value =
                     TestAPIState.DeletePollState(error = e.message ?: "Something went wrong")
             }*/
            //Delete Poll

        }
    }
}

sealed class TestAPIState<T>(
    open val loading: Boolean = false,
    open val message: String? = null,
    open val t: T? = null,
    open val error: String? = null
) {
    data object InitState : TestAPIState<Unit>()
    data class CreateUserState(
        override val loading: Boolean = false,
        override val message: String? = null,
        override val t: com.ask.user.UserWithLocation? = null,
        override val error: String? = null
    ) : TestAPIState<com.ask.user.UserWithLocation>(loading, message, t, error)

    data class UpdateUserState(
        override val loading: Boolean = false,
        override val message: String? = null,
        override val t: com.ask.user.UserWithLocation? = null,
        override val error: String? = null
    ) : TestAPIState<com.ask.user.UserWithLocation>(loading, message, t, error)

    data class CreatePollState(
        override val loading: Boolean = false,
        override val t: com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience? = null,
        override val error: String? = null,
        override val message: String? = null
    ) : TestAPIState<com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience>(loading, message, t, error)

    data class UpdatePollState(
        override val loading: Boolean = false,
        override val t: com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience? = null,
        override val error: String? = null,
        override val message: String? = null
    ) : TestAPIState<com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience>(loading, message, t, error)

    data class DeletePollState(
        override val loading: Boolean = false,
        override val t: com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience? = null,
        override val error: String? = null,
        override val message: String? = null,
    ) : TestAPIState<com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience>(loading, message, t, error)
}