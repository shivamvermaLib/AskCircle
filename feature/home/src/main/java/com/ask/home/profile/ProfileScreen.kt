package com.ask.home.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ask.common.AppImage
import com.ask.common.AppOptionTypeSelect
import com.ask.common.AppTextField
import com.ask.common.DropDownWithSelect
import com.ask.common.connectivityState
import com.ask.common.getByteArray
import com.ask.common.getExtension
import com.ask.common.preLoadImages
import com.ask.common.shimmerBrush
import com.ask.core.EMPTY
import com.ask.home.R
import com.ask.user.Gender
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProfileScreen(
    route: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onError: (String, onDismiss: () -> Unit) -> Unit = { _, _ -> },
    onOpenImage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<ProfileViewModel>()
    val profileUiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        launch {
            viewModel.uiStateFlow.mapNotNull { it.error }.collect {
                onError(it) {
                    viewModel.setError(null)
                }
            }
        }
        viewModel.screenOpenEvent(route)
    }
    ProfileScreen(
        profileUiState,
        sharedTransitionScope,
        animatedContentScope,
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
            }, {
                context.preLoadImages(listOf(it))
            })
        },
        viewModel::onImageClick,
        onOpenImage
    )
}

@Preview
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
private fun ProfileScreen(
    @PreviewParameter(ProfileTabViewPreviewParameter::class) profile: ProfileUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    setName: (String) -> Unit = {},
    setEmail: (String) -> Unit = {},
    setGender: (Gender) -> Unit = {},
    setCountry: (String) -> Unit = {},
    setAge: (Int) -> Unit = {},
    onUpdate: () -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onOpenImage: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.profile))
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(all = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isConnected by connectivityState()
            val singlePhotoPickerLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri ->
                        uri?.let { onImageClick(it.toString()) }
                    })
            Box {
                if (profile.profileLoading) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(shimmerBrush(), shape = RoundedCornerShape(20.dp))
                    )
                } else {
                    with(sharedTransitionScope) {
                        AppImage(
                            url = profile.profilePic,
                            contentDescription = profile.name,
                            contentScale = ContentScale.Fit,
                            placeholder = R.drawable.baseline_account_box_24,
                            error = R.drawable.baseline_account_box_24,
                            modifier =  Modifier.Companion
                                .sharedElement(
                                    sharedTransitionScope.rememberSharedContentState(
                                        key = profile.profilePic ?: EMPTY
                                    ),
                                    animatedVisibilityScope = animatedContentScope
                                )
                                .height(160.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { profile.profilePic?.let { onOpenImage(it) } }
                        )
                    }
                }
                FilledIconButton(onClick = {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.edit_image)
                    )
                }
            }
            Spacer(modifier = Modifier.size(20.dp))
            if (profile.profileLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .background(shimmerBrush())
                )
            } else {
                AppTextField(
                    hint = stringResource(R.string.name),
                    value = profile.name,
                    onValueChange = setName,
                    isError = profile.nameError.isNotBlank(),
                    errorMessage = profile.nameError,
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            if (profile.profileLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .background(shimmerBrush())
                )
            } else {
                AppTextField(
                    hint = stringResource(R.string.email),
                    value = profile.email,
                    onValueChange = setEmail,
                    isError = profile.emailError.isNotBlank(),
                    errorMessage = profile.emailError,
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.gender),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                if (profile.profileLoading) {
                    Box(
                        modifier = Modifier
                            .height(45.dp)
                            .width(100.dp)
                            .background(shimmerBrush())
                    )
                } else {
                    AppOptionTypeSelect(
                        Modifier,
                        profile.gender == Gender.MALE,
                        { setGender(Gender.MALE) },
                        stringResource(id = R.string.male),
                        ImageVector.vectorResource(id = R.drawable.baseline_male_24)
                    )
                }
                Spacer(modifier = Modifier.size(5.dp))
                if (profile.profileLoading) {
                    Box(
                        modifier = Modifier
                            .height(45.dp)
                            .width(100.dp)
                            .background(shimmerBrush())
                    )
                } else {
                    AppOptionTypeSelect(
                        Modifier,
                        profile.gender == Gender.FEMALE,
                        { setGender(Gender.FEMALE) },
                        stringResource(id = R.string.female),
                        ImageVector.vectorResource(id = R.drawable.baseline_female_24)
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.age),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                if (profile.profileLoading) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(shimmerBrush())
                    )
                } else {
                    DropDownWithSelect(
                        list = (profile.minAgeRange..profile.maxAgeRange).map { it },
                        title = profile.age?.toString() ?: "",
                        onItemSelect = setAge,
                        itemString = { it.toString() },
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.location),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                if (profile.profileLoading) {
                    Box(
                        modifier = Modifier
                            .height(45.dp)
                            .width(100.dp)
                            .background(shimmerBrush())
                    )
                } else {
                    DropDownWithSelect(
                        list = profile.countries,
                        title = profile.countries.find { it.name == profile.country }?.let {
                            "${it.emoji} ${it.name}"
                        } ?: stringResource(id = R.string.select),
                        modifier = Modifier
                            .padding(horizontal = 4.dp),
                        itemString = { "${it.emoji} ${it.name}" }) {
                        setCountry(it.name)
                    }
                }
            }
            Spacer(modifier = Modifier.size(20.dp))
            ElevatedButton(
                onClick = onUpdate,
                modifier = Modifier.fillMaxWidth(),
                enabled = profile.allowUpdate && isConnected
            ) {
                Text(text = stringResource(R.string.update))
            }
            Spacer(modifier = Modifier.size(100.dp))
        }
    }
}


class ProfileTabViewPreviewParameter : PreviewParameterProvider<ProfileUiState> {
    override val values: Sequence<ProfileUiState>
        get() = sequenceOf(
            ProfileUiState(
                name = "Shivam Verma",
                email = "shivamverma@gmail.com",
                gender = Gender.MALE,
                country = "India",
                age = 22,
            )
        )
}
