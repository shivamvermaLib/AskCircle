package com.ask.home

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ask.common.AppImage
import com.ask.common.WidgetWithUserView
import com.ask.common.connectivityState
import com.ask.core.EMPTY
import com.ask.user.User
import com.ask.widget.Filter
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.workmanager.CreateWidgetWorker
import com.ask.workmanager.WorkerStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    route: String,
    widgetId: String?,
    lastVotedEmptyOptions: List<String>,
    sizeClass: WindowSizeClass,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    navigateToCreate: (widget: WidgetWithOptionsAndVotesForTargetAudience?) -> Unit,
    onWidgetDetails: (index: Int, dashboardWidgetId: String) -> Unit,
    onAdminClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onOpenImage: (imagePath: String?) -> Unit = {},
    onOpenIndexImage: (index: Int, imagePath: String?) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val workerFlow = CreateWidgetWorker.getWorkerFlow(context)
    val homeViewModel = hiltViewModel<HomeViewModel>()
    LaunchedEffect(workerFlow) {
        homeViewModel.setWorkerFlow(workerFlow)
        homeViewModel.screenOpenEvent(route)
        homeViewModel.setLastVotedEmptyOptions(lastVotedEmptyOptions)
    }
    var permissionGranted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
    }
    LaunchedEffect(permissionGranted) {
        if (!permissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    val widgets = homeViewModel.widgetsFlow.collectAsLazyPagingItems()
    val uiState by homeViewModel.uiStateFlow.collectAsStateWithLifecycle()
    HomeScreen(
        uiState, widgets, widgetId, sizeClass,
        sharedTransitionScope, animatedContentScope,
        navigateToCreate, onWidgetDetails,
        onAdminClick,
        onSettingsClick, onOpenImage, onOpenIndexImage,
        homeViewModel::setFilterType,
        homeViewModel::vote,
        {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.widget_share_url, it))
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(context, shareIntent, null)
        },
        homeViewModel::onBookmarkClick,
        homeViewModel::onStopVoteClick,
        homeViewModel::onStartVoteClick,
        homeViewModel::setSearch
    )
}

@OptIn(
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
private fun HomeScreen(
    homeUiState: HomeUiState = HomeUiState(),
    widgets: LazyPagingItems<WidgetWithOptionsAndVotesForTargetAudience>,
    widgetId: String? = null,
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onCreateClick: (widget: WidgetWithOptionsAndVotesForTargetAudience?) -> Unit = {},
    onWidgetDetails: (index: Int, dashboardWidgetId: String) -> Unit = { _, _ -> },
    onAdminClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onOpenImage: (imagePath: String?) -> Unit = {},
    onOpenIndexImage: (index: Int, imagePath: String?) -> Unit = { _, _ -> },
    onFilterChange: (Filter) -> Unit = {},
    onVoteClick: (String, String) -> Unit = { _, _ -> },
    onShareClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onStartVoteClick: (String) -> Unit,
    onStopVoteClick: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    val isConnected by connectivityState()
    val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
    var selectedFilter by remember { mutableStateOf(Filter.Latest) }
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            snackBarHostState.showSnackbar(
                message = "No Internet Connection", duration = SnackbarDuration.Indefinite
            )
        }
    }
    LaunchedEffect(selectedFilter) {
        onFilterChange(selectedFilter)
    }
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 46.dp,
                    bottom = 10.dp
                )
            ) {
                SearchBarUi(
                    homeUiState.search,
                    homeUiState.user,
                    onSettingsClick,
                    onSearch,
                    onAdminClick
                )
                Spacer(modifier = Modifier.size(5.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(Filter.entries) {
                        ElevatedFilterChip(
                            selected = selectedFilter == it,
                            onClick = { selectedFilter = it },
                            label = { Text(text = it.title) })
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Snackbar {
                    Text(it.visuals.message)
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(visible = homeUiState.createWidgetStatus != WorkerStatus.Loading) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onCreateClick(null)
                    },
                    icon = { Icon(Icons.Filled.Add, stringResource(R.string.create_widget)) },
                    text = { Text(text = stringResource(id = R.string.create)) },
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DashBoardScreen(
                widgets,
                sizeClass,
                sharedTransitionScope,
                animatedContentScope,
                onVoteClick,
                onOpenImage = onOpenImage,
                onOpenIndexImage = onOpenIndexImage,
                onWidgetDetails = onWidgetDetails,
                onShareClick = onShareClick,
                onBookmarkClick = onBookmarkClick,
                onStopVoteClick = onStopVoteClick,
                onStartVoteClick = onStartVoteClick
            )
            /*CreatingCard(
                modifier = Modifier.align(Alignment.BottomCenter),
                homeUiState.createWidgetStatus == WorkerStatus.Loading
            )*/
        }
    }
}

@Preview
@Composable
fun CreatingCard(modifier: Modifier = Modifier, visible: Boolean = true) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        ElevatedCard(
            modifier = Modifier.padding(all = 16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.creating))
                Spacer(modifier = Modifier.weight(1f))
                CircularProgressIndicator()
            }
        }
    }
}

