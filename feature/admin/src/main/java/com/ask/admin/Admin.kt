package com.ask.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.common.AppTextField
import com.ask.common.BaseViewModel
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@Composable
fun AdminScreen() {
    val viewModel = hiltViewModel<AdminViewModel>()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    AdminContent(
        state = state,
        onAskAI = viewModel::askAI,
    )
}

@Composable
fun AdminContent(
    state: AdminUiState,
    onAskAI: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AppTextField(
                hint = "Enter Prompt",
                value = text,
                onValueChange = { text = it },
                minLines = 3,
                maxLines = 5,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.clickable {
                            text = ""
                        })
                }
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = {
                    onAskAI(text)
                }) {
                    Text(text = "Ask AI")
                }
                if (state.loading) {
                    CircularProgressIndicator()
                }
            }
        }
        item {
            Text(text = state.response)
        }
    }
}


@HiltViewModel
class AdminViewModel @Inject constructor(analyticsLogger: AnalyticsLogger) :
    BaseViewModel(analyticsLogger) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )
    private val _aiResponseFlow = MutableStateFlow("")
    private val _loadingFlow = MutableStateFlow(false)
    val uiStateFlow = combine(_aiResponseFlow, _loadingFlow) { response, loading ->
        AdminUiState(loading, response)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AdminUiState())

    fun askAI(prompt: String) {
        viewModelScope.launch {
            _loadingFlow.value = true
            val response = generativeModel.generateContent(prompt)
            _aiResponseFlow.value = response.text ?: ""
            _loadingFlow.value = false
        }
    }
}

data class AdminUiState(
    val loading: Boolean = false,
    val response: String = ""
)