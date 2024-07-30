package com.ask.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.category.GetCategoryUseCase
import com.ask.common.AppTextField
import com.ask.common.BaseViewModel
import com.ask.common.DropDownWithSelect
import com.ask.widget.GetWidgetsFromAiUseCase
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.workmanager.CreateWidgetWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random


@Composable
fun AdminScreen() {
    val viewModel = hiltViewModel<AdminViewModel>()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    AdminContent(
        state = state,
        onAskAI = viewModel::askAI,
        onCreateWidget = {
            CreateWidgetWorker.sendRequest(context, it)
            viewModel.removeWidget(it)
        },
        onRemoveWidget = viewModel::removeWidget,
        fetchCategories = viewModel::fetchCategories
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminContent(
    state: AdminUiState,
    onAskAI: (text: String, number: Int) -> Unit,
    fetchCategories: () -> Unit,
    onCreateWidget: (widget: WidgetWithOptionsAndVotesForTargetAudience) -> Unit,
    onRemoveWidget: (widget: WidgetWithOptionsAndVotesForTargetAudience) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var number by remember { mutableIntStateOf(10) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AppTextField(
                hint = "Enter Text",
                value = text,
                onValueChange = { text = it },
            )
        }
        item {
            Row {
                Text(
                    text = "Enter Number of Widgets", style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                DropDownWithSelect(list = (10..50).map { it },
                    title = number.toString(),
                    onItemSelect = { number = it },
                    itemString = { it.toString() })
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                FlowRow(
                    modifier = Modifier
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.selectedCategories.fastForEach {
                        FilterChip(
                            selected = true,
                            onClick = { /*TODO*/ },
                            label = { Text(text = it) })
                    }
                }
                Button(onClick = fetchCategories) {
                    Text(text = "Refresh")
                }
            }
        }

        item {
            if (state.loading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { onAskAI(text, number) }) {
                    Text(text = "Ask AI")
                }
            }
        }
        if (state.error != null) {
            item(key = state.error) {
                Text(text = state.error)
            }
        }
        items(state.widgets) { widget ->
            Row(
                modifier = Modifier.padding(all = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = Modifier
                        .padding(all = 6.dp)
                        .weight(2f)
                ) {
                    Text(text = widget.widget.title, style = MaterialTheme.typography.titleMedium)
                    Text(text = widget.widget.widgetType.name)
                    Spacer(modifier = Modifier.size(4.dp))
                    for (option in widget.options) {
                        Text(
                            text = option.option.text ?: "",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onCreateWidget(widget) })
                    {
                        Text(text = "Create")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onRemoveWidget(widget) })
                    {
                        Text(text = "Remove")
                    }
                }

            }
        }
    }
}


@HiltViewModel
class AdminViewModel @Inject constructor(
    analyticsLogger: AnalyticsLogger,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val getWidgetWithAiUseCase: GetWidgetsFromAiUseCase
) : BaseViewModel(analyticsLogger) {

    private val _uiStateFlow = MutableStateFlow(AdminUiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            val categories = if (uiStateFlow.value.categories.isNotEmpty()) {
                uiStateFlow.value.categories
            } else {
                getCategoryUseCase().first()
                    .map { listOf(it.category.name) + it.subCategories.map { it.title } }
                    .flatten()
            }
            _uiStateFlow.value =
                _uiStateFlow.value.copy(
                    categories = categories,
                    selectedCategories = categories.shuffled().take(Random.nextInt(3, 10))
                )
        }
    }

    fun askAI(text: String, number: Int) {
        safeApiCall({
            _uiStateFlow.value =
                _uiStateFlow.value.copy(loading = true, widgets = emptyList(), error = null)
        }, {
            val widgets =
                getWidgetWithAiUseCase(
                    number,
                    uiStateFlow.value.selectedCategories,
                    myWords = text.takeIf { it.isNotEmpty() })
            _uiStateFlow.value =
                _uiStateFlow.value.copy(loading = false, widgets = widgets, error = null)
        }, {
            _uiStateFlow.value = _uiStateFlow.value.copy(loading = false, error = it)
        })
    }


    fun removeWidget(widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience) {
        viewModelScope.launch {
            _uiStateFlow.value = _uiStateFlow.value.copy(
                widgets = _uiStateFlow.value.widgets.filter {
                    it != widgetWithOptionsAndVotesForTargetAudience
                }
            )
        }
    }
}


data class AdminUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val categories: List<String> = emptyList(),
    val selectedCategories: List<String> = emptyList(),
    val widgets: List<WidgetWithOptionsAndVotesForTargetAudience> = emptyList()
)