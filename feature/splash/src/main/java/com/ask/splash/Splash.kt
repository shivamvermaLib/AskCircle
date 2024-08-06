package com.ask.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ask.common.connectivityState
import com.ask.common.preLoadImages
import com.ask.workmanager.SyncWidgetWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun SplashScreen(route: String, navigateToHome: () -> Unit) {
    val viewModel = hiltViewModel<SplashViewModel>()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isConnected by connectivityState()
    LaunchedEffect(Unit) {
        launch {
            viewModel.uiStateFlow.collect {
                if (it is SplashUIState.Success) {
                    SyncWidgetWorker.sendRequest(context, it.time)
                    delay(2.seconds)
                    navigateToHome()
                }
            }
        }

        viewModel.init(isConnected, context::preLoadImages)
        viewModel.screenOpenEvent(route)
    }
    SplashScreen(uiState)
}

@Preview
@Composable
private fun SplashScreen(
    @PreviewParameter(SplashScreenProvider::class) uiState: SplashUIState
) {
    val animation = remember { Animatable(initialValue = 0f) }
    LaunchedEffect(uiState) {
        if (uiState is SplashUIState.Loading) {
            animation.animateTo(
                targetValue = uiState.progress,
                animationSpec = tween(
                    durationMillis = if (uiState.progress == 1f) 100 else 2000,
                    easing = LinearEasing
                ),
            )
        } else if (uiState is SplashUIState.Success) {
            animation.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 2000,
                    easing = LinearEasing
                ),
            )
        }
    }
    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.CenterStart
        ) {
            WavesLoadingIndicator(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                progress = animation.value
            )
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(all = 12.dp)
            ) {
                Text(
                    text = when (uiState) {
                        SplashUIState.Init -> stringResource(R.string.initializing)
                        is SplashUIState.Loading -> stringResource(R.string.loading)
                        is SplashUIState.Success -> stringResource(R.string.success)
                        is SplashUIState.Error -> stringResource(R.string.error)
                    },
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.size(10.dp))

                if (uiState is SplashUIState.Error)
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.titleMedium
                    )

                /*                if (uiState is SplashUIState.Loading)
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .align(Alignment.Start),
                                    )*/
            }
        }
    }
}


class SplashScreenProvider : PreviewParameterProvider<SplashUIState> {
    override val values = sequenceOf(
        SplashUIState.Init,
        SplashUIState.Loading(0.5f),
        SplashUIState.Success(21323213),
        SplashUIState.Error("Error Message")
    )
}

