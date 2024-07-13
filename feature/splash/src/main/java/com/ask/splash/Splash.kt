package com.ask.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.ask.common.preLoadImages
import com.ask.workmanager.SyncWidgetWorker
import kotlinx.coroutines.launch


@Composable
fun SplashScreen(route: String, navigateToHome: () -> Unit) {
    val viewModel = hiltViewModel<SplashViewModel>()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        launch {
            viewModel.uiStateFlow.collect {
                if (it is SplashUIState.Success) {
                    SyncWidgetWorker.sendRequest(context, it.time)
                    navigateToHome()
                }
            }
        }
        viewModel.init {
            context.preLoadImages(it)
        }
        viewModel.screenOpenEvent(route)
    }
    SplashScreen(uiState)
}

@Preview
@Composable
private fun SplashScreen(
    @PreviewParameter(SplashScreenProvider::class) uiState: SplashUIState
) {
    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(all = 12.dp)
            ) {
                Text(
                    text = when (uiState) {
                        SplashUIState.Init -> stringResource(R.string.initializing)
                        SplashUIState.Loading -> stringResource(R.string.loading)
                        is SplashUIState.Success -> stringResource(R.string.success)
                        is SplashUIState.Error -> stringResource(R.string.error)
                    },
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.size(10.dp))

                if (uiState is SplashUIState.Error)
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.titleLarge
                    )

                if (uiState == SplashUIState.Loading)
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Start),
                    )
            }
        }
    }
}

class SplashScreenProvider : PreviewParameterProvider<SplashUIState> {
    override val values = sequenceOf(
        SplashUIState.Init,
        SplashUIState.Loading,
        SplashUIState.Success(21323213),
        SplashUIState.Error("Error Message")
    )
}

