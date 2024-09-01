package com.ask.imageview

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.ask.common.AppImage

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ImageViewScreen(
    imageViewIndex: Int,
    imageViewPath: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBack: () -> Unit
) {
    with(sharedTransitionScope) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                AppImage(
                    modifier = Modifier
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
}
