package com.ask.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ask.core.EMPTY
import com.ask.user.User
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience

@Composable
fun WidgetWithUserView(
    widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
    onOptionClick: (String, String) -> Unit = { _, _ -> }
) {
    val lastVotedEmptyOptions = listOf(
        stringResource(R.string.your_voice_matters_vote_now),
        stringResource(R.string.shape_the_outcome_cast_your_vote),
        stringResource(R.string.join_the_conversation_vote_today),
        stringResource(R.string.be_a_trendsetter_vote_first),
        stringResource(R.string.get_involved_make_your_vote_count),
        stringResource(R.string.start_the_discussion_with_your_vote),
        stringResource(R.string.let_s_shape_the_future_vote_now),
        stringResource(R.string.vote_for_your_favorite_option)
    )

    ElevatedCard(modifier = Modifier
        .fillMaxWidth()
        .padding(all = 16.dp), onClick = { /*TODO*/ }) {
        Column(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            WidgetUserView(
                user = widgetWithOptionsAndVotesForTargetAudience.user,
                startedAtFormat = widgetWithOptionsAndVotesForTargetAudience.widget.startAtFormat
            )
            Spacer(modifier = Modifier.size(12.dp))
            HorizontalDivider(thickness = 1.dp)
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = widgetWithOptionsAndVotesForTargetAudience.lastVotedAtFormat?.let {
                    stringResource(
                        R.string.last_voted, it
                    )
                }
                    ?: lastVotedEmptyOptions.random(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.W400
            )
            Spacer(modifier = Modifier.size(6.dp))
            WidgetView(widget = widgetWithOptionsAndVotesForTargetAudience, onOptionClick)
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = stringResource(
                    R.string.total_votes,
                    widgetWithOptionsAndVotesForTargetAudience.widgetTotalVotes
                ),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.W400
            )
        }
    }
}

@Composable
fun WidgetUserView(user: User, startedAtFormat: String) {
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
            text = startedAtFormat, style = MaterialTheme.typography.bodySmall
        )
    }
}

//TODO: need to work on image with text
@Composable
fun WidgetView(
    widget: WidgetWithOptionsAndVotesForTargetAudience,
    onOptionClick: (String, String) -> Unit = { _, _ -> }
) {
    Text(text = widget.widget.title, style = MaterialTheme.typography.titleMedium)
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
                .background(shape = CircleShape, color = MaterialTheme.colorScheme.surface)
                .size(36.dp)
        ) {
            Text(
                text = "${index + 1}",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center),
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
                    if (isSystemInDarkTheme()) Color.Black else Color.White
                } else {
                    if (isSystemInDarkTheme()) Color.White else Color.Black
                },
                maxLines = 1
            )
            if (hasVotes)
                Text(
                    text = "${widgetOption.votesPercentFormat}%",
                    color = if (didUserVoted) {
                        if (isSystemInDarkTheme()) Color.Black else Color.White
                    } else {
                        if (isSystemInDarkTheme()) Color.White else Color.Black
                    },
                )
        }
        Spacer(modifier = Modifier.size(5.dp))
        if (isInput) {
            Icon(
                Icons.Rounded.Close,
                stringResource(R.string.clear, option.text ?: EMPTY),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClearIconClick() })
            Icon(
                Icons.Rounded.Delete,
                stringResource(R.string.delete, option.text ?: EMPTY),
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
            contentScale = if (option.imageUrl.isNullOrBlank()) ContentScale.Inside else ContentScale.Crop,
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
                .background(
                    shape = CircleShape,
                    color = if (didUserVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
                .size(36.dp)
        ) {
            Text(
                text = "${index + 1}",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center),
                color = if (didUserVoted) Color.White else MaterialTheme.colorScheme.primary
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
                        stringResource(R.string.delete, option.text ?: EMPTY),
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
