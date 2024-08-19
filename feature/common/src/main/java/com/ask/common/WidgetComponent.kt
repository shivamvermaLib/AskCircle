package com.ask.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ask.core.EMPTY
import com.ask.core.ImageSizeType
import com.ask.core.getImage
import com.ask.user.User
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WidgetWithUserView(
    index: Int,
    isAdmin: Boolean = false,
    widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit = { _, _ -> },
    onOpenIndexImage: (Int, String?) -> Unit,
    onOpenImage: (String?) -> Unit,
    onWidgetDetails: ((Int, String) -> Unit)? = null,
    onShareClick: (String) -> Unit = {},
    onBookmarkClick: (String) -> Unit = {},
    onStopVoteClick: (String) -> Unit = {},
    onStartVoteClick: (String) -> Unit = {},
    onAdminCreate: () -> Unit = {},
    onAdminMoveToCreate: () -> Unit = {},
    onAdminRemove: () -> Unit = {},
    onAdminUpdateTextToImage: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        with(sharedTransitionScope) {
            if (onWidgetDetails != null) {
                ElevatedCard(modifier = Modifier
                    .sharedElement(
                        sharedTransitionScope.rememberSharedContentState(
                            key = "$index-card-${widgetWithOptionsAndVotesForTargetAudience.widget.id}"
                        ), animatedVisibilityScope = animatedContentScope
                    )
                    .fillMaxWidth()
                    .padding(all = 16.dp),
                    onClick = {
                        onWidgetDetails.invoke(
                            index, widgetWithOptionsAndVotesForTargetAudience.widget.id
                        )
                    }) {
                    CardItem(
                        index,
                        widgetWithOptionsAndVotesForTargetAudience,
                        sharedTransitionScope,
                        animatedContentScope,
                        onOptionClick,
                        onOpenIndexImage,
                        onOpenImage,
                        onShareClick,
                        onBookmarkClick,
                        onStopVoteClick,
                        onStartVoteClick
                    )
                }
            } else {
                ElevatedCard(
                    modifier = Modifier
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(
                                key = "$index-card-${widgetWithOptionsAndVotesForTargetAudience.widget.id}"
                            ), animatedVisibilityScope = animatedContentScope
                        )
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                ) {
                    CardItem(
                        index,
                        widgetWithOptionsAndVotesForTargetAudience,
                        sharedTransitionScope,
                        animatedContentScope,
                        onOptionClick,
                        onOpenIndexImage,
                        onOpenImage,
                        onShareClick,
                        onBookmarkClick,
                        onStopVoteClick,
                        onStartVoteClick
                    )
                }
            }
        }
        if (widgetWithOptionsAndVotesForTargetAudience.showAdMob)
            AppAdmobBanner(modifier = Modifier.fillMaxWidth())

        if (isAdmin)
            AdminButton(
                onAdminCreate = onAdminCreate,
                onAdminMoveToCreate = onAdminMoveToCreate,
                onAdminRemove = onAdminRemove,
                onAdminUpdateTextToImage = onAdminUpdateTextToImage
            )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminButton(
    onAdminCreate: () -> Unit = {},
    onAdminMoveToCreate: () -> Unit = {},
    onAdminRemove: () -> Unit = {},
    onAdminUpdateTextToImage: () -> Unit = {}
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(all = 16.dp)
    ) {
        ElevatedButton(onClick = onAdminCreate) {
            Text(text = "Create")
        }
        ElevatedButton(onClick = onAdminMoveToCreate) {
            Text(text = "Move To Create Screen")
        }
        ElevatedButton(onClick = onAdminRemove) {
            Text(text = "Remove")
        }
        ElevatedButton(onClick = onAdminUpdateTextToImage) {
            Text(text = "Update Text To Image")
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CardItem(
    index: Int,
    widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit = { _, _ -> },
    onOpenIndexImage: (Int, String?) -> Unit,
    onOpenImage: (String?) -> Unit,
    onShareClick: (String) -> Unit = {},
    onBookmarkClick: (String) -> Unit = {},
    onStopVoteClick: (String) -> Unit = {},
    onStartVoteClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(all = 16.dp)
    ) {
        WidgetUserView(
            index = index,
            user = widgetWithOptionsAndVotesForTargetAudience.user,
            startedAtFormat = widgetWithOptionsAndVotesForTargetAudience.widget.startAtFormat,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            onOpenIndexImage
        )
        Spacer(modifier = Modifier.size(12.dp))
        HorizontalDivider(thickness = 1.dp)
        Spacer(modifier = Modifier.size(6.dp))
        Text(text = widgetWithOptionsAndVotesForTargetAudience.lastVotedAtFormat?.let {
            stringResource(R.string.last_voted, it)
        } ?: widgetWithOptionsAndVotesForTargetAudience.lastVotedAtOptional,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.W400)
        Spacer(modifier = Modifier.size(12.dp))
        WidgetView(
            widget = widgetWithOptionsAndVotesForTargetAudience,
            sharedTransitionScope,
            animatedContentScope,
            { widgetId, optionId ->
                if (widgetWithOptionsAndVotesForTargetAudience.isAllowedVoting) onOptionClick(
                    widgetId,
                    optionId
                )
            },
            onOpenImage
        )
        Spacer(modifier = Modifier.size(18.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
        ) {
            IconButton(
                title = stringResource(R.string.start),
                show = widgetWithOptionsAndVotesForTargetAudience.isCreatorOfTheWidget && widgetWithOptionsAndVotesForTargetAudience.isWidgetNotStarted,
                height = 30.dp,
                width = 52.dp,
                onClick = {
                    onStartVoteClick(
                        widgetWithOptionsAndVotesForTargetAudience.widget.id
                    )
                })

            IconButton(
                title = stringResource(R.string.end),
                show = widgetWithOptionsAndVotesForTargetAudience.isCreatorOfTheWidget && widgetWithOptionsAndVotesForTargetAudience.widget.endAt == null && widgetWithOptionsAndVotesForTargetAudience.isAllowedVoting,
                height = 29.dp,
                width = 44.dp,
                onClick = {
                    onStopVoteClick(
                        widgetWithOptionsAndVotesForTargetAudience.widget.id
                    )
                })

            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.share),
                contentDescription = "Share",
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        onShareClick(
                            widgetWithOptionsAndVotesForTargetAudience.widget.id
                        )
                    })
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = ImageVector.vectorResource(id = if (widgetWithOptionsAndVotesForTargetAudience.isBookmarked) R.drawable.baseline_bookmark_24 else R.drawable.round_bookmark_border_24),
                contentDescription = stringResource(R.string.bookmark),
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        onBookmarkClick(widgetWithOptionsAndVotesForTargetAudience.widget.id)
                    })
        }
        Spacer(modifier = Modifier.size(12.dp))
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

