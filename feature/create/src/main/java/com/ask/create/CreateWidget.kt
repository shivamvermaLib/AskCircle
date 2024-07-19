package com.ask.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ask.category.CategoryWithSubCategory
import com.ask.common.AppOptionTypeSelect
import com.ask.common.AppTextField
import com.ask.common.DropDownWithSelect
import com.ask.common.ImageOption
import com.ask.common.NonLazyGrid
import com.ask.common.SelectItemOnChipWithSearchDropDown
import com.ask.common.TextOption
import com.ask.common.connectivityState
import com.ask.core.EMPTY
import com.ask.country.Country
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.workmanager.CreateWidgetWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi


@Composable
fun CreateWidgetScreen(
    route: String, sizeClass: WindowSizeClass, onBack: () -> Unit
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
                context, uiState.toWidgetWithOptionsAndVotesForTargetAudience()
            )
            onBack()
        },
        {
            onBack()
        },
        onRemoveOption = viewModel::removeOption,
        onSelectCategoryWidget = viewModel::selectCategoryWidget,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalSharedTransitionApi::class
)
@Preview
@Composable
private fun CreateWidgetScreen(
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    @PreviewParameter(CreateWidgetStatePreviewParameterProvider::class) createWidgetUiState: CreateWidgetUiState,
    setTitle: (String) -> Unit = {},
    setDesc: (String) -> Unit = {},
    onOptionTypeChanged: (CreateWidgetUiState.WidgetOptionType) -> Unit = {},
    onOptionChanged: (Int, Widget.Option) -> Unit = { _, _ -> },
    onAddOption: () -> Unit = {},
    onGenderChanged: (Widget.GenderFilter) -> Unit = {},
    onMinAgeChanged: (Int) -> Unit = {},
    onMaxAgeChanged: (Int) -> Unit = {},
    onSelectCountry: (Country) -> Unit = {},
    onRemoveCountry: (Country) -> Unit = {},
    onCreateClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onRemoveOption: (Int) -> Unit = {},
    onSelectCategoryWidget: (List<Widget.WidgetCategory>) -> Unit = {},
) {
    val isConnected by connectivityState()
    val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
    var imagePickerIndex by remember { mutableIntStateOf(-1) }
    val singlePhotoPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
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
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            snackBarHostState.showSnackbar(
                message = "No Internet Connection", duration = SnackbarDuration.Indefinite
            )
        }
    }
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
                            optionWithVotes = WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                                option = option, votes = emptyList()
                            ),
                            didUserVoted = false,
                            totalOptions = createWidgetUiState.options.size,
                            isInput = true,
                            onDeleteIconClick = onRemoveOption,
                            onImageClick = {
                                imagePickerIndex = index
                                singlePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onOpenImage = {},
                            animatedContentScope = null,
                            sharedTransitionScope = null
                        )
                    }

                    CreateWidgetUiState.WidgetOptionType.Text -> Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        createWidgetUiState.options.forEachIndexed { index, option ->
                            Row {
                                TextOption(index = index,
                                    widgetOption = WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
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
                    enabled = createWidgetUiState.options.size < 4
                ) {
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
                SelectItemOnChipWithSearchDropDown(
                    stringResource(id = R.string.location),
                    createWidgetUiState.countries,
                    createWidgetUiState.targetAudienceLocations.mapNotNull {
                        it.country
                    }.mapNotNull { country ->
                        createWidgetUiState.countries.find { it.name == country }
                    },
                    {
                        "${it.emoji} ${it.name}"
                    },
                    onSelectCountry,
                    onRemoveCountry,
                )
                Spacer(modifier = Modifier.size(10.dp))
                CategorySelect(
                    createWidgetUiState.categories,
                    createWidgetUiState.widgetCategories,
                    onSelectCategoryWidget
                )
                Spacer(modifier = Modifier.size(20.dp))
                Button(
                    onCreateClick,
                    enabled = createWidgetUiState.allowCreate && isConnected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.create))
                }
                Spacer(modifier = Modifier.size(100.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelect(
    list: List<CategoryWithSubCategory>,
    widgetCategory: List<Widget.WidgetCategory>,
    onCategorySelect: (List<Widget.WidgetCategory>) -> Unit,
) {
    var selectedList by remember { mutableStateOf(widgetCategory) }
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf(EMPTY) }
    var filterList by remember { mutableStateOf(listOf<CategoryWithSubCategory>()) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Category", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.weight(1f))
        AppOptionTypeSelect(
            selected = false, onSelectedChange = { expanded = true }, title = "Select", icon = null
        )
    }
    Spacer(modifier = Modifier.size(6.dp))
    if (widgetCategory.isNotEmpty())
        FlowRow(
            Modifier
                .fillMaxWidth(1f)
                .wrapContentHeight(align = Alignment.Top),
            horizontalArrangement = Arrangement.Start,
        ) {
            widgetCategory.fastForEach {
                FilterChip(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = { /* do something*/ },
                    label = { Text(it.subCategory.ifBlank { it.category }) },
                    selected = true,
                )
            }
        }
    if (expanded) {
        filterList = list.filter { categoryWithSubCategory ->
            categoryWithSubCategory.category.name.contains(
                search,
                ignoreCase = true
            ) || categoryWithSubCategory.subCategories.fastAny {
                it.title.contains(
                    search,
                    ignoreCase = true
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
                                    selectedList + Widget.WidgetCategory(
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
                                            FilterChip(
                                                selected = selectedList.any { it.subCategory == subCategory.title },
                                                onClick = {
                                                    onClick(
                                                        categoryWithSubCategory.category.name,
                                                        subCategory.title
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        subCategory.title, color =
                                                        if (selectedList.any { it.subCategory == subCategory.title }) Color.Black else Color.White
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                                if (categorySelected)
                                    Spacer(modifier = Modifier.size(16.dp))
                            }
                        }

                    }
                }
            }
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
        Text(
            text = stringResource(R.string.age_range), style = MaterialTheme.typography.titleSmall
        )
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
    createWidgetUiState: CreateWidgetUiState, onGenderChanged: (Widget.GenderFilter) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.gender), style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.weight(1f))
        AppOptionTypeSelect(
            selected = createWidgetUiState.targetAudienceGender.gender == Widget.GenderFilter.ALL,
            onSelectedChange = { onGenderChanged(Widget.GenderFilter.ALL) },
            title = stringResource(R.string.all),
            icon = null
        )
        Spacer(modifier = Modifier.size(5.dp))
        AppOptionTypeSelect(
            selected = createWidgetUiState.targetAudienceGender.gender == Widget.GenderFilter.MALE,
            onSelectedChange = { onGenderChanged(Widget.GenderFilter.MALE) },
            title = stringResource(R.string.male),
            icon = ImageVector.vectorResource(id = R.drawable.baseline_male_24)
        )
        Spacer(modifier = Modifier.size(5.dp))
        AppOptionTypeSelect(
            selected = createWidgetUiState.targetAudienceGender.gender == Widget.GenderFilter.FEMALE,
            onSelectedChange = { onGenderChanged(Widget.GenderFilter.FEMALE) },
            title = stringResource(R.string.female),
            icon = ImageVector.vectorResource(id = R.drawable.baseline_female_24)
        )
    }
}

@Composable
fun OptionTypeSelect(
    createWidgetUiState: CreateWidgetUiState,
    onOptionTypeChanged: (CreateWidgetUiState.WidgetOptionType) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.options), style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.weight(1f))
        AppOptionTypeSelect(
            selected = createWidgetUiState.optionType == CreateWidgetUiState.WidgetOptionType.Text,
            onSelectedChange = { onOptionTypeChanged.invoke(CreateWidgetUiState.WidgetOptionType.Text) },
            title = stringResource(R.string.text),
            icon = ImageVector.vectorResource(id = R.drawable.baseline_text_fields_24)
        )
        Spacer(modifier = Modifier.size(5.dp))
        AppOptionTypeSelect(
            selected = createWidgetUiState.optionType == CreateWidgetUiState.WidgetOptionType.Image,
            onSelectedChange = { onOptionTypeChanged.invoke(CreateWidgetUiState.WidgetOptionType.Image) },
            title = stringResource(R.string.image),
            icon = ImageVector.vectorResource(id = R.drawable.baseline_image_24)
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
                Widget.Option(
                    imageUrl = "https://example.com/image.jpg"
                ), Widget.Option(
                    imageUrl = "https://example.com/image.jpg"
                )
            ),
            Widget.TargetAudienceGender(gender = Widget.GenderFilter.MALE),
            listOf(Widget.TargetAudienceLocation()),
            listOf(
                Country(name = "India", emoji = ""),
                Country(name = "Australia", emoji = ""),
                Country(name = "Italy", emoji = ""),
                Country(name = "Greece", emoji = ""),
                Country(name = "USA", emoji = ""),
                Country(name = "Brazil", emoji = ""),
                Country(name = "Nepal", emoji = "")
            ),
            targetAudienceAgeRange = Widget.TargetAudienceAgeRange(
                min = 45, max = 66
            ),
        ), CreateWidgetUiState(
            "widget title",
            "widget title error",
            "desc",
            "desc error",
            CreateWidgetUiState.WidgetOptionType.Text,
            listOf(
                Widget.Option(
                    text = "Option 1"
                ), Widget.Option(
                    text = "Option 2"
                )
            ),
            Widget.TargetAudienceGender(gender = Widget.GenderFilter.MALE),
            targetAudienceAgeRange = Widget.TargetAudienceAgeRange(
                min = 45, max = 66
            ),
            targetAudienceLocations = listOf(Widget.TargetAudienceLocation()),
            countries = listOf(
                Country(name = "India", emoji = ""),
                Country(name = "Australia", emoji = ""),
                Country(name = "Nepal", emoji = "")
            )
        )
    )
}
