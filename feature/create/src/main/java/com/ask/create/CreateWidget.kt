package com.ask.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkManager
import com.ask.common.AppOptionTypeSelect
import com.ask.common.AppTextField
import com.ask.common.DropDownWithSelect
import com.ask.common.ImageOption
import com.ask.common.NonLazyGrid
import com.ask.common.TextOption
import com.ask.country.Country
import com.ask.workmanager.CreateWidgetWorker


@Composable
fun CreateWidgetScreen(
    route: String,
    sizeClass: WindowSizeClass,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<CreateWidgetViewModel>()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.screenOpenEvent(route)
    }
    CreateWidgetScreen(
        sizeClass,
        uiState,
        viewModel::setTitle,
        viewModel::setDesc,
        viewModel::setOptionType,
        viewModel::updateOption,
        viewModel::addOption,
        viewModel::setGender,
        viewModel::setMinAge,
        viewModel::setMaxAge,
        viewModel::addLocation,
        viewModel::removeLocation,
        {
            CreateWidgetWorker.sendRequest(
                context,
                uiState.toWidgetWithOptionsAndVotesForTargetAudience()
            )
            onBack()
        },
        {
            onBack()
        },
        onRemoveOption = viewModel::removeOption
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun CreateWidgetScreen(
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    @PreviewParameter(CreateWidgetStatePreviewParameterProvider::class) createWidgetUiState: CreateWidgetUiState,
    setTitle: (String) -> Unit = {},
    setDesc: (String) -> Unit = {},
    onOptionTypeChanged: (CreateWidgetUiState.WidgetOptionType) -> Unit = {},
    onOptionChanged: (Int, com.ask.widget.Widget.Option) -> Unit = { _, _ -> },
    onAddOption: () -> Unit = {},
    onGenderChanged: (com.ask.widget.Widget.GenderFilter) -> Unit = {},
    onMinAgeChanged: (Int) -> Unit = {},
    onMaxAgeChanged: (Int) -> Unit = {},
    onSelectCountry: (Country) -> Unit = {},
    onRemoveCountry: (Country) -> Unit = {},
    onCreateClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onRemoveOption: (Int) -> Unit = {}
) {
    val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
    var imagePickerIndex by remember { mutableIntStateOf(-1) }
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (imagePickerIndex > -1) {
                uri?.let {
                    onOptionChanged(
                        imagePickerIndex,
                        createWidgetUiState.options[imagePickerIndex].copy(imageUrl = it.toString())
                    )
                }
            }
        })
    Scaffold(snackbarHost = {
        SnackbarHost(
            hostState = snackBarHostState, modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Snackbar {
                Text(it.visuals.message)
            }
        }
    }, topBar = {
        MediumTopAppBar(
            title = {
                Text(text = "Create")
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close),
                    )
                }
            },
        )
    }) { padding ->
        println("width = [${sizeClass.widthSizeClass}]")
        val modifier = if (sizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
            Modifier
                .padding(padding)
                .padding(all = 16.dp)
        } else {
            Modifier
                .fillMaxWidth(
                    when (sizeClass.widthSizeClass) {
                        WindowWidthSizeClass.Medium -> 0.8f
                        WindowWidthSizeClass.Expanded -> 0.6f
                        else -> 1f
                    }
                )
                .padding(padding)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppTextField(
                    hint = stringResource(R.string.title),
                    value = createWidgetUiState.title,
                    onValueChange = setTitle,
                    isError = createWidgetUiState.titleError.isNotBlank(),
                    errorMessage = createWidgetUiState.titleError
                )
                AppTextField(
                    hint = stringResource(R.string.description),
                    value = createWidgetUiState.desc,
                    onValueChange = setDesc,
                    minLines = 3,
                    maxLines = 8,
                )
                OptionTypeSelect(createWidgetUiState, onOptionTypeChanged)
                Spacer(modifier = Modifier.size(5.dp))
                when (createWidgetUiState.optionType) {
                    CreateWidgetUiState.WidgetOptionType.Image -> NonLazyGrid(
                        modifier = Modifier,
                        rowModifier = Modifier,
                        spacing = 6.dp,
                        columns = 2,
                        itemCount = createWidgetUiState.options.size
                    ) { index ->
                        val option = createWidgetUiState.options[index]
                        ImageOption(
                            index = index,
                            optionWithVotes = com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                                option = option, votes = emptyList()
                            ),
                            didUserVoted = false,
                            totalOptions = createWidgetUiState.options.size,
                            isInput = true,
                            onDeleteIconClick = onRemoveOption
                        ) {
                            imagePickerIndex = index
                            singlePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    }

                    CreateWidgetUiState.WidgetOptionType.Text -> Column(
                        verticalArrangement = Arrangement.spacedBy(
                            6.dp
                        )
                    ) {
                        createWidgetUiState.options.forEachIndexed { index, option ->
                            Row {
                                TextOption(index = index,
                                    widgetOption = com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                                        option = option, votes = emptyList()
                                    ),
                                    didUserVoted = false,
                                    isInput = true,
                                    onValueChange = {
                                        onOptionChanged(index, option.copy(text = it))
                                    },
                                    onClearIconClick = {
                                        onOptionChanged(index, option.copy(text = ""))
                                    },
                                    onDeleteIconClick = {
                                        onRemoveOption(index)
                                    })
                            }
                        }
                    }
                }

                TextButton(
                    modifier = Modifier.align(Alignment.Start),
                    onClick = onAddOption,
                    enabled = createWidgetUiState.options.size < 4) {
                    Text(text = stringResource(R.string.add_option))
                }
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    modifier = Modifier.align(Alignment.Start),
                    text = stringResource(R.string.target_audience),
                    style = MaterialTheme.typography.titleMedium
                )
                GenderSelect(createWidgetUiState, onGenderChanged)
                AgeRangeSelect(createWidgetUiState, onMinAgeChanged, onMaxAgeChanged)
                Spacer(modifier = Modifier.size(10.dp))
                LocationSelect(createWidgetUiState, onSelectCountry, onRemoveCountry)
                Spacer(modifier = Modifier.size(20.dp))
                Button(
                    onCreateClick,
                    enabled = createWidgetUiState.allowCreate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(com.ask.common.R.string.create))
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationSelect(
    createWidgetUiState: CreateWidgetUiState,
    onSelectCountry: (Country) -> Unit,
    onRemoveCountry: (Country) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(com.ask.common.R.string.location),
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.size(6.dp))
    FlowRow(
        Modifier
            .fillMaxWidth(1f)
            .wrapContentHeight(align = Alignment.Top),
        horizontalArrangement = Arrangement.Start,
    ) {
        createWidgetUiState.targetAudienceLocations.mapNotNull {
            it.country
        }.mapNotNull { country ->
            createWidgetUiState.countries.find { it.name == country }
        }.fastForEach {
            FilterChip(modifier = Modifier
                .padding(horizontal = 4.dp)
                .align(alignment = Alignment.CenterVertically),
                onClick = { /* do something*/ },
                label = { Text("${it.emoji} ${it.name}") },
                selected = true,
                trailingIcon = {
                    Icon(imageVector = Icons.Rounded.Close,
                        contentDescription = it.name,
                        modifier = Modifier
                            .size(FilterChipDefaults.IconSize)
                            .clickable { onRemoveCountry(it) })
                })
        }
        DropDownWithSelect(createWidgetUiState.countries,
            stringResource(com.ask.common.R.string.select),
            modifier = Modifier.padding(horizontal = 4.dp),
            itemString = { "${it.emoji} ${it.name}" }) {
            onSelectCountry(it)
        }
    }
}

