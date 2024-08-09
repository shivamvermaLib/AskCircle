package com.ask.app

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.ask.admin.AdminScreen
import com.ask.create.CreateWidgetScreen
import com.ask.home.HomeScreen
import com.ask.home.R
import com.ask.imageview.ImageViewModel
import com.ask.imageview.ImageViewScreen
import com.ask.profile.ProfileScreen
import com.ask.splash.SplashScreen
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.widgetdetails.WidgetDetailScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AskNavigation(sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero)) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val lastVotedEmptyOptions = listOf(
        context.getString(R.string.your_voice_matters_vote_now),
        context.getString(R.string.shape_the_outcome_cast_your_vote),
        context.getString(R.string.join_the_conversation_vote_today),
        context.getString(R.string.be_a_trendsetter_vote_first),
        context.getString(R.string.get_involved_make_your_vote_count),
        context.getString(R.string.start_the_discussion_with_your_vote),
        context.getString(R.string.let_s_shape_the_future_vote_now),
        context.getString(R.string.vote_for_your_favorite_option)
    )
    SharedTransitionLayout {
        NavHost(navController = navController, startDestination = SplashScreen) {
            composable<SplashScreen>(deepLinks = listOf(navDeepLink {
                uriPattern = "https://ask-app-36527.web.app/widget/{widgetId}"
            }, navDeepLink {
                uriPattern = "ask-circle://widget/{widgetId}"
            })) {
                SplashScreen(Json.encodeToString(SplashScreen)) {
                    val widgetId = it.arguments?.getString("widgetId")
                    navController.navigate(HomeScreen(widgetId)) {
                        popUpTo(SplashScreen) {
                            inclusive = true
                        }
                    }
                }
            }
            composable<HomeScreen> { navBackStackEntry ->
                val route = navBackStackEntry.toRoute<HomeScreen>()
                val widgetId = route.widgetId
                LaunchedEffect(widgetId) {
                    widgetId?.let {
                        navController.navigate(WidgetDetailsScreen(0, widgetId))
                    }
                }
                HomeScreen(Json.encodeToString(route),
                    widgetId,
                    lastVotedEmptyOptions,
                    sizeClass,
                    this@SharedTransitionLayout,
                    this@composable,
                    {
                        navController.navigate(CreateScreen(Json.encodeToString(it)))
                    },
                    { index, widgetId ->
                        navController.navigate(WidgetDetailsScreen(index, widgetId))
                    },
                    {
                        navController.navigate(AdminScreen)
                    },
                    {
                        //settings
                    },
                    {
                        it?.let {
                            navController.navigate(ImageViewScreen(it))
                        }
                    },
                    { index, url ->
                        url?.let {
                            navController.navigate(ImageViewScreen(it, index))
                        }
                    })
            }
            composable<CreateScreen> {
                val route = it.toRoute<CreateScreen>()
                CreateWidgetScreen(
                    Json.encodeToString(route),
                    route.widgetJson?.let { it1 -> Json.decodeFromString(it1) },
                    sizeClass
                ) {
                    navController.popBackStack()
                }
            }
            composable<ProfileScreen> {
                ProfileScreen(Json.encodeToString(ProfileScreen),
                    this@SharedTransitionLayout,
                    this@composable,
                    { msg, onDismiss ->

                    },
                    {
                        navController.navigate(ImageViewScreen(it))
                    })
            }
            composable<ImageViewScreen> {
                val imageViewScreen = it.toRoute<ImageViewScreen>()
                val imageViewModel = hiltViewModel<ImageViewModel>()
                LaunchedEffect(Unit) {
                    imageViewModel.onImageOpen(imageViewScreen.imagePath)
                }
                ImageViewScreen(
                    imageViewScreen.index,
                    imageViewScreen.imagePath,
                    this@SharedTransitionLayout,
                    this@composable
                ) {
                    navController.popBackStack()
                }
            }
            composable<WidgetDetailsScreen> { navBackStackEntry ->
                val route = navBackStackEntry.toRoute<WidgetDetailsScreen>()
                WidgetDetailScreen(
                    Json.encodeToString(route),
                    route.index,
                    route.widgetId,
                    this@SharedTransitionLayout,
                    this@composable,
                    onOpenImage = {
                        it?.let {
                            navController.navigate(ImageViewScreen(it))
                        }
                    },
                    lastVotedEmptyOptions = lastVotedEmptyOptions,
                    onOpenIndexImage = { index, url ->
                        url?.let {
                            navController.navigate(ImageViewScreen(it, index))
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable<AdminScreen> {
                AdminScreen(
                    this@SharedTransitionLayout,
                    this@composable,
                    onAdminMoveToCreate = {
                        navController.navigate(CreateScreen(Json.encodeToString(it)))
                    },
                    {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}


@Serializable
object SplashScreen

@Serializable
data class HomeScreen(val widgetId: String? = null)

@Serializable
data class CreateScreen(val widgetJson: String? = null)

@Serializable
data object ProfileScreen

@Serializable
data class ImageViewScreen(val imagePath: String, val index: Int = -1)

@Serializable
data class WidgetDetailsScreen(val index: Int, val widgetId: String)

@Serializable
data object AdminScreen


class WidgetWithOptionAndVotesForTargetAudiencePreviewParameters :
    PreviewParameterProvider<WidgetWithOptionsAndVotesForTargetAudience> {
    override val values: Sequence<WidgetWithOptionsAndVotesForTargetAudience>
        get() = sequenceOf(
            WidgetWithOptionsAndVotesForTargetAudience(
                widget = Widget(title = "Who will win the IPL?"),
                options = listOf(WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                    option = Widget.Option(text = "Mumbai Indians"), votes = emptyList()
                ).apply {
                    votesPercent = 30f
                }, WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                    option = Widget.Option(text = "Chennai Super Kings"), votes = emptyList()
                ).apply {
                    didUserVoted = true
                    votesPercent = 70f
                }),
                targetAudienceGender = Widget.TargetAudienceGender(),
                targetAudienceAgeRange = Widget.TargetAudienceAgeRange(),
                targetAudienceLocations = listOf(Widget.TargetAudienceLocation()),
                user = com.ask.user.User(name = "Shivam"),
                categories = listOf(Widget.WidgetCategory(category = "Health")),
                isBookmarked = false
            ), WidgetWithOptionsAndVotesForTargetAudience(
                widget = Widget(title = "Who will win the IPL?"),
                options = listOf(WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                    option = Widget.Option(imageUrl = "https://picsum.photos/id/237/200/300"),
                    votes = emptyList()
                ).apply {
                    votesPercent = 45.6F
                }, WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                    option = Widget.Option(imageUrl = "https://picsum.photos/id/237/200/300"),
                    votes = emptyList()
                ).apply {
                    didUserVoted = true
                    votesPercent = 54.4F
                }),
                targetAudienceGender = Widget.TargetAudienceGender(),
                targetAudienceAgeRange = Widget.TargetAudienceAgeRange(),
                targetAudienceLocations = listOf(Widget.TargetAudienceLocation()),
                user = com.ask.user.User(name = "Shivam"),
                categories = listOf(Widget.WidgetCategory(category = "Health")),
                isBookmarked = true
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

