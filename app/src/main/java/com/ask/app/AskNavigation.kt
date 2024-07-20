package com.ask.app

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.ask.create.CreateWidgetScreen
import com.ask.home.HomeScreen
import com.ask.splash.SplashScreen
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AskNavigation(sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero)) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SplashScreen) {
        composable<SplashScreen>(deepLinks = listOf(navDeepLink {
            uriPattern = "https://ask-app-36527.web.app/widget/{widgetId}"
        }, navDeepLink {
            uriPattern = "ask-circle://widget/{widgetId}"
        })) {
            SplashScreen(Json.encodeToString(SplashScreen)) {
                val widgetId = it.arguments?.getString("widgetId")
                println("WidgetID: $widgetId")
                navController.navigate(HomeScreen(widgetId)) {
                    popUpTo(SplashScreen) {
                        inclusive = true
                    }
                }
            }
        }
//        composable<TestAPI> { TestAPIScreen() }
        composable<HomeScreen> {
            val route = it.toRoute<HomeScreen>()
            val widgetId = route.widgetId
            HomeScreen(Json.encodeToString(route), widgetId, sizeClass) {
                navController.navigate(CreateScreen)
            }
        }
        composable<CreateScreen> {
            CreateWidgetScreen(Json.encodeToString(CreateScreen), sizeClass) {
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
data class HomeScreen(val widgetId: String? = null)

@Serializable
object CreateScreen


class WidgetWithOptionAndVotesForTargetAudiencePreviewParameters :
    PreviewParameterProvider<WidgetWithOptionsAndVotesForTargetAudience> {
    override val values: Sequence<WidgetWithOptionsAndVotesForTargetAudience>
        get() = sequenceOf(
            WidgetWithOptionsAndVotesForTargetAudience(
                widget = Widget(title = "Who will win the IPL?"),
                options = listOf(
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = Widget.Option(text = "Mumbai Indians"),
                        votes = emptyList()
                    ).apply {
                        votesPercent = 30f
                    },
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = Widget.Option(text = "Chennai Super Kings"),
                        votes = emptyList()
                    ).apply {
                        didUserVoted = true
                        votesPercent = 70f
                    }
                ),
                targetAudienceGender = Widget.TargetAudienceGender(),
                targetAudienceAgeRange = Widget.TargetAudienceAgeRange(),
                targetAudienceLocations = listOf(Widget.TargetAudienceLocation()),
                user = com.ask.user.User(name = "Shivam"),
                categories = listOf(Widget.WidgetCategory(category = "Health"))
            ),
            WidgetWithOptionsAndVotesForTargetAudience(
                widget = Widget(title = "Who will win the IPL?"),
                options = listOf(
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = Widget.Option(imageUrl = "https://picsum.photos/id/237/200/300"),
                        votes = emptyList()
                    ).apply {
                        votesPercent = 45.6F
                    },
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = Widget.Option(imageUrl = "https://picsum.photos/id/237/200/300"),
                        votes = emptyList()
                    ).apply {
                        didUserVoted = true
                        votesPercent = 54.4F
                    }
                ),
                targetAudienceGender = Widget.TargetAudienceGender(),
                targetAudienceAgeRange = Widget.TargetAudienceAgeRange(),
                targetAudienceLocations = listOf(Widget.TargetAudienceLocation()),
                user = com.ask.user.User(name = "Shivam"),
                categories = listOf(Widget.WidgetCategory(category = "Health"))
            )
        )
}

/*
@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun WidgetWithUserViewPreview(
    @PreviewParameter(WidgetWithOptionAndVotesForTargetAudiencePreviewParameters::class) widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
) {
    ASKTheme {
        SharedTransitionLayout {
            WidgetWithUserView(
                widgetWithOptionsAndVotesForTargetAudience = widgetWithOptionsAndVotesForTargetAudience,
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedContentScope =
            )
        }
    }
}
*/

