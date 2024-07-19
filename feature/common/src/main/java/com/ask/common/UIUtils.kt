package com.ask.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.ask.core.EMPTY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Composable
fun SnackBarMessageHandler(
    snackBarMessage: String?,
    onDismissSnackBar: () -> Unit,
    snackBarHostState: SnackbarHostState
) {
    if (snackBarMessage == null) return
    LaunchedEffect(snackBarMessage, onDismissSnackBar) {
        snackBarHostState.showSnackbar(snackBarMessage)
        onDismissSnackBar()
    }
}

@Composable
fun <T> DropDownWithSelect(
    list: List<T>,
    title: String,
    modifier: Modifier = Modifier,
    itemString: (T) -> String,
    onItemSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        AppOptionTypeSelect(
            selected = false,
            onSelectedChange = { expanded = true },
            title = title,
            icon = null
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            list.forEach {
                DropdownMenuItem(
                    text = { Text(text = itemString(it)) },
                    onClick = {
                        expanded = false
                        onItemSelect(it)
                    })
            }
        }
    }
}

@Composable
fun <T> DropDownWithSelectAndSearch(
    list: List<T>,
    title: String,
    modifier: Modifier = Modifier,
    itemString: (T) -> String,
    onItemSelect: (T) -> Unit
) {
    var search by remember { mutableStateOf(EMPTY) }
    var expanded by remember { mutableStateOf(false) }
    val filterList = if (search.isBlank()) list else list.filter {
        itemString(it).lowercase().contains(search.lowercase())
    }
    Box(modifier = modifier) {
        AppOptionTypeSelect(
            selected = false,
            onSelectedChange = { expanded = true },
            title = title,
            icon = null
        )
        DropdownMenu(expanded = expanded, onDismissRequest = {
            search = EMPTY
            expanded = false
        }) {
            AppTextField(hint = stringResource(R.string.search),
                value = search,
                onValueChange = {
                    search = it
                })
            filterList.fastForEach {
                DropdownMenuItem(
                    text = { Text(text = itemString(it)) },
                    onClick = {
//                        expanded = false
                        onItemSelect(it)
//                        search = EMPTY
                    })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> SelectItemOnChipWithSearchDropDown(
    title: String,
    list: List<T>,
    selectedItem: List<T>,
    itemString: (T) -> String,
    onSelect: (T) -> Unit,
    onRemove: (T) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.weight(1f))
        DropDownWithSelectAndSearch(
            list,
            stringResource(R.string.select),
            modifier = Modifier.padding(horizontal = 4.dp),
            itemString = itemString,
        ) {
            onSelect(it)
        }
    }
    Spacer(modifier = Modifier.size(6.dp))
    if (selectedItem.isNotEmpty())
        FlowRow(
            Modifier
                .fillMaxWidth(1f)
                .wrapContentHeight(align = Alignment.Top),
            horizontalArrangement = Arrangement.Start,
        ) {
            selectedItem.fastForEach {
                FilterChip(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .align(alignment = Alignment.CenterVertically),
                    onClick = { /* do something*/ },
                    label = { Text(itemString(it)) },
                    selected = true,
                    trailingIcon = {
                        Icon(imageVector = Icons.Rounded.Close,
                            contentDescription = it.toString(),
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                                .clickable { onRemove(it) })
                    },
                )
            }
        }
}

@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String = EMPTY,
    maxLines: Int = 1,
    minLines: Int = 1,
    hasMaxLength: Boolean = false,
) {
    var textFieldWidth by remember { mutableIntStateOf(0) }
    val maxLength = remember(textFieldWidth) { calculateMaxLength(textFieldWidth) }
    TextField(
        value = value,
        onValueChange = {
            if (hasMaxLength) {
                if (it.length < maxLength) {
                    onValueChange(it)
                }
            } else {
                onValueChange(it)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                textFieldWidth = it.size.width
            },
        label = { Text(text = hint) },
        isError = isError,
        maxLines = maxLines,
        minLines = minLines,
        supportingText = {
            Text(if (isError) errorMessage else EMPTY, Modifier.clearAndSetSemantics {})
        },
    )
}


fun calculateMaxLength(width: Int): Int {
    // Assume a monospace font for simplicity, or use the actual font metrics
    // Adjust the character width estimation according to your font size and type
    val characterWidth = 10 // This is an example; adjust according to your font
    return width / characterWidth
}


@Composable
fun AppOptionTypeSelect(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    title: String, icon: ImageVector?
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = {
            onSelectedChange(!selected)
        },
        label = { Text(title) },
        leadingIcon = {
            when (icon != null) {
                true -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }

                false -> Unit
            }
        },
    )
}


@Composable
fun NonLazyGrid(
    columns: Int,
    itemCount: Int,
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    content: @Composable() (Int) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        var rows = (itemCount / columns)
        if (itemCount.mod(columns) > 0) {
            rows += 1
        }

        for (rowId in 0 until rows) {
            val firstIndex = rowId * columns
            Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(spacing)) {
                if (firstIndex + columns > itemCount) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        content(firstIndex)
                    }
                } else {
                    for (columnId in 0 until columns) {
                        val index = firstIndex + columnId

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            if (index < itemCount) {
                                content(index)
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AppImage(
    modifier: Modifier,
    url: String?,
    contentDescription: String,
    contentScale: ContentScale,
    placeholder: Int,
    error: Int
) {
    val isConnected by connectivityState()
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(url)
        .dispatcher(Dispatchers.IO)
        .memoryCacheKey(url)
        .diskCacheKey(url)
        .placeholder(placeholder)
        .error(error)
        .fallback(placeholder)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .crossfade(isConnected)
        .build()
    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}


@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            ),
            label = "shimmer"
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}


