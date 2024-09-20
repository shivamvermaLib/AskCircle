package com.ask.widgetdetails

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ask.common.AppImage
import com.ask.common.WidgetWithUserView
import com.ask.common.preLoadImages
import com.ask.core.checkIfFirebaseUrl
import com.ask.widget.WidgetDetailsWithResult.WidgetResult

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WidgetDetailScreen(
    route: String,
    widgetViewIndex: Int,
    widgetViewWidgetId: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    lastVotedEmptyOptions: List<String>,
    onOpenImage: (String?) -> Unit,
    onOpenIndexImage: (Int, String?) -> Unit,
    onBack: () -> Unit
) {
    val viewModel = hiltViewModel<WidgetDetailViewModel>()
    val context = LocalContext.current
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(route) {
        viewModel.screenOpenEvent(route)
    }
    LaunchedEffect(widgetViewWidgetId, lastVotedEmptyOptions) {
        viewModel.init(
            widgetViewWidgetId,
            context.getString(R.string.unspecified),
            context.getString(R.string.male),
            context.getString(R.string.female),
            lastVotedEmptyOptions,
            context::preLoadImages,
        )
    }
    WidgetDetailScreen(
        widgetViewIndex,
        state,
        sharedTransitionScope,
        animatedContentScope,
        viewModel::vote,
        onOpenImage,
        onOpenIndexImage,
        onBack
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun WidgetDetailScreen(
    index: Int,
    widgetUiState: WidgetUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit,
    onOpenImage: (String?) -> Unit,
    onOpenIndexImage: (Int, String?) -> Unit,
    onBack: () -> Unit = {}
) {
    val widget = widgetUiState.widgetDetailsWithResult.widgetWithOptionsAndVotesForTargetAudience
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Widget Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (widgetUiState.loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            widgetUiState.error?.let {
                Text(text = it, modifier = Modifier.align(Alignment.Center))
            }
            widget?.let {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WidgetWithUserView(
                        index,
                        false,
                        it.toWidgetWithOptionsAndVoteCountAndCommentCount(),
                        sharedTransitionScope,
                        animatedContentScope,
                        onOptionClick,
                        onOpenIndexImage,
                        onOpenImage,
                        null
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    PieChartGroup(
                        showAds = false,
                        title = stringResource(R.string.result),
                        resultData = widgetUiState.widgetDetailsWithResult.widgetResults
                    )
                    widgetUiState.widgetDetailsWithResult.widgetOptionResults.forEach {
                        PieChartGroup(
                            showAds = true,
                            title = it.key,
                            resultData = it.value
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PieChartGroup(showAds: Boolean, title: String, resultData: Map<String, List<WidgetResult>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
//        if (showAds)
//            AppAdmobBanner(modifier = Modifier.fillMaxWidth())
        var visible by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (title.checkIfFirebaseUrl()) {
                AppImage(
                    modifier = Modifier.size(45.dp),
                    url = title,
                    contentDescription = "Image Option",
                    contentScale = ContentScale.Crop,
                    placeholder = R.drawable.baseline_image_24,
                    error = R.drawable.baseline_broken_image_24
                )
            } else {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { visible = !visible }) {
                Text(text = stringResource(if (visible) R.string.hide_all else R.string.view_all))
            }
        }
        AnimatedVisibility(visible = visible) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.size(10.dp))
                resultData.forEach {
                    PieChart(
                        title = it.key, data = it.value
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PieChart(
    title: String,
    data: List<WidgetResult>,
    radiusOuter: Dp = 80.dp,
    chartBarWidth: Dp = 60.dp,
    animDuration: Int = 1000,
) {
    if (data.isEmpty()) {
        Spacer(modifier = Modifier)
    } else {
        val totalSum = data.sumOf { it.count }

        val resultData =
            data.map { it.copy(floatValue = 360 * it.count.toFloat() / totalSum.toFloat()) }

        var animationPlayed by remember { mutableStateOf(false) }

        var lastValue = 0f
        val radiusOuterPx = with(LocalDensity.current) {
            radiusOuter.toPx()
        }
        // it is the diameter value of the Pie
        val animateSize by animateFloatAsState(
            targetValue = if (animationPlayed) radiusOuterPx else 0f, animationSpec = tween(
                durationMillis = animDuration, delayMillis = 0, easing = LinearOutSlowInEasing
            ), label = "size"
        )

        // if you want to stabilize the Pie Chart you can use value -90f
        // 90f is used to complete 1/4 of the rotation
        val animateRotation by animateFloatAsState(
            targetValue = if (animationPlayed) 90f * 11f else 0f, animationSpec = tween(
                durationMillis = animDuration, delayMillis = 0, easing = LinearOutSlowInEasing
            ), label = "rotation"
        )

        // to play the animation only once when the function is Created or Recomposed
        LaunchedEffect(key1 = true) {
            animationPlayed = true
        }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(horizontal = 6.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Box(
                    modifier = Modifier.size(animateSize.dp - 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(radiusOuter)
                            .rotate(animateRotation)
                    ) {
                        // draw each Arc for each data entry in Pie Chart
                        resultData.forEach { chartData ->
                            drawArc(
                                color = Color(chartData.color),
                                lastValue,
                                chartData.floatValue,
                                useCenter = false,
                                style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Butt)
                            )
                            lastValue += chartData.floatValue
                        }
                    }
                }

                FlowRow(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.forEach {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(color = Color(it.color))
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "(${it.percent}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

}