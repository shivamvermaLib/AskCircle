package com.ask.home

import android.Manifest
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
import com.ask.workmanager.CreateWidgetWorker
import com.ask.workmanager.WorkerStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun HomeScreen(
    route: String, sizeClass: WindowSizeClass, navigateToCreate: () -> Unit
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
    HomeScreen(uiState, sizeClass, navigateToCreate)
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalCoroutinesApi::class)
@Preview
@Composable
private fun HomeScreen(
    homeUiState: HomeUiState = HomeUiState(),
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    onCreateClick: () -> Unit = {},
) {
    val isConnected by connectivityState()
    val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val homeNavigationController = rememberNavController()
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            snackBarHostState.showSnackbar(
                message = "No Internet Connection", duration = SnackbarDuration.Indefinite
            )
        }
    }
    Scaffold(snackbarHost = {
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
        ) {
            Snackbar {
                Text(it.visuals.message)
            }
        }
    }, floatingActionButton = {
        if (homeUiState.createWidgetStatus != WorkerStatus.Loading) ExtendedFloatingActionButton(
            onClick = onCreateClick,
            icon = { Icon(Icons.Filled.Add, stringResource(R.string.create_widget)) },
            text = { Text(text = stringResource(id = R.string.create)) },
        )
    }, bottomBar = {
        val navBackStackEntry by homeNavigationController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        NavigationBar {
            listOf(HomeTabScreen.Home, HomeTabScreen.Profile).forEach { item ->
                NavigationBarItem(icon = {
                    Icon(
                        when (item) {
                            HomeTabScreen.Home -> Icons.Filled.Home
                            HomeTabScreen.Profile -> Icons.Rounded.AccountCircle
                            else -> Icons.Rounded.Close
                        }, contentDescription = item.toString()
                    )
                },
                    label = { Text(item.toString()) },
                    selected = currentRoute == item.javaClass.canonicalName,
                    onClick = { homeNavigationController.navigate(item) })
            }
        }
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val scope = rememberCoroutineScope()
            HomeNavigation(homeNavigationController, sizeClass) { msg, dismissSnackBar ->
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
    onError: (String, onDismiss: () -> Unit) -> Unit = { _, _ -> }
) {
    SharedTransitionLayout {
        NavHost(
            navController = homeNavigationController, startDestination = HomeTabScreen.Home
        ) {
            composable<HomeTabScreen.Home> {
                DashboardScreen(
                    Json.encodeToString(HomeTabScreen.Home),
                    sizeClass,
                    this@SharedTransitionLayout,
                    this@composable
                ) {
                    it?.let {
                        homeNavigationController.navigate(HomeTabScreen.ImageView(it))
                    }
                }
            }
            composable<HomeTabScreen.Profile> {
                ProfileScreen(
                    Json.encodeToString(HomeTabScreen.Profile),
                    this@SharedTransitionLayout,
                    this@composable,
                    onError
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
                    imageView,
                    this@SharedTransitionLayout,
                    this@composable
                ) {
                    homeNavigationController.popBackStack()
                }
            }
        }
    }
}


@Serializable
sealed interface HomeTabScreen {
    @Serializable
    data object Home : HomeTabScreen

    @Serializable
    data object Profile : HomeTabScreen

    @Serializable
    data class ImageView(val imagePath: String) : HomeTabScreen
}

