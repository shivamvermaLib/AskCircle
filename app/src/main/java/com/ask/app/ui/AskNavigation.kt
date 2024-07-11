package com.ask.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ask.app.CREATE_WIDGET
import com.ask.app.WIDGET
import com.ask.app.ui.screens.SplashScreen
import com.ask.app.ui.screens.SplashUIState
import com.ask.app.ui.screens.SplashViewModel
import com.ask.app.ui.screens.TestAPIScreen
import com.ask.app.ui.screens.create.CreateWidgetScreen
import com.ask.app.ui.screens.create.CreateWidgetViewModel
import com.ask.app.ui.screens.home.HomeScreen
import com.ask.app.ui.screens.home.HomeViewModel
import com.ask.app.workmanager.CreateWidgetWorker
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun AskNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val workerFlow = workManager.getWorkInfosByTagFlow(CREATE_WIDGET)

    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> {
            val viewModel = hiltViewModel<SplashViewModel>()
            val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                launch {
                    viewModel.uiStateFlow.collect {
                        if (it is SplashUIState.Success) {
                            navController.navigate(Home)
                        }
                    }
                }
                viewModel.init()
                viewModel.screenOpenEvent(it.destination.route)
            }
            SplashScreen(uiState)
        }
        composable<TestAPI> { TestAPIScreen() }
        composable<Home> {
            val homeViewModel = hiltViewModel<HomeViewModel>()
            LaunchedEffect(workerFlow) {
                homeViewModel.setWorkerFlow(workerFlow)
                homeViewModel.screenOpenEvent(it.destination.route)
            }
            val uiState by homeViewModel.uiStateFlow.collectAsStateWithLifecycle()
            HomeScreen(uiState) {
                navController.navigate(Create)
            }
        }
        composable<Create> {
            val viewModel = hiltViewModel<CreateWidgetViewModel>()
            val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                viewModel.screenOpenEvent(it.destination.route)
            }
            CreateWidgetScreen(
                uiState,
                viewModel::setTitle,
                viewModel::setDesc,
                viewModel::setOptionType,
                viewModel::updateOption,
                viewModel::addOption,
                viewModel::setGender,
                viewModel::setMinAge,
                viewModel::setMaxAge,
                viewModel::addLocation,
                viewModel::removeLocation,
                {
                    val workData = Data.Builder().apply {
                        putString(
                            WIDGET,
                            Json.encodeToString(uiState.toWidgetWithOptionsAndVotesForTargetAudience())
                        )
                    }
                    val workRequest = OneTimeWorkRequestBuilder<CreateWidgetWorker>()
                        .setInputData(workData.build())
                        .addTag(CREATE_WIDGET)
                        .build()
                    workManager.enqueueUniqueWork(
                        CREATE_WIDGET,
                        ExistingWorkPolicy.APPEND_OR_REPLACE,
                        workRequest
                    )
                    navController.popBackStack()
                },
                {
                    navController.popBackStack()
                },
                onRemoveOption = viewModel::removeOption
            )
        }
    }
}


@Serializable
object Splash

@Serializable
object TestAPI

@Serializable
object Home

@Serializable
object Create

