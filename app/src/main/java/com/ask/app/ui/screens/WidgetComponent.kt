package com.ask.app.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ask.app.R
import com.ask.app.data.models.User
import com.ask.app.data.models.Widget
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.app.ui.screens.utils.NonLazyGrid


class WidgetWithOptionAndVotesForTargetAudiencePreviewParameters :
    PreviewParameterProvider<WidgetWithOptionsAndVotesForTargetAudience> {
    override val values: Sequence<WidgetWithOptionsAndVotesForTargetAudience>
        get() = sequenceOf(
            WidgetWithOptionsAndVotesForTargetAudience(
                widget = Widget(title = "Who will win the IPL?"),
                options = listOf(
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = Widget.Option(text = "Mumbai Indians"),
                        votes = emptyList()
                    ).apply {
                        votesPercent = "30"
                    },
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = Widget.Option(text = "Chennai Super Kings"),
                        votes = emptyList()
                    ).apply {
                        didUserVoted = true
                        votesPercent = "70"
                    }
                ),
                targetAudienceGender = Widget.TargetAudienceGender(),
                targetAudienceAgeRange = Widget.TargetAudienceAgeRange(),
                targetAudienceLocations = listOf(Widget.TargetAudienceLocation()),
                user = User(name = "Shivam")
            ),
            WidgetWithOptionsAndVotesForTargetAudience(
                widget = Widget(title = "Who will win the IPL?"),
                options = listOf(
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = Widget.Option(imageUrl = "https://picsum.photos/id/237/200/300"),
                        votes = emptyList()
                    ).apply {
                        votesPercent = "30"
                    },
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = Widget.Option(imageUrl = "https://picsum.photos/id/237/200/300"),
                        votes = emptyList()
                    ).apply {
                        didUserVoted = true
                        votesPercent = "70"
                    }
                ),
                targetAudienceGender = Widget.TargetAudienceGender(),
                targetAudienceAgeRange = Widget.TargetAudienceAgeRange(),
                targetAudienceLocations = listOf(Widget.TargetAudienceLocation()),
                user = User(name = "Shivam")
            )
        )
}

@Preview
@Composable
fun WidgetWithUserView(
    @PreviewParameter(WidgetWithOptionAndVotesForTargetAudiencePreviewParameters::class) widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
    onOptionClick: (String, String) -> Unit = { _, _ -> }
) {
    ElevatedCard(modifier = Modifier
        .fillMaxWidth()
        .padding(all = 16.dp), onClick = { /*TODO*/ }) {
        Column(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            WidgetUserView(
                user = widgetWithOptionsAndVotesForTargetAudience.user,
                startedAt = widgetWithOptionsAndVotesForTargetAudience.widget.startAt
            )
            Spacer(modifier = Modifier.size(10.dp))
            WidgetView(widget = widgetWithOptionsAndVotesForTargetAudience, onOptionClick)
        }
    }
}

@Composable
fun WidgetUserView(user: User, startedAt: Long) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(user.profilePic).crossfade(true)
                .build(),
            contentDescription = user.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.baseline_account_circle_24),
            error = painterResource(id = R.drawable.baseline_account_circle_24),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Text(text = user.name, modifier = Modifier.padding(horizontal = 6.dp))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = DateUtils.getRelativeTimeSpanString(
                startedAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString(), style = MaterialTheme.typography.bodySmall
        )
    }
}

//TODO: need to work on image with text
@Composable
fun WidgetView(
    widget: WidgetWithOptionsAndVotesForTargetAudience,
    onOptionClick: (String, String) -> Unit = { _, _ -> }
) {
    Text(text = widget.widget.title, style = MaterialTheme.typography.labelLarge)
    widget.widget.description?.let {
        Text(
            text = it, style = MaterialTheme.typography.bodySmall
        )
    }
    Spacer(modifier = Modifier.size(10.dp))
    if (widget.isImageOnly) {
        NonLazyGrid(
            modifier = Modifier,
            rowModifier = Modifier,
            spacing = 6.dp,
            columns = 2,
            itemCount = widget.options.size
        ) { index ->
            val widgetOption = widget.options[index]
            ImageOption(
                index = index,
                totalOptions = widget.options.size,
                optionWithVotes = widgetOption,
                didUserVoted = widgetOption.didUserVoted,
            ) {
                onOptionClick(widget.widget.id, it)
            }
        }
    } else if (widget.isTextOnly) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            widget.options.forEachIndexed { index, widgetOption ->
                TextOption(
                    index = index,
                    widgetOption = widgetOption,
                    didUserVoted = widgetOption.didUserVoted,
                    onOptionClick = {
                        onOptionClick(widget.widget.id, it)
                    }
                )
            }
        }
    }
}


