package com.ask.app.ui

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ask.app.ui.screens.TestAPIScreen
import com.ask.app.ui.theme.ASKTheme
import com.ask.common.WidgetWithUserView
import com.ask.create.CreateWidgetScreen
import com.ask.home.HomeScreen
import com.ask.splash.SplashScreen
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
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


class WidgetWithOptionAndVotesForTargetAudiencePreviewParameters :
    PreviewParameterProvider<WidgetWithOptionsAndVotesForTargetAudience> {
    override val values: Sequence<WidgetWithOptionsAndVotesForTargetAudience>
        get() = sequenceOf(
            WidgetWithOptionsAndVotesForTargetAudience(
                widget = com.ask.widget.Widget(title = "Who will win the IPL?"),
                options = listOf(
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = com.ask.widget.Widget.Option(text = "Mumbai Indians"),
                        votes = emptyList()
                    ).apply {
                        votesPercent = 30f
                    },
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = com.ask.widget.Widget.Option(text = "Chennai Super Kings"),
                        votes = emptyList()
                    ).apply {
                        didUserVoted = true
                        votesPercent = 70f
                    }
                ),
                targetAudienceGender = com.ask.widget.Widget.TargetAudienceGender(),
                targetAudienceAgeRange = com.ask.widget.Widget.TargetAudienceAgeRange(),
                targetAudienceLocations = listOf(com.ask.widget.Widget.TargetAudienceLocation()),
                user = com.ask.user.User(name = "Shivam")
            ),
            WidgetWithOptionsAndVotesForTargetAudience(
                widget = com.ask.widget.Widget(title = "Who will win the IPL?"),
                options = listOf(
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = com.ask.widget.Widget.Option(imageUrl = "https://picsum.photos/id/237/200/300"),
                        votes = emptyList()
                    ).apply {
                        votesPercent = 45.6F
                    },
                    WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                        option = com.ask.widget.Widget.Option(imageUrl = "https://picsum.photos/id/237/200/300"),
                        votes = emptyList()
                    ).apply {
                        didUserVoted = true
                        votesPercent = 54.4F
                    }
                ),
                targetAudienceGender = com.ask.widget.Widget.TargetAudienceGender(),
                targetAudienceAgeRange = com.ask.widget.Widget.TargetAudienceAgeRange(),
                targetAudienceLocations = listOf(com.ask.widget.Widget.TargetAudienceLocation()),
                user = com.ask.user.User(name = "Shivam")
            )
        )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun WidgetWithUserViewPreview(
    @PreviewParameter(WidgetWithOptionAndVotesForTargetAudiencePreviewParameters::class) widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience,
) {
    ASKTheme {
        WidgetWithUserView(widgetWithOptionsAndVotesForTargetAudience = widgetWithOptionsAndVotesForTargetAudience)
    }
}

