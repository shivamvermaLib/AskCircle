package com.ask.app.ui

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ask.app.ui.screens.TestAPIScreen
import com.ask.create.CreateWidgetScreen
import com.ask.home.HomeScreen
import com.ask.splash.SplashScreen
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AskNavigation(sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero)) {
    val navController = rememberNavController()


    NavHost(navController = navController, startDestination = SplashScreen) {
        composable<SplashScreen> {
            SplashScreen(it.destination.route ?: "Splash") {
                navController.navigate(HomeScreen) {
                    popUpTo(SplashScreen) {
                        inclusive = true
                    }
                }
            }
        }
        composable<TestAPI> { TestAPIScreen() }
        composable<HomeScreen> {
            HomeScreen(it.destination.route ?: "Home", sizeClass) {
                navController.navigate(CreateScreen)
            }
        }
        composable<CreateScreen> {
            CreateWidgetScreen(it.destination.route ?: "Create", sizeClass) {
                navController.popBackStack()
            }
        }
    }
}


@Serializable
object SplashScreen

@Serializable
object TestAPI

@Serializable
object HomeScreen

@Serializable
object CreateScreen

