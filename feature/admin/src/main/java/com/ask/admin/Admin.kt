package com.ask.admin

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ask.common.AppImage
import com.ask.common.AppOptionTypeSelect
import com.ask.common.AppTextField
import com.ask.common.DropDownWithSelect
import com.ask.common.WidgetWithUserView
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.workmanager.CreateWidgetWorker


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AdminScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onAdminMoveToCreate: (widget: WidgetWithOptionsAndVotesForTargetAudience) -> Unit,
) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    AdminContent(
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        state = state,
        onAskAI = viewModel::askAI,
        onCreateWidget = {
            CreateWidgetWorker.sendRequest(context, it)
            viewModel.removeWidget(it)
        },
        onRemoveWidget = viewModel::removeWidget,
        fetchCategories = viewModel::fetchCategories,
        fetchCountries = viewModel::fetchCountries,
        onAdminMoveToCreate = {
            onAdminMoveToCreate(it)
            viewModel.removeWidget(it)
        },
        onWidgetSelectForImage = viewModel::selectWidgetForTextToImageOption,
        onFetchImage = viewModel::onFetchImage,
        onUpdateWidget = viewModel::updateWidget,
        onOptionSelected = viewModel::onOptionSelected
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AdminContent(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    state: AdminUiState,
    onAskAI: (text: String, number: Int) -> Unit,
    fetchCategories: () -> Unit,
    fetchCountries: () -> Unit,
    onCreateWidget: (widget: WidgetWithOptionsAndVotesForTargetAudience) -> Unit,
    onRemoveWidget: (widget: WidgetWithOptionsAndVotesForTargetAudience) -> Unit,
    onAdminMoveToCreate: (widget: WidgetWithOptionsAndVotesForTargetAudience) -> Unit,
    onWidgetSelectForImage: (widget: WidgetWithOptionsAndVotesForTargetAudience?) -> Unit,
    onFetchImage: (String) -> Unit,
    onUpdateWidget: (WidgetWithOptionsAndVotesForTargetAudience) -> Unit,
    onOptionSelected: (Widget.Option) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var number by remember { mutableIntStateOf(10) }

    BottomSheetForTextToImage(
        state.selectedWidget,
        state.selectedOption,
        state.webViewSelectedImages,
        onFetchImage,
        onUpdateWidget,
        onOptionSelected,
        onWidgetSelectForImage
    ) {
        onWidgetSelectForImage(null)
    }


    LazyColumn(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AppTextField(
                hint = "Enter Text",
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.padding(all = 16.dp)
            )
        }
        item {
            FlowRow(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)) {
                for (item in state.searchList) {
                    FilterChip(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .align(alignment = Alignment.CenterVertically),
                        onClick = {
                            text = item
                        },
                        label = { Text(item) },
                        selected = true,
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Country")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        state.selectedCountries.fastForEach {
                            FilterChip(selected = true,
                                onClick = { text = it.name },
                                label = { Text(text = "${it.emoji} ${it.name}") })
                        }
                    }
                }
                Button(onClick = fetchCountries) {
                    Text(text = "Refresh")
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Categories")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        state.selectedCategories.fastForEach {
                            FilterChip(selected = true,
                                onClick = { text = it },
                                label = { Text(text = it) })
                        }
                    }
                }
                Button(onClick = fetchCategories) {
                    Text(text = "Refresh")
                }
            }
        }
        item {
            if (state.loading) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CircularProgressIndicator()
                }
            } else {
                Button(
                    onClick = { onAskAI(text, number) }, modifier = Modifier.padding(all = 16.dp)
                ) {
                    Text(text = "Ask AI")
                }
            }
        }
        if (state.error != null) {
            item(key = state.error) {
                Text(text = state.error, modifier = Modifier.padding(all = 16.dp))
            }
        }
        item {
            Spacer(modifier = Modifier.size(10.dp))
        }
        itemsIndexed(state.widgets) { index, widget ->
            WidgetWithUserView(index = index,
                isAdmin = true,
                widgetWithOptionsAndVotesForTargetAudience = widget,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onOpenIndexImage = { _, _ -> },
                onOpenImage = {},
                onAdminCreate = { onCreateWidget(widget) },
                onAdminRemove = { onRemoveWidget(widget) },
                onAdminMoveToCreate = {
                    onAdminMoveToCreate(widget)
                },
                onAdminUpdateTextToImage = {
                    onWidgetSelectForImage(widget)
                })
        }
    }
}

