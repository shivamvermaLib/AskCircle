package com.ask.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ask.category.CategoryWithSubCategory
import com.ask.common.AppImage
import com.ask.common.AppOptionTypeSelect
import com.ask.common.AppTextField
import com.ask.common.DropDownWithSelect
import com.ask.common.connectivityState
import com.ask.common.shimmerBrush
import com.ask.common.toErrorString
import com.ask.core.EMPTY
import com.ask.core.ImageSizeType
import com.ask.core.getImage
import com.ask.user.Gender
import com.ask.user.User
import com.ask.workmanager.UpdateProfileWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProfileScreen(
    route: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onMessage: (String, onDismiss: () -> Unit) -> Unit = { _, _ -> },
    onOpenImage: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<ProfileViewModel>()
    val profileUiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        launch {
            viewModel.uiStateFlow.mapNotNull { it.error }.collect {
                onMessage(it) {
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
            UpdateProfileWorker.sendRequest(
                context,
                profileUiState.name,
                profileUiState.email,
                profileUiState.gender,
                profileUiState.age,
                profileUiState.profilePic,
                profileUiState.country,
                profileUiState.userCategories,
            )
            onMessage(context.getString(R.string.profile_update_in_progress)) {}
        },
        viewModel::onImageClick,
        onOpenImage,
        viewModel::onCategorySelect
    )
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalSharedTransitionApi::class)
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
    onOpenImage: (String) -> Unit = {},
    onCategorySelect: (List<User.UserCategory>) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(all = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isConnected by connectivityState()
        val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
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
                    AppImage(url = profile.profilePic.getImage(ImageSizeType.SIZE_500),
                        contentDescription = profile.name,
                        contentScale = ContentScale.Fit,
                        placeholder = R.drawable.baseline_account_box_24,
                        error = R.drawable.baseline_account_box_24,
                        modifier = Modifier.Companion
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(
                                    key = profile.profilePic.getImage(ImageSizeType.SIZE_ORIGINAL)
                                        ?: EMPTY
                                ), animatedVisibilityScope = animatedContentScope
                            )
                            .height(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                profile.profilePic?.let {
                                    onOpenImage(
                                        it.getImage(ImageSizeType.SIZE_ORIGINAL) ?: EMPTY
                                    )
                                }
                            })
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
                    Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_image)
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
                isError = profile.nameError != -1,
                errorMessage = profile.nameError.toErrorString(context),
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
                isError = profile.emailError != -1,
                errorMessage = profile.emailError.toErrorString(context),
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
                text = stringResource(R.string.age), style = MaterialTheme.typography.titleSmall
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
                DropDownWithSelect(list = profile.countries,
                    title = profile.countries.find { it.name == profile.country }?.let {
                        "${it.emoji} ${it.name}"
                    } ?: stringResource(id = R.string.select),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    itemString = { "${it.emoji} ${it.name}" }) {
                    setCountry(it.name)
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        CategorySelect(
            profile.profileLoading, profile.categories, profile.userCategories, onCategorySelect
        )
        Spacer(modifier = Modifier.size(20.dp))
        ElevatedButton(
            onClick = onUpdate,
            modifier = Modifier.fillMaxWidth(),
            enabled = profile.allowUpdate && isConnected && !profile.profileLoading
        ) {
            Text(text = stringResource(R.string.update))
        }
        Spacer(modifier = Modifier.size(100.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelect(
    loading: Boolean,
    list: List<CategoryWithSubCategory>,
    userCategories: List<User.UserCategory>,
    onCategorySelect: (List<User.UserCategory>) -> Unit,
) {
    var selectedList by remember { mutableStateOf(userCategories) }
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf(EMPTY) }
    var filterList by remember { mutableStateOf(listOf<CategoryWithSubCategory>()) }
    LaunchedEffect(userCategories) {
        selectedList = userCategories
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Interests", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.weight(1f))
        if (loading) {
            Box(
                modifier = Modifier
                    .height(45.dp)
                    .width(100.dp)
                    .background(shimmerBrush())
            )
        } else {
            AppOptionTypeSelect(
                selected = false,
                onSelectedChange = { expanded = true },
                title = "Select",
                icon = null
            )
        }
    }
    Spacer(modifier = Modifier.size(6.dp))
    if (userCategories.isNotEmpty()) {
        FlowRow(
            Modifier
                .fillMaxWidth(1f)
                .wrapContentHeight(align = Alignment.Top),
            horizontalArrangement = Arrangement.Start,
        ) {
            userCategories.fastForEach {
                FilterChip(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = { /* do something*/ },
                    label = { Text(it.subCategory.ifBlank { it.category }) },
                    selected = true,
                )
            }
        }
    }
    if (expanded) {
        filterList = list.filter { categoryWithSubCategory ->
            categoryWithSubCategory.category.name.contains(
                search, ignoreCase = true
            ) || categoryWithSubCategory.subCategories.fastAny {
                it.title.contains(
                    search, ignoreCase = true
                )
            }
        }
        Dialog(
            onDismissRequest = {
                expanded = false
                onCategorySelect(selectedList)
            },
            properties = DialogProperties(
                dismissOnBackPress = true, usePlatformDefaultWidth = true
            ),
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxSize(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Select Category",
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        expanded = false
                        onCategorySelect(selectedList)
                    }) {
                        Text(text = "Done")
                    }
                }
                AppTextField(
                    hint = "Search",
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filterList) { categoryWithSubCategory ->
                        fun onClick(category: String, subCategory: String? = null) {
                            selectedList =
                                if (subCategory != null && selectedList.any { it.subCategory == subCategory }) {
                                    selectedList.filter { it.subCategory != subCategory }
                                } else if (subCategory == null && selectedList.any { it.category == category }) {
                                    selectedList.filter { it.category != category }
                                } else {
                                    selectedList + User.UserCategory(
                                        category = category, subCategory = subCategory ?: ""
                                    )
                                }
                        }

                        val categorySelected =
                            selectedList.any { it.category == categoryWithSubCategory.category.name }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onClick(categoryWithSubCategory.category.name)
                                },
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onClick(categoryWithSubCategory.category.name) },
                                colors = CardDefaults.elevatedCardColors().copy(
                                    containerColor = if (categorySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Spacer(modifier = Modifier.size(16.dp))
                                Text(
                                    text = categoryWithSubCategory.category.name,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (categorySelected) Color.White else Color.Black,

                                    )
                                Spacer(modifier = Modifier.size(16.dp))
                                AnimatedVisibility(categorySelected) {
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {

                                        categoryWithSubCategory.subCategories.forEach { subCategory ->
                                            FilterChip(selected = selectedList.any { it.subCategory == subCategory.title },
                                                onClick = {
                                                    onClick(
                                                        categoryWithSubCategory.category.name,
                                                        subCategory.title
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        subCategory.title,
                                                        color = if (selectedList.any { it.subCategory == subCategory.title }) Color.Black else Color.White
                                                    )
                                                })
                                        }
                                    }
                                }
                                if (categorySelected) Spacer(modifier = Modifier.size(16.dp))
                            }
                        }

                    }
                }
            }
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
