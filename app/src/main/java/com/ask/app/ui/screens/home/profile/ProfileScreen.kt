package com.ask.app.ui.screens.home.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ask.app.R
import com.ask.app.data.models.Gender
import com.ask.app.ui.screens.utils.AppImage
import com.ask.app.ui.screens.utils.AppOptionTypeSelect
import com.ask.app.ui.screens.utils.AppTextField
import com.ask.app.ui.screens.utils.DropDownWithSelect

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ProfileScreen(
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    profile: ProfileUiState,
    myWidgetsUiState: MyWidgetsUiState,
    setName: (String) -> Unit = {},
    setEmail: (String) -> Unit = {},
    setGender: (Gender) -> Unit = {},
    setCountry: (String) -> Unit = {},
    setAge: (Int) -> Unit = {},
    onUpdate: () -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onOptionClick: (String, String) -> Unit = { _, _ -> },
    onScreenOpen: (String) -> Unit = {}
) {
    val widthClass = sizeClass.widthSizeClass
    var selectedTab by remember { mutableStateOf(ProfileTab.Profile) }
    Column(
        modifier = Modifier.padding(all = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(20.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (tab in ProfileTab.entries) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(5.dp)
                            .background(
                                color = if (selectedTab == tab && widthClass == WindowWidthSizeClass.Compact) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(9.dp)
                            )
                            .clickable {
                                if (widthClass == WindowWidthSizeClass.Compact)
                                    selectedTab = tab
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = tab.title),
                            textAlign = TextAlign.Center,
                            color = if (selectedTab == tab && widthClass == WindowWidthSizeClass.Compact) Color.White else Color.Black
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(16.dp))
        LaunchedEffect(selectedTab) {
            onScreenOpen(selectedTab.name)
        }
        if (widthClass == WindowWidthSizeClass.Compact) {
            when (selectedTab) {
                ProfileTab.Profile -> ProfileTabView(
                    profile = profile,
                    onImageClick = onImageClick,
                    setName = setName,
                    setEmail = setEmail,
                    setGender = setGender,
                    setCountry = setCountry,
                    setAge = setAge,
                    onUpdate = onUpdate
                )

                ProfileTab.MyWidgets -> MyWidgetsScreen(
                    uiState = myWidgetsUiState,
                    onOptionClick = onOptionClick
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileTabView(
                    modifier = Modifier.weight(1f),
                    profile = profile,
                    onImageClick = onImageClick,
                    setName = setName,
                    setEmail = setEmail,
                    setGender = setGender,
                    setCountry = setCountry,
                    setAge = setAge,
                    onUpdate = onUpdate
                )
                MyWidgetsScreen(
                    uiState = myWidgetsUiState, onOptionClick = onOptionClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(
    backgroundColor = 0xFFFFFFFF,
    showBackground = true
)
@Composable
fun ProfileTabView(
    @PreviewParameter(ProfileTabViewPreviewParameter::class) profile: ProfileUiState,
    modifier: Modifier = Modifier,
    onImageClick: (String) -> Unit = {},
    setName: (String) -> Unit = {},
    setEmail: (String) -> Unit = {},
    setGender: (Gender) -> Unit = {},
    setCountry: (String) -> Unit = {},
    setAge: (Int) -> Unit = {},
    onUpdate: () -> Unit = {}
) {
    val singlePhotoPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                uri?.let { onImageClick(it.toString()) }
            })

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (profile.profileLoading) {
            CircularProgressIndicator()
        } else {
            Box(modifier = Modifier.size(20.dp))
            Box(modifier = Modifier) {
                AppImage(
                    url = profile.profilePic,
                    contentDescription = profile.name,
                    contentScale = ContentScale.Fit,
                    placeholder = R.drawable.baseline_account_box_24,
                    error = R.drawable.baseline_account_box_24,
                    modifier = Modifier
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
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
            AppTextField(
                hint = stringResource(R.string.name),
                value = profile.name,
                onValueChange = setName,
                isError = profile.nameError.isNotBlank(),
                errorMessage = profile.nameError
            )
            AppTextField(
                hint = stringResource(R.string.email),
                value = profile.email,
                onValueChange = setEmail,
                isError = profile.emailError.isNotBlank(),
                errorMessage = profile.emailError
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.gender),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                AppOptionTypeSelect(
                    selected = profile.gender == Gender.MALE,
                    { setGender(Gender.MALE) },
                    stringResource(id = R.string.male),
                    ImageVector.vectorResource(id = R.drawable.baseline_male_24)
                )
                Spacer(modifier = Modifier.size(5.dp))
                AppOptionTypeSelect(
                    selected = profile.gender == Gender.FEMALE,
                    { setGender(Gender.FEMALE) },
                    stringResource(id = R.string.female),
                    ImageVector.vectorResource(id = R.drawable.baseline_female_24)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.age),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                DropDownWithSelect(list = (profile.minAgeRange..profile.maxAgeRange).map { it },
                    title = profile.age?.toString() ?: "",
                    onItemSelect = setAge,
                    itemString = { it.toString() })
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.location),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                DropDownWithSelect(profile.countries,
                    profile.countries.find { it.name == profile.country }?.let {
                        "${it.emoji} ${it.name}"
                    } ?: stringResource(id = R.string.select),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    itemString = { "${it.emoji} ${it.name}" }) {
                    setCountry(it.name)
                }
            }
            Spacer(modifier = Modifier.size(20.dp))
            ElevatedButton(
                onClick = onUpdate,
                modifier = Modifier.fillMaxWidth(),
                enabled = profile.allowUpdate
            ) {
                Text(text = stringResource(R.string.update))
            }
            Spacer(modifier = Modifier.size(60.dp))
        }
    }
}

enum class ProfileTab(val title: Int) { Profile(R.string.profile), MyWidgets(R.string.my_widgets) }

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