@Composable
fun WebViewComponent(query: String, collectedImageCount: Int, onFetchImage: (String) -> Unit) {
    var webView: WebView? by remember { mutableStateOf(null) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 12.dp)
        ) {
            Text(text = "Get Images = $collectedImageCount")
            TextButton(onClick = {
                webView?.evaluateJavascript(
                    "(function() { " + "const anchorTags = document.querySelectorAll('a[href]'); // Get all anchor tags\n" + "const hrefs = []; // Array to store href values\n" + "\n" + "anchorTags.forEach(tag => {\n" + "  if(tag.href.includes(\"/imgres?q=\")){  \n" + "      hrefs.push(tag.href);\n" + "  }\n" + "});\n" + "\n" + "console.log(hrefs.length);" + "return hrefs;" + " })();",
                    onFetchImage
                )
            }) {
                Text(text = "Fetch")
            }
        }
        AndroidView(
            factory = { context ->
                WebView(context).apply {
//                    clipToOutline = true
//                    setLayerType(View.LAYER_TYPE_NONE, null)
                    settings.javaScriptEnabled = true

                    settings.userAgentString =
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
                    webView = this
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Inject JavaScript to get the HTML source
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            println("console: ${consoleMessage?.message()}")
                            return super.onConsoleMessage(consoleMessage)
                        }
                    }
                    loadUrl("https://www.google.com/search?hl=en&tbm=isch&q=$query")
                }
            }, update = {
                it.loadUrl("https://www.google.com/search?hl=en&tbm=isch&q=$query")
            }, modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .verticalScroll(rememberScrollState())
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetForTextToImage(
    widget: WidgetWithOptionsAndVotesForTargetAudience?,
    selectedOption: Widget.Option?,
    images: List<String>,
    onFetchImage: (String) -> Unit,
    onUpdateWidget: (WidgetWithOptionsAndVotesForTargetAudience) -> Unit,
    onOptionSelected: (Widget.Option) -> Unit,
    onSelectWidget: (WidgetWithOptionsAndVotesForTargetAudience?) -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (widget != null) {
        var selectWebView by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

        val modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            sheetState = sheetState,
            onDismissRequest = onDismissRequest
        ) {
            Row(
                modifier = modifier, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = widget.widget.title,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(10.dp))
                TextButton(onClick = {
                    onUpdateWidget(widget.copy(options = widget.options.map { optionWithVotes ->
                        optionWithVotes.copy(
                            option = optionWithVotes.option.copy(
                                text = null
                            )
                        )
                    }))
                    onDismissRequest()
                }, enabled = (widget.isTextOnly && widget.isImageOnly).not()) {
                    Text(text = "Done")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = modifier
            ) {
                Text(
                    text = "Options", style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                DropDownWithSelect(list = widget.options,
                    title = selectedOption?.text ?: "No Text",
                    onItemSelect = { t ->
                        onOptionSelected(t.option)
                    },
                    itemString = { it.option.text.toString() })

            }

            Row(
                modifier = modifier, verticalAlignment = Alignment.CenterVertically
            ) {
                AppOptionTypeSelect(
                    selected = selectWebView,
                    onSelectedChange = { selectWebView = true },
                    title = "WebView",
                    icon = null
                )
                Spacer(modifier = Modifier.size(5.dp))
                AppOptionTypeSelect(
                    selected = selectWebView.not(),
                    onSelectedChange = { selectWebView = false },
                    title = "Images",
                    icon = null
                )
            }

            Box(modifier = modifier) {
                if (selectWebView) {
                    WebViewComponent(query = selectedOption?.text ?: "", images.size, onFetchImage)
                } else {
                    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                        if (images.isEmpty()) {
                            item {
                                Text(text = "No Images Found")
                            }
                        }
                        items(images) { item ->
                            Box(modifier = Modifier) {
                                AppImage(
                                    modifier = Modifier
                                        .clickable {
                                            onSelectWidget(widget.copy(options = widget.options.map {
                                                if (it.option.id == selectedOption?.id) {
                                                    it.copy(
                                                        option = it.option.copy(imageUrl = item)
                                                    )
                                                } else {
                                                    it
                                                }
                                            }))
                                        },
                                    url = item,
                                    contentDescription = item,
                                    contentScale = ContentScale.Crop,
                                    placeholder = R.drawable.baseline_image_24,
                                    error = R.drawable.baseline_broken_image_24
                                )
                                Text(
                                    text = widget.options.find { it.option.imageUrl == item }?.option?.text
                                        ?: "No Text",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(vertical = 3.dp, horizontal = 5.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = MaterialTheme.shapes.small
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
