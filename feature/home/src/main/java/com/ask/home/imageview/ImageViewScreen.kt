package com.ask.home.imageview

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import com.ask.common.AppImage
import com.ask.home.HomeTabScreen
import com.ask.home.R

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ImageViewScreen(
    imageView: HomeTabScreen.ImageView,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBackClick: () -> Unit,
) {
    with(sharedTransitionScope) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppImage(
                modifier = Modifier.Companion
                    .sharedElement(
                        sharedTransitionScope.rememberSharedContentState(
                            key = when (imageView.index) {
                                -1 -> imageView.imagePath
                                else -> "${imageView.index}${imageView.imagePath}"
                            }
                        ),
                        animatedVisibilityScope = animatedContentScope
                    )
                    .align(Alignment.Center)
                    .aspectRatio(1f)
                    .fillMaxSize()
                    .background(color = Color.White.copy(alpha = 0.7f)),
                url = imageView.imagePath,
                contentDescription = "Image",
                contentScale = ContentScale.Fit,
                placeholder = R.drawable.baseline_image_24,
                error = R.drawable.baseline_broken_image_24,
            )
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = "Back"
                )
            }
        }
    }
}
