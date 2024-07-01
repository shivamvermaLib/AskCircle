package com.ask.app.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ask.app.R
import com.ask.app.data.models.Poll
import com.ask.app.data.models.PollWithOptionsAndVotesForTargetAudience
import com.ask.app.data.models.User

@Composable
fun HomeScreen() {

}

@Preview
@Composable
fun PollWidget(
    @PreviewParameter(PollWidgetPreviewParameterProvider::class) poll: PollWithOptionsAndVotesForTargetAudience
) {
    Card {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(poll.user.profilePic)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.baseline_account_circle_24),
                    contentDescription = "Profile Pic",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clip(CircleShape)
                )
                Text(text = poll.user.name, modifier = Modifier.padding(horizontal = 6.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = DateUtils.getRelativeTimeSpanString(
                        poll.poll.createdAt,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE
                    ).toString()
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = poll.poll.title,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

class PollWidgetPreviewParameterProvider :
    PreviewParameterProvider<PollWithOptionsAndVotesForTargetAudience> {
    override val values: Sequence<PollWithOptionsAndVotesForTargetAudience> = sequenceOf(
        PollWithOptionsAndVotesForTargetAudience(
            poll = Poll(
                title = "Which OS you prefer the most?",
            ),
            user = User(),
            options = emptyList(),
            targetAudience = Poll.TargetAudience()
        )
    )

}