@Composable
fun IconButton(title: String, show: Boolean, height: Dp, width: Dp, onClick: () -> Unit) {
    AnimatedVisibility(visible = show) {
        OutlinedButton(
            onClick = onClick,
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.defaultMinSize(
                minHeight = height, minWidth = width
            ),
            colors = ButtonDefaults.textButtonColors(
                containerColor = if (isSystemInDarkTheme()) Color.White else Color.Black
            ),
            border = BorderStroke(2.dp, if (isSystemInDarkTheme()) Color.White else Color.Black)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = if (isSystemInDarkTheme()) Color.Black else Color.White
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WidgetUserView(
    index: Int,
    user: User,
    startedAtFormat: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOpenImage: (Int, String?) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (user.profilePic.isNullOrBlank()) {
            AppImage(url = user.profilePic.getImage(ImageSizeType.SIZE_ORIGINAL),
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                placeholder = R.drawable.baseline_account_circle_24,
                error = R.drawable.baseline_account_circle_24,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable {
                        onOpenImage(
                            index, user.profilePic.getImage(ImageSizeType.SIZE_ORIGINAL)
                        )
                    })
        } else {
            with(sharedTransitionScope) {
                AppImage(url = user.profilePic.getImage(ImageSizeType.SIZE_ORIGINAL),
                    contentDescription = user.name,
                    contentScale = ContentScale.Crop,
                    placeholder = R.drawable.baseline_account_circle_24,
                    error = R.drawable.baseline_account_circle_24,
                    modifier = Modifier.Companion
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(
                                key = "$index${user.profilePic.getImage(ImageSizeType.SIZE_ORIGINAL) ?: EMPTY}"
                            ), animatedVisibilityScope = animatedContentScope
                        )
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable {
                            onOpenImage(
                                index, user.profilePic.getImage(ImageSizeType.SIZE_ORIGINAL)
                            )
                        })
            }
        }
        Column(
            horizontalAlignment = Alignment.Start, modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Text(text = user.name)
            Spacer(modifier = Modifier.size(3.dp))
            Text(text = startedAtFormat, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

//TODO: need to work on image with text
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WidgetView(
    widget: WidgetWithOptionsAndVotesForTargetAudience,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit = { _, _ -> },
    onOpenImage: (String?) -> Unit,
) {
    Text(text = widget.widget.title, style = MaterialTheme.typography.titleMedium)
    widget.widget.description?.let {
        Text(text = it, style = MaterialTheme.typography.bodySmall)
    }
    Spacer(modifier = Modifier.size(8.dp))
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
                onOptionClick = {
                    onOptionClick(widget.widget.id, it)
                },
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onOpenImage = onOpenImage
            )
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
                    },
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
    hasError: Boolean = false,
    onValueChange: (String) -> Unit = {},
    onClearIconClick: () -> Unit = {},
    onOptionClick: (String) -> Unit = {},
    onDeleteIconClick: (Int) -> Unit = {}
) {
    val (option, _) = widgetOption
    TextButton(modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(all = 5.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.let {
                if (didUserVoted) {
                    it.primary
                } else {
                    it.primaryContainer
                }
            }),
        onClick = {
            onOptionClick(option.id)
        }) {
        Box(
            modifier = Modifier
                .background(
                    shape = CircleShape, color = MaterialTheme.colorScheme.surface
                )
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
                textStyle = TextStyle(
                    color = if (hasError) Color.Red else if (isSystemInDarkTheme()) Color.White else Color.Black,
                ),
                cursorBrush = SolidColor(
                    if (isSystemInDarkTheme()) Color.White else Color.Black
                ),
            )
        } else {
            Text(
                text = option.text ?: EMPTY,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(),
                color = if (didUserVoted) {
                    if (isSystemInDarkTheme()) Color.Black else Color.White
                } else {
                    if (isSystemInDarkTheme()) Color.White else Color.Black
                },
                maxLines = 1,
                fontSize = 16.sp
            )
            if (hasVotes) Text(
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
            Icon(Icons.Rounded.Close,
                stringResource(R.string.clear, option.text ?: EMPTY),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClearIconClick() })
            Spacer(modifier = Modifier.size(6.dp))
            Icon(Icons.Rounded.Delete,
                stringResource(R.string.delete, option.text ?: EMPTY),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDeleteIconClick(index) })
        }
        Spacer(modifier = Modifier.size(5.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ImageOption(
    modifier: Modifier = Modifier,
    index: Int,
    totalOptions: Int,
    optionWithVotes: WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes,
    didUserVoted: Boolean,
    isInput: Boolean = false,
    hasVotes: Boolean = false,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
    onDeleteIconClick: (Int) -> Unit = {},
    onOptionClick: (String) -> Unit,
    onOpenImage: (String?) -> Unit,
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
    Box(
        modifier = modifier
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
            .combinedClickable(onClick = { onOptionClick(option.id) },
                onLongClick = { onOpenImage(option.imageUrl.getImage(ImageSizeType.SIZE_ORIGINAL)) }),
    ) {
        if (sharedTransitionScope != null && animatedContentScope != null) {
            with(sharedTransitionScope) {
                AppImage(url = option.imageUrl.getImage(ImageSizeType.SIZE_ORIGINAL) ?: EMPTY,
                    contentDescription = option.id,
                    contentScale = if (option.imageUrl.isNullOrBlank()) ContentScale.Inside else ContentScale.Crop,
                    placeholder = R.drawable.baseline_image_24,
                    error = R.drawable.baseline_broken_image_24,
                    modifier = Modifier.Companion
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(
                                key = option.imageUrl.getImage(ImageSizeType.SIZE_ORIGINAL) ?: EMPTY
                            ), animatedVisibilityScope = animatedContentScope
                        )
                        .clip(roundedCornerShape)
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .onGloballyPositioned { sizeImage = it.size })
            }
        } else {
            AppImage(url = option.imageUrl.getImage(ImageSizeType.SIZE_ORIGINAL) ?: EMPTY,
                contentDescription = option.id,
                contentScale = if (option.imageUrl.isNullOrBlank()) ContentScale.Inside else ContentScale.Crop,
                placeholder = R.drawable.baseline_image_24,
                error = R.drawable.baseline_broken_image_24,
                modifier = Modifier
                    .clip(roundedCornerShape)
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .onGloballyPositioned { sizeImage = it.size })
        }
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
                        .padding(all = 6.dp)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.baseline_delete_24),
                        stringResource(R.string.delete, option.text ?: EMPTY),
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.Center)
                            .clickable { onDeleteIconClick(index) },
                        tint = Color.Unspecified
                    )
                }
            }
            if (hasVotes) {
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
}
