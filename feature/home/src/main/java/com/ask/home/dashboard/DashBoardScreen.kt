package com.ask.home.dashboard

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ask.common.WidgetWithUserView
import com.ask.home.R
import com.ask.user.User
import com.ask.widget.FilterType
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DashboardScreen(
    route: String,
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOpenImage: (String?) -> Unit,
) {
    val viewModel = hiltViewModel<DashboardViewModel>()
    val lastVotedEmptyOptions = listOf(
        stringResource(R.string.your_voice_matters_vote_now),
        stringResource(R.string.shape_the_outcome_cast_your_vote),
        stringResource(R.string.join_the_conversation_vote_today),
        stringResource(R.string.be_a_trendsetter_vote_first),
        stringResource(R.string.get_involved_make_your_vote_count),
        stringResource(R.string.start_the_discussion_with_your_vote),
        stringResource(R.string.let_s_shape_the_future_vote_now),
        stringResource(R.string.vote_for_your_favorite_option)
    )
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.setLastVotedEmptyOptions(lastVotedEmptyOptions)
        viewModel.screenOpenEvent(route)
    }
    DashBoardScreen(
        state,
        sizeClass,
        sharedTransitionScope,
        animatedContentScope,
        { widgetId, optionId ->
            viewModel.vote(widgetId, optionId, route)
        },
        viewModel::setFilterType,
        onOpenImage = onOpenImage
    )
}

@Preview(name = "phone", device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Preview(name = "pixel4", device = "id:pixel_4")
@Preview(name = "tablet", device = "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480")
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
private fun DashBoardScreen(
    @PreviewParameter(DashBoardScreenPreviewParameterProvider::class) uiState: DashboardUiState,
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit = { _, _ -> },
    onFilterTypeChange: (FilterType) -> Unit = {},
    onOpenImage: (String?) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_filter_list_24),
                            contentDescription = stringResource(R.string.filter)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        FilterType.entries.forEach {
                            DropdownMenuItem(
                                text = { Text(text = it.name) },
                                onClick = {
                                    onFilterTypeChange(it)
                                    showMenu = false
                                },
                            )
                        }
                    }
                },
            )
        },
    ) {
        var widthClass = sizeClass.widthSizeClass
        val heightClass = sizeClass.heightSizeClass
        val context = LocalContext.current
        val displayMetrics = context.resources.displayMetrics
        //Width And Height Of Screen
        val width = displayMetrics.widthPixels / displayMetrics.density
        if (width in 840f..850f) {
            widthClass = WindowWidthSizeClass.Medium
        }
        if (widthClass == WindowWidthSizeClass.Compact) {
            DashboardList(
                it,
                uiState,
                sharedTransitionScope,
                animatedContentScope,
                onOptionClick,
                onOpenImage
            )
        } else {
            val columnCount =
                if (widthClass == WindowWidthSizeClass.Expanded && heightClass == WindowHeightSizeClass.Medium) 3
                else if (widthClass == WindowWidthSizeClass.Medium && heightClass == WindowHeightSizeClass.Expanded) 2
                else if (widthClass == WindowWidthSizeClass.Expanded && heightClass == WindowHeightSizeClass.Expanded) 3
                else 2

            DashboardGrid(
                it,
                uiState,
                columnCount,
                sharedTransitionScope,
                animatedContentScope,
                onOptionClick,
                onOpenImage
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DashboardList(
    it: PaddingValues,
    uiState: DashboardUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit,
    onOpenImage: (String?) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(it)
    ) {
        items(uiState.widgets) { widget ->
            WidgetWithUserView(
                widget,
                sharedTransitionScope,
                animatedContentScope,
                onOptionClick,
                onOpenImage
            )
        }
        item {
            Box(modifier = Modifier.size(100.dp))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DashboardGrid(
    it: PaddingValues,
    uiState: DashboardUiState,
    columnCount: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit,
    onOpenImage: (String?) -> Unit,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columnCount), modifier = Modifier
            .fillMaxSize()
            .padding(it)
    ) {
        items(uiState.widgets) { widget ->
            WidgetWithUserView(
                widget,
                sharedTransitionScope,
                animatedContentScope,
                onOptionClick,
                onOpenImage
            )
        }
        item {
            Box(modifier = Modifier.size(100.dp))
        }
    }
}

class DashBoardScreenPreviewParameterProvider : PreviewParameterProvider<DashboardUiState> {
    override val values: Sequence<DashboardUiState>
        get() = sequenceOf(
            DashboardUiState(
                widgets = listOf(
                    WidgetWithOptionsAndVotesForTargetAudience(
                        widget = Widget(
                            title = "Find the answer?"
                        ),
                        options = listOf(
                            WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                                option = Widget.Option(text = "option1"),
                                votes = listOf()
                            ),
                            WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                                option = Widget.Option(text = "Option 2"),
                                votes = listOf()
                            ),
                        ),
                        targetAudienceAgeRange = Widget.TargetAudienceAgeRange(),
                        targetAudienceGender = Widget.TargetAudienceGender(),
                        targetAudienceLocations = listOf(),
                        user = User()
                    )
                )
            )
        )
}