package com.ask.home

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ask.common.connectivityState
import com.ask.home.dashboard.DashboardScreen
import com.ask.home.imageview.ImageViewModel
import com.ask.home.imageview.ImageViewScreen
import com.ask.home.profile.ProfileScreen
import com.ask.home.widgetview.WidgetViewScreen
import com.ask.widget.Filter
import com.ask.workmanager.CreateWidgetWorker
import com.ask.workmanager.WorkerStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun HomeScreen(
    route: String, widgetId: String?, sizeClass: WindowSizeClass, navigateToCreate: () -> Unit
) {
    val context = LocalContext.current
    val workerFlow = CreateWidgetWorker.getWorkerFlow(context)
    val homeViewModel = hiltViewModel<HomeViewModel>()
    LaunchedEffect(workerFlow) {
        homeViewModel.setWorkerFlow(workerFlow)
        homeViewModel.screenOpenEvent(route)
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

    val uiState by homeViewModel.uiStateFlow.collectAsStateWithLifecycle()
    HomeScreen(uiState, widgetId, sizeClass, navigateToCreate)
}

@OptIn(
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterial3Api::class
)
@Preview
@Composable
private fun HomeScreen(
    homeUiState: HomeUiState = HomeUiState(),
    widgetId: String? = null,
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    onCreateClick: () -> Unit = {},
) {
    var showMenu by remember { mutableStateOf(false) }
    val isConnected by connectivityState()
    val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val homeNavigationController = rememberNavController()
    val dashboardFirst = HomeTabScreen.Dashboard(Filter.Latest.name, widgetId = widgetId)
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            snackBarHostState.showSnackbar(
                message = "No Internet Connection", duration = SnackbarDuration.Indefinite
            )
        }
    }
    LaunchedEffect(widgetId) {
        widgetId?.let {
            homeNavigationController.navigate(HomeTabScreen.WidgetView(0, it))
        }
    }
    Scaffold(
        topBar = {
            val navBackStackEntry by homeNavigationController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = when {
                                currentRoute?.contains("Dashboard") == true -> R.string.app_name
                                currentRoute?.contains("Profile") == true -> R.string.profile
                                else -> R.string.app_name
                            }
                        )
                    )
                },
                actions = {
                    if (currentRoute?.contains("Dashboard") == true) {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_filter_list_24),
                                contentDescription = stringResource(R.string.filter)
                            )
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            Filter.entries.forEach {
                                DropdownMenuItem(
                                    text = { Text(text = it.name) },
                                    onClick = {
                                        homeNavigationController.navigate(HomeTabScreen.Dashboard(it.name))
                                        showMenu = false
                                    },
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (currentRoute?.contains("WidgetView") == true || currentRoute?.contains("ImageView") == true) {
                        IconButton(onClick = { homeNavigationController.popBackStack() }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
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
            if (homeUiState.createWidgetStatus != WorkerStatus.Loading) ExtendedFloatingActionButton(
                onClick = onCreateClick,
                icon = { Icon(Icons.Filled.Add, stringResource(R.string.create_widget)) },
                text = { Text(text = stringResource(id = R.string.create)) },
            )
        },
        bottomBar = {
            val navBackStackEntry by homeNavigationController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            NavigationBar {
                listOf(
                    R.string.dashboard, R.string.profile
                ).forEach { item ->
                    val stringResource = stringResource(id = item)
                    NavigationBarItem(icon = {
                        Icon(
                            when (item) {
                                R.string.dashboard -> Icons.Filled.Home
                                R.string.profile -> Icons.Rounded.AccountCircle
                                else -> Icons.Rounded.Close
                            }, contentDescription = stringResource
                        )
                    },
                        label = { Text(stringResource(id = item)) },
                        selected = currentRoute?.contains(stringResource) == true,
                        onClick = {
                            when (item) {
                                R.string.dashboard -> homeNavigationController.navigate(
                                    HomeTabScreen.Dashboard(
                                        Filter.Latest.name
                                    )
                                )

                                R.string.profile -> homeNavigationController.navigate(HomeTabScreen.Profile)
                            }
                        })
                }
            }
        }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val scope = rememberCoroutineScope()
            HomeNavigation(
                homeNavigationController, sizeClass, dashboardFirst
            ) { msg, dismissSnackBar ->
                scope.launch {
                    snackBarHostState.showSnackbar(msg)
                    dismissSnackBar()
                }
            }
            if (homeUiState.createWidgetStatus == WorkerStatus.Loading) {
                ElevatedCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(all = 16.dp),
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
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeNavigation(
    homeNavigationController: NavHostController,
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    dashboard: HomeTabScreen.Dashboard,
    onMessage: (String, onDismiss: () -> Unit) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    SharedTransitionLayout {
        NavHost(
            navController = homeNavigationController, startDestination = dashboard
        ) {
            composable<HomeTabScreen.Dashboard> { backStackEntry ->
                val route = backStackEntry.toRoute<HomeTabScreen.Dashboard>()
                DashboardScreen(Json.encodeToString(route),
                    sizeClass,
                    Filter.valueOf(route.filter),
                    this@SharedTransitionLayout,
                    this@composable,
                    onOpenImage = {
                        it?.let {
                            homeNavigationController.navigate(HomeTabScreen.ImageView(it))
                        }
                    },
                    onOpenIndexImage = { index, url ->
                        url?.let {
                            homeNavigationController.navigate(HomeTabScreen.ImageView(it, index))
                        }
                    },
                    onWidgetDetails = { index, dashboardWidgetId ->
                        homeNavigationController.navigate(
                            HomeTabScreen.WidgetView(
                                index,
                                dashboardWidgetId
                            )
                        )
                    },
                    onShareClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, "https://ask-app-36527.web.app/widget/$it")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(context, shareIntent, null)
                    }
                )
            }
            composable<HomeTabScreen.Profile> {
                ProfileScreen(
                    Json.encodeToString(HomeTabScreen.Profile),
                    this@SharedTransitionLayout,
                    this@composable,
                    onMessage,
                ) {
                    homeNavigationController.navigate(HomeTabScreen.ImageView(it))
                }
            }
            composable<HomeTabScreen.ImageView> {
                val imageView = it.toRoute<HomeTabScreen.ImageView>()
                val imageViewModel = hiltViewModel<ImageViewModel>()
                LaunchedEffect(Unit) {
                    imageViewModel.onImageOpen(imageView.imagePath)
                }
                ImageViewScreen(
                    imageView, this@SharedTransitionLayout, this@composable
                )
            }
            composable<HomeTabScreen.WidgetView> { navBackStackEntry ->
                val route = navBackStackEntry.toRoute<HomeTabScreen.WidgetView>()
                WidgetViewScreen(
                    Json.encodeToString(route),
                    widgetView = route,
                    this@SharedTransitionLayout,
                    this@composable,
                    onOpenImage = {
                        it?.let {
                            homeNavigationController.navigate(HomeTabScreen.ImageView(it))
                        }
                    },
                    onOpenIndexImage = { index, url ->
                        url?.let {
                            homeNavigationController.navigate(HomeTabScreen.ImageView(it, index))
                        }
                    })
            }
        }
    }
}


@Serializable
sealed interface HomeTabScreen {
    @Serializable
    data class Dashboard(val filter: String, val widgetId: String? = null) : HomeTabScreen

    @Serializable
    data object Profile : HomeTabScreen

    @Serializable
    data class ImageView(val imagePath: String, val index: Int = -1) : HomeTabScreen

    @Serializable
    data class WidgetView(val index: Int, val widgetId: String)
}