@Composable
fun TextOption(
    index: Int,
    widgetOption: WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes,
    didUserVoted: Boolean,
    isInput: Boolean = false,
    onValueChange: (String) -> Unit = {},
    onClearIconClick: () -> Unit = {},
    onOptionClick: (String) -> Unit = {},
    onDeleteIconClick: (Int) -> Unit = {}
) {
    val (option, _) = widgetOption
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.let {
                if (didUserVoted) {
                    it.primary
                } else {
                    it.primaryContainer
                }
            })
            .padding(
                all = 5.dp
            )
            .clickable {
                onOptionClick(option.id)
            }, verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(shape = CircleShape, color = Color.White)
                .size(30.dp)
        ) {
            Text(
                text = "${index + 1}",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        if (isInput) {
            BasicTextField(
                value = option.text ?: "",
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        } else {
            Text(
                text = option.text!!,
                modifier = Modifier.weight(1f),
                color = if (didUserVoted) {
                    Color.White
                } else {
                    Color.Black
                },
                maxLines = 1
            )
            if (widgetOption.votesPercent.isNotBlank())
                Text(text = "${widgetOption.votesPercent}%")
        }
        Spacer(modifier = Modifier.size(5.dp))
        if (isInput) {
            Icon(
                Icons.Rounded.Close,
                stringResource(R.string.clear, option.text ?: ""),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClearIconClick() })
            Icon(
                Icons.Rounded.Delete,
                stringResource(R.string.delete, option.text ?: ""),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDeleteIconClick(index) }
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
    }
}

@Composable
fun ImageOption(
    index: Int,
    totalOptions: Int,
    optionWithVotes: WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes,
    didUserVoted: Boolean,
    isInput: Boolean = false,
    onDeleteIconClick: (Int) -> Unit = {},
    onImageClick: (String) -> Unit,
) {
    val (option, _) = optionWithVotes
    val roundedCornerShape = RoundedCornerShape(
        topStart = when (index) {
            0 -> 10.dp
            else -> 0.dp
        }, topEnd = when (index) {
            1 -> 10.dp
            else -> 0.dp
        }, bottomStart = when (index) {
            0 -> when (totalOptions == 2) {
                true -> 10.dp
                else -> 0.dp
            }

            2 -> 10.dp
            else -> 0.dp
        }, bottomEnd = when (index) {
            1 -> when (totalOptions == 2) {
                true -> 10.dp
                else -> 0.dp
            }

            2 -> when (totalOptions == 3) {
                true -> 10.dp
                else -> 0.dp
            }

            3 -> 10.dp
            else -> 0.dp
        }
    )
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .background(
            color = MaterialTheme.colorScheme.let {
                if (didUserVoted) {
                    it.primary
                } else {
                    it.primaryContainer
                }
            },
            shape = roundedCornerShape,
        )
        .clickable { onImageClick(option.id) }) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(option.imageUrl!!)
                .crossfade(true).build(),
            contentDescription = option.id,
            contentScale = if(isInput) ContentScale.Inside else ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.baseline_image_24),
            error = painterResource(id = R.drawable.baseline_broken_image_24),
            modifier = Modifier
                .clip(roundedCornerShape)
                .fillMaxSize()
                .align(Alignment.Center)
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp, end = 6.dp)
                .align(Alignment.TopEnd)
                .background(shape = CircleShape, color = Color.White)
                .size(25.dp)
        ) {
            Text(
                text = "${index + 1}",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        if (isInput) {
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp, end = 6.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    stringResource(R.string.delete, option.text ?: ""),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onDeleteIconClick(index) }
                )
            }
        }
        if (optionWithVotes.votesPercent.isNotBlank())
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp, end = 6.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Text(
                    text = "${optionWithVotes.votesPercent}%",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
    }
}
