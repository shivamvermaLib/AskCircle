package com.ask.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ask.user.FeedbackType
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onProfileClick: () -> Unit, onBack: () -> Unit) {
    val viewModel = hiltViewModel<SettingsViewModel>()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SettingsScreen(
        state, viewModel::onEvent, onProfileClick, onBack,
        viewModel::submitFeedback
    ) {
        scope.launch {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SettingState,
    onEvent: (SettingEvent) -> Unit,
    onProfileClick: () -> Unit,
    onBack: () -> Unit,
    onSendFeedback: (FeedbackType, String) -> Unit = { _, _ -> },
    onGoogleLogin: () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    var showFeedbackDialog by remember { mutableStateOf(false) }

    if (showFeedbackDialog) {
        FeedbackDialog(onDismiss = { showFeedbackDialog = false }, { rating, text ->
            onSendFeedback(rating, text)
            showFeedbackDialog = false
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(all = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.account_settings),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(10.dp))
            SettingsTile(
                title = stringResource(R.string.edit_profile),
                desc = stringResource(R.string.view_and_edit_your_profile_information),
                onClick = onProfileClick
            )
            Spacer(modifier = Modifier.size(10.dp))
            ConnectTile(
                title = "Connect Via Account",
                desc = "Connect and sync your data, making it accessible from any device you log in to.",
                onClick = onGoogleLogin
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = stringResource(R.string.notification_settings),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(10.dp))
            SwitchWithTitleAndDesc(
                title = stringResource(R.string.new_widget),
                desc = stringResource(R.string.when_a_new_widget_is_created),
                state = state.notificationSettings.newWidgetNotification,
                onStateChange = { onEvent(SettingEvent.ToggleNewWidgetNotification(it)) }
            )
            SwitchWithTitleAndDesc(
                title = stringResource(R.string.vote_on_your_widget),
                desc = stringResource(R.string.when_someone_vote_on_your_widget),
                state = state.notificationSettings.voteOnYourWidgetNotification,
                onStateChange = {
                    onEvent(
                        SettingEvent.ToggleVoteOnYourWidgetNotification(
                            it
                        )
                    )
                }
            )
            /*SwitchWithTitleAndDesc(
                title = stringResource(R.string.widget_result),
                desc = stringResource(R.string.when_the_widget_you_vote_is_done),
                state = state.notificationSettings.widgetResultNotification,
                onStateChange = { onEvent(SettingEvent.ToggleWidgetResultNotification(it)) }
            )*/
            SwitchWithTitleAndDesc(
                title = stringResource(R.string.widget_time_end),
                desc = stringResource(R.string.when_your_widget_time_ends),
                state = state.notificationSettings.widgetTimeEndNotification,
                onStateChange = {
                    onEvent(
                        SettingEvent.ToggleWidgetTimeEndNotification(
                            it
                        )
                    )
                }
            )
            SwitchWithTitleAndDesc(
                title = stringResource(R.string.voting_reminder),
                desc = stringResource(R.string.when_you_get_a_reminder_to_vote),
                state = state.notificationSettings.votingReminderNotification,
                onStateChange = {
                    onEvent(
                        SettingEvent.ToggleVotingReminderNotification(
                            it
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.size(10.dp))
            SettingsTile(
                title = stringResource(R.string.feedback),
                desc = stringResource(R.string.tell_us_about_your_experience_or_any_feedback),
                onClick = {
                    showFeedbackDialog = true
                }
            )
            Spacer(modifier = Modifier.size(10.dp))
            SettingsTile(title = stringResource(R.string.version), desc = state.version)
            Spacer(modifier = Modifier.size(10.dp))
            SettingsTile(
                title = stringResource(R.string.privacy_policy),
                desc = "",
                onClick = {
                    uriHandler.openUri(state.termsAndConditions)
                })
            Spacer(modifier = Modifier.size(10.dp))
            SettingsTile(
                title = stringResource(R.string.about_us),
                desc = "",
                onClick = {
                    uriHandler.openUri(state.termsAndConditions)
                })
        }
    }
}

@Composable
fun FeedbackDialog(onDismiss: () -> Unit, onSubmit: (FeedbackType, String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(FeedbackType.NORMAL) }
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.feedback),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.size(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FeedbackType.entries.forEach {
                        IconButton(onClick = { rating = it }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(
                                    id = when (it) {
                                        FeedbackType.HAPPY -> R.drawable.face_smile_svgrepo_com
                                        FeedbackType.SAD -> R.drawable.face_sad_svgrepo_com
                                        FeedbackType.NORMAL -> R.drawable.face_svgrepo_com
                                    }
                                ), contentDescription = it.name,
                                tint = if (rating == it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(6.dp))
                TextField(
                    value = text, onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Enter Feedback") },
                    minLines = 3,
                    maxLines = 6
                )
                Spacer(modifier = Modifier.size(10.dp))
                ElevatedButton(onClick = { onSubmit(rating, text) }) {
                    Text(text = "Submit")
                }
            }
        }
    }
}

@Composable
fun SettingsTile(title: String, desc: String, onClick: () -> Unit = {}) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
        .clickable { onClick() }
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = desc, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ConnectTile(title: String, desc: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Column(modifier = Modifier
            .weight(2f)
            .clickable { onClick() }
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = desc, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.weight(1f))
//        IconButton(onClick = onClick) {
//            Icon(
//                imageVector = ImageVector.vectorResource(id = R.drawable.google_178_svgrepo_com),
//                contentDescription = "Google Sign in"
//            )
//        }
    }
}

@Composable
fun SwitchWithTitleAndDesc(
    title: String,
    desc: String?,
    state: Boolean,
    onStateChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            desc?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
        }
        Switch(
            checked = state,
            onCheckedChange = onStateChange
        )
    }
}

@Composable
@Preview
fun SwitchSample() {
    SwitchWithTitleAndDesc(title = "Notification", desc = "kj adkfh kdjh jkfd", state = true) {

    }
}