/*@Preview(name = "phone", device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Preview(name = "pixel4", device = "id:pixel_4")
@Preview(name = "tablet", device = "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480")*/
@OptIn(
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
private fun DashBoardScreen(
    widgets: LazyPagingItems<WidgetWithOptionsAndVotesForTargetAudience>,
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit = { _, _ -> },
    onOpenImage: (String?) -> Unit,
    onOpenIndexImage: (Int, String?) -> Unit,
    onWidgetDetails: (Int, String) -> Unit,
    onShareClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit = {},
    onStopVoteClick: (String) -> Unit = {},
    onStartVoteClick: (String) -> Unit = {}
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
    if (widgets.itemCount == 0) {
        EmptyDashboardList(
            stringResource(R.string.widget_collection_empty),
            stringResource(R.string.start_creating_widgets)
        )
    } else {
        if (widthClass == WindowWidthSizeClass.Compact) {
            DashboardList(
                widgets,
                sharedTransitionScope,
                animatedContentScope,
                onOptionClick,
                onOpenImage,
                onOpenIndexImage,
                onWidgetDetails,
                onShareClick,
                onBookmarkClick,
                onStopVoteClick,
                onStartVoteClick
            )
        } else {
            val columnCount =
                if (widthClass == WindowWidthSizeClass.Expanded && heightClass == WindowHeightSizeClass.Medium) 3
                else if (widthClass == WindowWidthSizeClass.Medium && heightClass == WindowHeightSizeClass.Expanded) 2
                else if (widthClass == WindowWidthSizeClass.Expanded && heightClass == WindowHeightSizeClass.Expanded) 3
                else 2

            DashboardGrid(
                widgets,
                columnCount,
                sharedTransitionScope,
                animatedContentScope,
                onOptionClick,
                onOpenImage,
                onOpenIndexImage,
                onWidgetDetails,
                onShareClick,
                onBookmarkClick,
                onStopVoteClick,
                onStartVoteClick
            )
        }
    }
}

@Composable
fun SearchBarUi(
    search: String,
    user: User,
    onSettingsClick: () -> Unit,
    onSearch: (String) -> Unit,
    onAdminClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        TextField(
            value = search,
            onValueChange = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(text = stringResource(R.string.search))
            },
            singleLine = true,
            maxLines = 1,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            leadingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_search_24),
                    contentDescription = stringResource(R.string.search),
                    modifier = Modifier.padding(start = 16.dp)
                )
            },
            trailingIcon = {
                Row(
                    modifier = Modifier.padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (search.isNotEmpty()) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_clear_24),
                            contentDescription = stringResource(R.string.clear_search),
                            modifier = Modifier.clickable { onSearch(EMPTY) }
                        )
                    }
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_supervisor_account_24),
                        contentDescription = stringResource(R.string.admin),
                        modifier = Modifier.clickable { onAdminClick() }
                    )
                    AppImage(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(shape = CircleShape)
                            .clickable { onSettingsClick() },
                        url = user.profilePic,
                        contentDescription = user.name,
                        contentScale = ContentScale.Crop,
                        placeholder = R.drawable.baseline_account_circle_24,
                        error = R.drawable.baseline_account_circle_24
                    )
                }
            }
        )
    }
}

@Composable
fun EmptyDashboardList(title: String, subTitle: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.empty_box_svgrepo_com),
            contentDescription = "Empty List",
            modifier = Modifier.size(100.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = subTitle, style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DashboardList(
    widgets: LazyPagingItems<WidgetWithOptionsAndVotesForTargetAudience>,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit,
    onOpenImage: (String?) -> Unit,
    onOpenIndexImage: (Int, String?) -> Unit,
    onWidgetDetails: (Int, String) -> Unit,
    onShareClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit = {},
    onStopVoteClick: (String) -> Unit = {},
    onStartVoteClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyListState()
    ) {
        items(widgets.itemCount) { index ->
            val widget = widgets[index]
            widget?.let {
                WidgetWithUserView(
                    index,
                    false,
                    it,
                    sharedTransitionScope,
                    animatedContentScope,
                    onOptionClick,
                    onOpenIndexImage,
                    onOpenImage,
                    onWidgetDetails,
                    onShareClick,
                    onBookmarkClick,
                    onStopVoteClick,
                    onStartVoteClick
                )
            }
        }
        item {
            Box(modifier = Modifier.size(110.dp))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DashboardGrid(
    widgets: LazyPagingItems<WidgetWithOptionsAndVotesForTargetAudience>,
    columnCount: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onOptionClick: (String, String) -> Unit,
    onOpenImage: (String?) -> Unit,
    onOpenIndexImage: (Int, String?) -> Unit,
    onWidgetDetails: (Int, String) -> Unit,
    onShareClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit = {},
    onStopVoteClick: (String) -> Unit = {},
    onStartVoteClick: (String) -> Unit = {}
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columnCount),
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyStaggeredGridState()
    ) {
        items(widgets.itemCount) { index ->
            val widget = widgets[index]
            widget?.let {
                WidgetWithUserView(
                    index,
                    false,
                    it,
                    sharedTransitionScope,
                    animatedContentScope,
                    onOptionClick,
                    onOpenIndexImage,
                    onOpenImage,
                    onWidgetDetails,
                    onShareClick,
                    onBookmarkClick,
                    onStopVoteClick,
                    onStartVoteClick
                )
            }
        }

        item {
            Box(modifier = Modifier.size(100.dp))
        }
    }
}


@Serializable
sealed interface HomeTabScreen {
    @Serializable
    data class Dashboard(val filter: String, val widgetId: String? = null) : HomeTabScreen
}
