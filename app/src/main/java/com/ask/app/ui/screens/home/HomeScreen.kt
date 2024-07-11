package com.ask.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ask.app.R
import com.ask.app.ui.screens.home.dashboard.DashBoardScreen
import com.ask.app.ui.screens.home.dashboard.DashboardViewModel
import com.ask.app.ui.screens.home.profile.MyWidgetsViewModel
import com.ask.app.ui.screens.home.profile.ProfileScreen
import com.ask.app.ui.screens.home.profile.ProfileViewModel
import com.ask.app.ui.screens.utils.getByteArray
import com.ask.app.ui.screens.utils.getExtension
import com.ask.app.workmanager.WorkerStatus
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Preview
@Composable
fun HomeScreen(
    homeUiState: HomeUiState = HomeUiState(),
    onCreateClick: () -> Unit = {},
) {
    val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val homeNavigationController = rememberNavController()
    Scaffold(snackbarHost = {
        SnackbarHost(
            hostState = snackBarHostState, modifier = Modifier.padding(horizontal = 16.dp)
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
                NavigationBarItem(
                    icon = {
                        Icon(
                            when (item) {
                                HomeTabScreen.Home -> Icons.Filled.Home
                                HomeTabScreen.Profile -> Icons.Rounded.AccountCircle
                            },
                            contentDescription = item.toString()
                        )
                    },
                    label = { Text(item.toString()) },
                    selected = currentRoute == item.javaClass.canonicalName,
                    onClick = { homeNavigationController.navigate(item) }
                )
            }
        }
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val scope = rememberCoroutineScope()
            HomeNavigation(homeNavigationController) { msg, dismissSnackBar ->
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

@Composable
fun HomeNavigation(
    homeNavigationController: NavHostController,
    onError: (String, onDismiss: () -> Unit) -> Unit = { _, _ -> }
) {
    NavHost(navController = homeNavigationController, startDestination = HomeTabScreen.Home) {
        composable<HomeTabScreen.Home> {
            val viewModel = hiltViewModel<DashboardViewModel>()
            val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                viewModel.screenOpenEvent(it.destination.route)
            }
            DashBoardScreen(state, { widgetId, optionId ->
                viewModel.vote(widgetId, optionId, it.destination.route ?: "Dashboard")
            }, viewModel::setFilterType)
        }
        composable<HomeTabScreen.Profile> {
            val context = LocalContext.current
            val viewModel = hiltViewModel<ProfileViewModel>()
            val myWidgetsViewModel = hiltViewModel<MyWidgetsViewModel>()
            val profileUiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
            val myWidgetsUiState by myWidgetsViewModel.uiStateFlow.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                launch {
                    merge(viewModel.uiStateFlow.mapNotNull { it.error },
                        myWidgetsViewModel.uiStateFlow.mapNotNull { it.error }).collect {
                        onError(it) {
                            viewModel.setError(null)
                            myWidgetsViewModel.setError(null)
                        }
                    }
                }
                viewModel.screenOpenEvent(it.destination.route)
            }
            ProfileScreen(
                profileUiState,
                myWidgetsUiState,
                viewModel::setName,
                viewModel::setEmail,
                viewModel::setGender,
                viewModel::setCountry,
                viewModel::setAge,
                {
                    viewModel.onUpdate({
                        context.getExtension(it)
                    }, {
                        context.getByteArray(it)
                    })
                },
                viewModel::onImageClick,
                onOptionClick = { widgetId, optionId ->
                    myWidgetsViewModel.vote(widgetId, optionId, it.destination.route ?: "Profile")
                },
                onScreenOpen = {
                    viewModel.screenOpenEvent(it)
                }
            )
        }
    }
}


@Serializable
sealed interface HomeTabScreen {
    @Serializable
    data object Home : HomeTabScreen

    @Serializable
    data object Profile : HomeTabScreen
}

