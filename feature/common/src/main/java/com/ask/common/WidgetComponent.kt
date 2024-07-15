package com.ask.common

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ask.core.EMPTY
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience

class WidgetWithOptionAndVotesForTargetAudiencePreviewParameters :
    PreviewParameterProvider<WidgetWithOptionsAndVotesForTargetAudience> {
    override val values: Sequence<WidgetWithOptionsAndVotesForTargetAudience>
        get() = sequenceOf(
            WidgetWithOptionsAndVotesForTargetAudience(
                widget = com.ask.widget.Widget(title = "Who will win the IPL?"),
                options = listOf(
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = com.ask.widget.Widget.Option(text = "Mumbai Indians"),
                        votes = emptyList()
                    ).apply {
                        votesPercent = 30f
                    },
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = com.ask.widget.Widget.Option(text = "Chennai Super Kings"),
                        votes = emptyList()
                    ).apply {
                        didUserVoted = true
                        votesPercent = 70f
                    }
                ),
                targetAudienceGender = com.ask.widget.Widget.TargetAudienceGender(),
                targetAudienceAgeRange = com.ask.widget.Widget.TargetAudienceAgeRange(),
                targetAudienceLocations = listOf(com.ask.widget.Widget.TargetAudienceLocation()),
                user = com.ask.user.User(name = "Shivam")
            ),
            WidgetWithOptionsAndVotesForTargetAudience(
                widget = com.ask.widget.Widget(title = "Who will win the IPL?"),
                options = listOf(
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = com.ask.widget.Widget.Option(imageUrl = "https://picsum.photos/id/237/200/300"),
                        votes = emptyList()
                    ).apply {
                        votesPercent = 45.6F
                    },
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = com.ask.widget.Widget.Option(imageUrl = "https://picsum.photos/id/237/200/300"),
                        votes = emptyList()
                    ).apply {
                        didUserVoted = true
                        votesPercent = 54.4F
                    }
                ),
                targetAudienceGender = com.ask.widget.Widget.TargetAudienceGender(),
                targetAudienceAgeRange = com.ask.widget.Widget.TargetAudienceAgeRange(),
                targetAudienceLocations = listOf(com.ask.widget.Widget.TargetAudienceLocation()),
                user = com.ask.user.User(name = "Shivam")
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
fun WidgetUserView(user: com.ask.user.User, startedAt: Long) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppImage(
            url = user.profilePic,
            contentDescription = user.name,
            contentScale = ContentScale.Crop,
            placeholder = R.drawable.baseline_account_circle_24,
            error = R.drawable.baseline_account_circle_24,
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
                hasVotes = widget.hasVotes,
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
                    hasVotes = widget.hasVotes,
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
    hasVotes: Boolean = false,
    onValueChange: (String) -> Unit = {},
    onClearIconClick: () -> Unit = {},
    onOptionClick: (String) -> Unit = {},
    onDeleteIconClick: (Int) -> Unit = {}
) {
    val (option, _) = widgetOption
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.let {
                    if (didUserVoted) {
                        it.primary
                    } else {
                        it.primaryContainer
                    }
                })
            .padding(all = 5.dp)
            .clickable {
                onOptionClick(option.id)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(shape = CircleShape, color = Color.White)
                .size(36.dp)
        ) {
            Text(
                text = "${index + 1}",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        if (isInput) {
            BasicTextField(
                value = option.text ?: EMPTY,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        } else {
            Text(
                text = option.text ?: EMPTY,
                modifier = Modifier.weight(1f),
                color = if (didUserVoted) {
                    Color.White
                } else {
                    Color.Black
                },
                maxLines = 1
            )
            if (hasVotes)
                Text(
                    text = "${widgetOption.votesPercentFormat}%",
                    color = if (didUserVoted) Color.White else Color.Black
                )
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
    hasVotes: Boolean = false,
    onDeleteIconClick: (Int) -> Unit = {},
    onImageClick: (String) -> Unit,
) {
    var sizeImage by remember { mutableStateOf(IntSize.Zero) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black),
        startY = sizeImage.height.toFloat() / 3.8f,  // 1/3
        endY = sizeImage.height.toFloat()
    )
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
        AppImage(
            url = option.imageUrl ?: EMPTY,
            contentDescription = option.id,
            contentScale = ContentScale.Crop,
            placeholder = R.drawable.baseline_image_24,
            error = R.drawable.baseline_broken_image_24,
            modifier = Modifier
                .clip(roundedCornerShape)
                .fillMaxSize()
                .align(Alignment.Center)
                .onGloballyPositioned { sizeImage = it.size }
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp, start = 6.dp)
                .align(Alignment.TopStart)
                .background(shape = CircleShape, color = Color.White)
                .size(36.dp)
        ) {
            Text(
                text = "${index + 1}",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(gradient, shape = roundedCornerShape)
                .padding(bottom = 8.dp, end = 8.dp)
                .align(Alignment.BottomEnd)
        ) {
            if (isInput) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp, end = 6.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.baseline_delete_24),
                        stringResource(R.string.delete, option.text ?: ""),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onDeleteIconClick(index) },
                        tint = Color.Unspecified
                    )
                }
            }
            if (hasVotes)
                Text(
                    text = "${optionWithVotes.votesPercentFormat}%",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.BottomEnd),
                    color = Color.White
                )
        }
    }
}
