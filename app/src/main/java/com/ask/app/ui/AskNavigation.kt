package com.ask.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ask.app.ui.screens.SplashScreen
import com.ask.app.ui.screens.TestAPIScreen
import kotlinx.serialization.Serializable

@Composable
fun AskNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = TestAPI) {
        composable<Splash> { SplashScreen() }
        composable<TestAPI> { TestAPIScreen() }
    }
}


@Serializable
object Splash

@Serializable
object TestAPI

