package com.ask.app.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.ask.app.domain.SyncUsersAndWidgetsUseCase
import com.ask.app.ui.screens.utils.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@Preview
@Composable
fun SplashScreen(
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
                        SplashUIState.Init -> "Initializing"
                        SplashUIState.Loading -> "Loading"
                        SplashUIState.Success -> "Success"
                        is SplashUIState.Error -> "Error"
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
        SplashUIState.Success,
        SplashUIState.Error("Error Message")
    )
}

sealed interface SplashUIState {
    data object Init : SplashUIState
    data object Loading : SplashUIState
    data object Success : SplashUIState
    data class Error(val message: String) : SplashUIState
}

@HiltViewModel
class SplashViewModel @Inject constructor(private val syncUsersAndWidgetsUseCase: SyncUsersAndWidgetsUseCase) :
    BaseViewModel() {

    private val _uiStateFlow = MutableStateFlow<SplashUIState>(SplashUIState.Init)
    val uiStateFlow = _uiStateFlow.asStateFlow()

    fun init() {
        safeApiCall({
            _uiStateFlow.value = SplashUIState.Loading
        }, {
            syncUsersAndWidgetsUseCase(true)
            _uiStateFlow.value = SplashUIState.Success
        }, {
            _uiStateFlow.value = SplashUIState.Error(it)
        })
    }
}

