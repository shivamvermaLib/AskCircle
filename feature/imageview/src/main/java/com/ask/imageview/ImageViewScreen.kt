package com.ask.imageview

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.ask.common.AppImage

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ImageViewScreen(
    imageViewIndex: Int,
    imageViewPath: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    with(sharedTransitionScope) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppImage(
                modifier = Modifier.Companion
                    .sharedElement(
                        sharedTransitionScope.rememberSharedContentState(
                            key = when (imageViewIndex) {
                                -1 -> imageViewPath
                                else -> "${imageViewIndex}${imageViewPath}"
                            }
                        ),
                        animatedVisibilityScope = animatedContentScope
                    )
                    .align(Alignment.Center)
                    .aspectRatio(1f)
                    .fillMaxSize()
                    .background(color = Color.White.copy(alpha = 0.7f)),
                url = imageViewPath,
                contentDescription = "Image",
                contentScale = ContentScale.Fit,
                placeholder = R.drawable.baseline_image_24,
                error = R.drawable.baseline_broken_image_24,
            )
        }
    }
}