@Composable
fun AgeRangeSelect(
    createWidgetUiState: CreateWidgetUiState,
    onMinAgeChanged: (Int) -> Unit,
    onMaxAgeChanged: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = stringResource(R.string.age_range), style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.weight(1f))
        DropDownWithSelect(list = (createWidgetUiState.minAge..createWidgetUiState.maxAge).map { it },
            title = createWidgetUiState.targetAudienceAgeRange.min.toString(),
            onItemSelect = onMinAgeChanged,
            itemString = { it.toString() })
        Spacer(modifier = Modifier.size(6.dp))
        DropDownWithSelect(list = (createWidgetUiState.minAge..createWidgetUiState.maxAge).map { it },
            title = createWidgetUiState.targetAudienceAgeRange.max.toString(),
            onItemSelect = onMaxAgeChanged,
            itemString = { it.toString() })
    }
}

@Composable
fun GenderSelect(
    createWidgetUiState: CreateWidgetUiState,
    onGenderChanged: (com.ask.widget.Widget.GenderFilter) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(com.ask.common.R.string.gender),
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.weight(1f))
        AppOptionTypeSelect(
            selected = createWidgetUiState.targetAudienceGender.gender == com.ask.widget.Widget.GenderFilter.ALL,
            { onGenderChanged(com.ask.widget.Widget.GenderFilter.ALL) },
            stringResource(R.string.all),
            null
        )
        Spacer(modifier = Modifier.size(5.dp))
        AppOptionTypeSelect(
            selected = createWidgetUiState.targetAudienceGender.gender == com.ask.widget.Widget.GenderFilter.MALE,
            { onGenderChanged(com.ask.widget.Widget.GenderFilter.MALE) },
            stringResource(com.ask.common.R.string.male),
            ImageVector.vectorResource(id = com.ask.common.R.drawable.baseline_male_24)
        )
        Spacer(modifier = Modifier.size(5.dp))
        AppOptionTypeSelect(
            selected = createWidgetUiState.targetAudienceGender.gender == com.ask.widget.Widget.GenderFilter.FEMALE,
            { onGenderChanged(com.ask.widget.Widget.GenderFilter.FEMALE) },
            stringResource(com.ask.common.R.string.female),
            ImageVector.vectorResource(id = com.ask.common.R.drawable.baseline_female_24)
        )
    }
}

