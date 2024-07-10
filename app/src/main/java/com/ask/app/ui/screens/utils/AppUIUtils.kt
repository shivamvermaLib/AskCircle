package com.ask.app.ui.screens.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers

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
            selected = false, { expanded = true }, title, null
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
fun AppTextField(
    modifier: Modifier = Modifier,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String = "",
    maxLines: Int = 1,
    minLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = hint) },
        isError = isError,
        maxLines = maxLines,
        minLines = minLines,
        supportingText = {
            Text(if (isError) errorMessage else "", Modifier.clearAndSetSemantics {})
        },
    )
}


@Composable
fun AppOptionTypeSelect(
    selected: Boolean, onSelectedChange: (Boolean) -> Unit, title: String, icon: ImageVector?
) {
    FilterChip(
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


@Composable
fun AppImage(
    modifier: Modifier,
    url: String?,
    contentDescription: String,
    contentScale: ContentScale,
    placeholder: Int,
    error: Int
) {
    val listener = object : ImageRequest.Listener {
        override fun onError(request: ImageRequest, result: ErrorResult) {
            super.onError(request, result)
        }

        override fun onSuccess(request: ImageRequest, result: SuccessResult) {
            super.onSuccess(request, result)
        }
    }
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(url)
        .listener(listener)
        .dispatcher(Dispatchers.IO)
        .memoryCacheKey(url)
        .diskCacheKey(url)
        .placeholder(placeholder)
        .error(error)
        .fallback(placeholder)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)
        .build()
    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}