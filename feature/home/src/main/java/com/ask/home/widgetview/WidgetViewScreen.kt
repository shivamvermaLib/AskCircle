package com.ask.home.widgetview

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ask.common.WidgetWithUserView
import com.ask.common.preLoadImages
import com.ask.home.HomeTabScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WidgetViewScreen(
    route: String,
    widgetView: HomeTabScreen.WidgetView,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    lastVotedEmptyOptions: List<String>,
    onOpenImage: (String?) -> Unit,
    onOpenIndexImage: (Int, String?) -> Unit,
) {
    val (index, widgetId) = widgetView
    val viewModel = hiltViewModel<WidgetViewModel>()
    val context = LocalContext.current
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(widgetId, lastVotedEmptyOptions) {
        viewModel.init(widgetId, lastVotedEmptyOptions) {
            context.preLoadImages(it)
        }
    }
    WidgetViewScreen(
        index,
        state,
        sharedTransitionScope,
        animatedContentScope,
        { voteWidgetId, optionId ->
            viewModel.vote(voteWidgetId, optionId, route)
        },
        onOpenImage,
        onOpenIndexImage
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun WidgetViewScreen(
    index: Int,
    widgetUiState: WidgetUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit,
    onOpenImage: (String?) -> Unit,
    onOpenIndexImage: (Int, String?) -> Unit,
) {
    val widget = widgetUiState.widgetWithOptionsAndVotesForTargetAudience
    Box(modifier = Modifier.fillMaxSize()) {
        if (widgetUiState.loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        widgetUiState.error?.let {
            Text(text = it, modifier = Modifier.align(Alignment.Center))
        }
        widget?.let {
            WidgetWithUserView(
                index,
                it,
                sharedTransitionScope,
                animatedContentScope,
                onOptionClick,
                onOpenIndexImage,
                onOpenImage,
                null
            )
        }
    }
}