@Composable
fun OptionTypeSelect(
    createWidgetUiState: CreateWidgetUiState,
    onOptionTypeChanged: (CreateWidgetUiState.WidgetOptionType) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = stringResource(R.string.options), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.weight(1f))
        AppOptionTypeSelect(
            selected = createWidgetUiState.optionType == CreateWidgetUiState.WidgetOptionType.Text,
            { onOptionTypeChanged.invoke(CreateWidgetUiState.WidgetOptionType.Text) },
            stringResource(R.string.text),
            ImageVector.vectorResource(id = R.drawable.baseline_text_fields_24)
        )
        Spacer(modifier = Modifier.size(5.dp))
        AppOptionTypeSelect(
            selected = createWidgetUiState.optionType == CreateWidgetUiState.WidgetOptionType.Image,
            { onOptionTypeChanged.invoke(CreateWidgetUiState.WidgetOptionType.Image) },
            stringResource(R.string.image),
            ImageVector.vectorResource(id = com.ask.common.R.drawable.baseline_image_24)
        )
    }
}


class CreateWidgetStatePreviewParameterProvider : PreviewParameterProvider<CreateWidgetUiState> {
    override val values: Sequence<CreateWidgetUiState> = sequenceOf(
        CreateWidgetUiState(
            "widget title",
            "widget title error",
            "desc",
            "desc error",
            CreateWidgetUiState.WidgetOptionType.Image,
            listOf(
                com.ask.widget.Widget.Option(
                    imageUrl = "https://example.com/image.jpg"
                ), com.ask.widget.Widget.Option(
                    imageUrl = "https://example.com/image.jpg"
                )
            ),
            com.ask.widget.Widget.TargetAudienceGender(gender = com.ask.widget.Widget.GenderFilter.MALE),
            listOf(com.ask.widget.Widget.TargetAudienceLocation()),
            listOf(
                Country(name = "India", emoji = ""),
                Country(name = "Australia", emoji = ""),
                Country(name = "Italy", emoji = ""),
                Country(name = "Greece", emoji = ""),
                Country(name = "USA", emoji = ""),
                Country(name = "Brazil", emoji = ""),
                Country(name = "Nepal", emoji = "")
            ),
            targetAudienceAgeRange = com.ask.widget.Widget.TargetAudienceAgeRange(
                min = 45, max = 66
            ),
        ), CreateWidgetUiState(
            "widget title",
            "widget title error",
            "desc",
            "desc error",
            CreateWidgetUiState.WidgetOptionType.Text,
            listOf(
                com.ask.widget.Widget.Option(
                    text = "Option 1"
                ), com.ask.widget.Widget.Option(
                    text = "Option 2"
                )
            ),
            com.ask.widget.Widget.TargetAudienceGender(gender = com.ask.widget.Widget.GenderFilter.MALE),
            targetAudienceAgeRange = com.ask.widget.Widget.TargetAudienceAgeRange(
                min = 45, max = 66
            ),
            targetAudienceLocations = listOf(com.ask.widget.Widget.TargetAudienceLocation()),
            countries = listOf(
                Country(name = "India", emoji = ""),
                Country(name = "Australia", emoji = ""),
                Country(name = "Nepal", emoji = "")
            )
        )
    )
}
