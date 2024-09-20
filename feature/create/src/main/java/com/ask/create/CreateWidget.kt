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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.ask.common.toErrorString
import com.ask.core.EMPTY
import com.ask.core.toDate
import com.ask.country.Country
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.workmanager.CreateWidgetWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Calendar


@Composable
fun CreateWidgetScreen(
    route: String,
    widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience?,
    sizeClass: WindowSizeClass, onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<CreateWidgetViewModel>()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.screenOpenEvent(route)
        widgetWithOptionsAndVotesForTargetAudience?.let {
            viewModel.setWidget(it)
        }
    }
    CreateWidgetScreen(sizeClass, uiState) {
        when (it) {
            CreateWidgetUiEvent.BackEvent -> onBack()
            CreateWidgetUiEvent.CreateWidgetEvent -> {
                CreateWidgetWorker.sendRequest(
                    context, uiState.toWidgetWithOptionsAndVotesForTargetAudience()
                )
                onBack()
            }

            else -> {
                viewModel.onEvent(it)
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class
)
@Preview
@Composable
private fun CreateWidgetScreen(
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    @PreviewParameter(CreateWidgetStatePreviewParameterProvider::class) createWidgetUiState: CreateWidgetUiState,
    onEvent: (CreateWidgetUiEvent) -> Unit = {},
) {
    val isConnected by connectivityState()
    val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
    var imagePickerIndex by remember { mutableIntStateOf(-1) }
    val context = LocalContext.current
    val singlePhotoPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (imagePickerIndex > -1) {
                    uri?.let {
                        onEvent(
                            CreateWidgetUiEvent.OptionChangedEvent(
                                imagePickerIndex,
                                createWidgetUiState.options[imagePickerIndex].copy(imageUrl = it.toString())
                            )
                        )
                    }
                }
            })
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            snackBarHostState.showSnackbar(
                message = context.getString(R.string.no_internet_connection),
                duration = SnackbarDuration.Indefinite
            )
        }
    }
    LaunchedEffect(createWidgetUiState.error) {
        if (createWidgetUiState.error != -1) {
            snackBarHostState.showSnackbar(
                message = context.getString(createWidgetUiState.error)
            )
            onEvent(CreateWidgetUiEvent.ErrorEvent(-1))
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
                IconButton(onClick = { onEvent(CreateWidgetUiEvent.BackEvent) }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close),
                    )
                }
            },
        )
    }) { padding ->
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
                    value = createWidgetUiState.widget.title,
                    onValueChange = {
                        onEvent(CreateWidgetUiEvent.TitleChangedEvent(it))
                    },
                    isError = createWidgetUiState.titleError != -1,
                    errorMessage = createWidgetUiState.titleError.toErrorString(context)
                )
                AppTextField(
                    hint = stringResource(R.string.description),
                    value = createWidgetUiState.widget.description ?: EMPTY,
                    onValueChange = {
                        onEvent(CreateWidgetUiEvent.DescChangedEvent(it))
                    },
                    minLines = 3,
                    maxLines = 8,
                    isError = createWidgetUiState.descError != -1,
                    errorMessage = createWidgetUiState.descError.toErrorString(context)
                )
                OptionTypeSelect(createWidgetUiState) {
                    onEvent(CreateWidgetUiEvent.OptionTypeChangedEvent(it))
                }
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
                            option = option,
                            didUserVoted = false,
                            totalOptions = createWidgetUiState.options.size,
                            isInput = true,
                            onDeleteIconClick = {
                                onEvent(CreateWidgetUiEvent.RemoveOptionEvent(index))
                            },
                            onOptionClick = {
                                imagePickerIndex = index
                                singlePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onOpenImage = {},
                            animatedContentScope = null,
                            sharedTransitionScope = null,
                            votesPercentFormat = EMPTY
                        )
                    }

                    CreateWidgetUiState.WidgetOptionType.Text -> Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        createWidgetUiState.options.forEachIndexed { index, option ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                TextOption(
                                    index = index,
                                    option = option,
                                    didUserVoted = false,
                                    isInput = true,
                                    onValueChange = {
                                        onEvent(
                                            CreateWidgetUiEvent.OptionChangedEvent(
                                                index,
                                                option.copy(text = it)
                                            )
                                        )
                                    },
                                    onClearIconClick = {
                                        onEvent(
                                            CreateWidgetUiEvent.OptionChangedEvent(
                                                index,
                                                option.copy(text = "")
                                            )
                                        )
                                    },
                                    onDeleteIconClick = {
                                        onEvent(CreateWidgetUiEvent.RemoveOptionEvent(index))
                                    },
                                    hasError = createWidgetUiState.optionError.contains(option.id),
                                    votesPercentFormat = EMPTY
                                )
                            }
                        }
                    }
                }

                TextButton(
                    modifier = Modifier.align(Alignment.Start),
                    onClick = {
                        onEvent(CreateWidgetUiEvent.AddOptionEvent)
                    },
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
                GenderSelect(createWidgetUiState) {
                    onEvent(CreateWidgetUiEvent.GenderChangedEvent(it))
                }
                Spacer(modifier = Modifier.size(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.marital_status),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    DropDownWithSelect(
                        list = Widget.MarriageStatusFilter.entries,
                        title = when (createWidgetUiState.targetAudienceGender.marriageStatusFilter) {
                            Widget.MarriageStatusFilter.ALL -> context.getString(R.string.all)
                            Widget.MarriageStatusFilter.SINGLE -> context.getString(R.string.single)
                            Widget.MarriageStatusFilter.MARRIED -> context.getString(R.string.married)
                            Widget.MarriageStatusFilter.DIVORCED -> context.getString(R.string.divorced)
                            Widget.MarriageStatusFilter.WIDOW -> context.getString(R.string.widow)
                        },
                        onItemSelect = {
                            onEvent(
                                CreateWidgetUiEvent.UpdateMarriageStatusFilterEvent(
                                    it
                                )
                            )
                        },
                        itemString = {
                            when (it) {
                                Widget.MarriageStatusFilter.ALL -> context.getString(R.string.all)
                                Widget.MarriageStatusFilter.SINGLE -> context.getString(R.string.single)
                                Widget.MarriageStatusFilter.MARRIED -> context.getString(R.string.married)
                                Widget.MarriageStatusFilter.DIVORCED -> context.getString(R.string.divorced)
                                Widget.MarriageStatusFilter.WIDOW -> context.getString(R.string.widow)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.size(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.education),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    DropDownWithSelect(
                        list = Widget.EducationFilter.entries,
                        title = when (createWidgetUiState.targetAudienceGender.educationFilter) {
                            Widget.EducationFilter.ALL -> context.getString(R.string.all)
                            Widget.EducationFilter.PRIMARY -> context.getString(R.string.primary)
                            Widget.EducationFilter.SECONDARY -> context.getString(R.string.secondary)
                            Widget.EducationFilter.HIGH_SCHOOL -> context.getString(R.string.high_school)
                            Widget.EducationFilter.UNDER_GRADUATE -> context.getString(R.string.under_graduate)
                            Widget.EducationFilter.POST_GRADUATE -> context.getString(R.string.post_graduate)
                            Widget.EducationFilter.DOC_OR_PHD -> context.getString(R.string.phd)
                        },
                        onItemSelect = {
                            onEvent(
                                CreateWidgetUiEvent.UpdateEducationFilterEvent(
                                    it
                                )
                            )
                        },
                        itemString = {
                            when (it) {
                                Widget.EducationFilter.ALL -> context.getString(R.string.all)
                                Widget.EducationFilter.PRIMARY -> context.getString(R.string.primary)
                                Widget.EducationFilter.SECONDARY -> context.getString(R.string.secondary)
                                Widget.EducationFilter.HIGH_SCHOOL -> context.getString(R.string.high_school)
                                Widget.EducationFilter.UNDER_GRADUATE -> context.getString(R.string.under_graduate)
                                Widget.EducationFilter.POST_GRADUATE -> context.getString(R.string.post_graduate)
                                Widget.EducationFilter.DOC_OR_PHD -> context.getString(R.string.phd)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.size(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.occupation),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    DropDownWithSelect(
                        list = Widget.OccupationFilter.entries,
                        title = when (createWidgetUiState.targetAudienceGender.occupationFilter) {
                            Widget.OccupationFilter.ALL -> context.getString(R.string.all)
                            Widget.OccupationFilter.EMPLOYED -> context.getString(R.string.employed)
                            Widget.OccupationFilter.SELF_EMPLOYED -> context.getString(R.string.self_employed)
                            Widget.OccupationFilter.UNEMPLOYED -> context.getString(R.string.unemployed)
                            Widget.OccupationFilter.RETIRED -> context.getString(R.string.retired)
                        },
                        onItemSelect = {
                            onEvent(
                                CreateWidgetUiEvent.UpdateOccupationFilterEvent(
                                    it
                                )
                            )
                        },
                        itemString = {
                            when (it) {
                                Widget.OccupationFilter.ALL -> context.getString(R.string.all)
                                Widget.OccupationFilter.EMPLOYED -> context.getString(R.string.employed)
                                Widget.OccupationFilter.SELF_EMPLOYED -> context.getString(R.string.self_employed)
                                Widget.OccupationFilter.UNEMPLOYED -> context.getString(R.string.unemployed)
                                Widget.OccupationFilter.RETIRED -> context.getString(R.string.retired)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.size(10.dp))
                AgeRangeSelect(
                    createWidgetUiState,
                    { onEvent(CreateWidgetUiEvent.MinAgeChangedEvent(it)) },
                    { onEvent(CreateWidgetUiEvent.MaxAgeChangedEvent(it)) })
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
                    {
                        onEvent(CreateWidgetUiEvent.SelectCountryEvent(it))
                    },
                    {
                        onEvent(CreateWidgetUiEvent.RemoveCountryEvent(it))
                    },
                )
                Spacer(modifier = Modifier.size(10.dp))
                CategorySelect(
                    createWidgetUiState.categories,
                    createWidgetUiState.widgetCategories
                ) {
                    onEvent(CreateWidgetUiEvent.SelectCategoryWidgetEvent(it))
                }
                Spacer(modifier = Modifier.size(15.dp))
                Text(
                    modifier = Modifier.align(Alignment.Start),
                    text = stringResource(R.string.more_options),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.size(10.dp))
                StartTime(createWidgetUiState) {
                    onEvent(
                        CreateWidgetUiEvent.StartTimeChangedEvent(
                            it
                        )
                    )
                }
                Spacer(modifier = Modifier.size(10.dp))
                EndTime(createWidgetUiState) { onEvent(CreateWidgetUiEvent.EndTimeChangedEvent(it)) }
                Spacer(modifier = Modifier.size(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.allow_anonymous_voters),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(createWidgetUiState.widget.allowAnonymous, {
                        onEvent(CreateWidgetUiEvent.AllowAnonymousEvent(it))
                    })
                }
                Spacer(modifier = Modifier.size(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Allow Multiple Selections",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(createWidgetUiState.widget.allowMultipleSelection, {
                        onEvent(CreateWidgetUiEvent.AllowMultipleSelection(it))
                    })
                }
                Spacer(modifier = Modifier.size(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Result")
                    Spacer(modifier = Modifier.weight(1f))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Widget.WidgetResult.entries.forEach { result ->
                            AppOptionTypeSelect(
                                selected = createWidgetUiState.widget.widgetResult == result,
                                onSelectedChange = {
                                    onEvent(CreateWidgetUiEvent.WidgetResultChangedEvent(result))
                                },
                                title = when (result) {
                                    Widget.WidgetResult.AFTER_VOTE -> "After Vote"
                                    Widget.WidgetResult.ALWAYS -> "Always"
                                    Widget.WidgetResult.TIME_END -> "After Ends At"
                                },
                                icon = null
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(20.dp))
                Button(
                    { onEvent(CreateWidgetUiEvent.CreateWidgetEvent) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndTime(createWidgetUiState: CreateWidgetUiState, onTimeChanged: (Long?) -> Unit) {
    var openPicker by remember { mutableStateOf(false) }
    val calendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            timeInMillis = createWidgetUiState.widget.startAt
            add(Calendar.DATE, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }
    LaunchedEffect(createWidgetUiState.widget.startAt) {
        calendar.timeInMillis = createWidgetUiState.widget.startAt
        calendar.add(Calendar.DATE, 1)
    }
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {

        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis >= calendar.timeInMillis
        }

        override fun isSelectableYear(year: Int): Boolean {
            return year in (calendar.get(Calendar.YEAR)..calendar.get(Calendar.YEAR) + createWidgetUiState.maxYearAllowed)
        }
    })
    if (openPicker) {
        DatePickerDialog(
            onDismissRequest = {
                openPicker = false
            },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onTimeChanged(it) }
                    openPicker = false
                }) {
                    Text(text = stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { openPicker = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }) {
            DatePicker(state = datePickerState)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.end_at), style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.weight(1f))
        AppOptionTypeSelect(
            selected = createWidgetUiState.widget.endAt != null,
            onSelectedChange = { openPicker = true },
            title = createWidgetUiState.widget.endAt?.toDate()
                ?: stringResource(id = R.string.select),
            icon = ImageVector.vectorResource(id = R.drawable.round_calendar_month_24)
        )
        Spacer(modifier = Modifier.size(5.dp))
        AppOptionTypeSelect(
            selected = createWidgetUiState.widget.endAt == null,
            onSelectedChange = { onTimeChanged(null) },
            title = stringResource(R.string.manual),
            icon = ImageVector.vectorResource(id = R.drawable.baseline_back_hand_24)
        )
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
        Widget.GenderFilter.entries.forEach { gender ->
            Spacer(modifier = Modifier.size(6.dp))
            AppOptionTypeSelect(
                selected = createWidgetUiState.targetAudienceGender.gender == gender,
                onSelectedChange = { onGenderChanged(gender) },
                title = when (gender) {
                    Widget.GenderFilter.ALL -> stringResource(R.string.all)
                    Widget.GenderFilter.MALE -> stringResource(R.string.male)
                    Widget.GenderFilter.FEMALE -> stringResource(id = R.string.female)
                },
                icon = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartTime(
    createWidgetUiState: CreateWidgetUiState,
    onTimeChanged: (Long) -> Unit
) {
    var openPicker by remember { mutableStateOf(false) }
    val calendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {

        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis >= calendar.timeInMillis
        }

        override fun isSelectableYear(year: Int): Boolean {
            return year in (calendar.get(Calendar.YEAR)..calendar.get(Calendar.YEAR) + createWidgetUiState.maxYearAllowed)
        }
    })
    if (openPicker) {
        DatePickerDialog(
            onDismissRequest = {
                openPicker = false
            },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onTimeChanged(it) }
                    openPicker = false
                }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { openPicker = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }) {
            DatePicker(state = datePickerState)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.start_at), style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.weight(1f))
        AppOptionTypeSelect(
            selected = false,
            onSelectedChange = { openPicker = true },
            title = createWidgetUiState.widget.startAt.toDate(),
            icon = ImageVector.vectorResource(id = R.drawable.round_calendar_month_24)
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
            text = stringResource(R.string.options),
            style = MaterialTheme.typography.titleMedium
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


class CreateWidgetStatePreviewParameterProvider :
    PreviewParameterProvider<CreateWidgetUiState> {
    override val values: Sequence<CreateWidgetUiState> = sequenceOf(
        CreateWidgetUiState(
            Widget("widget title"),
            -1,
            -1,
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
            Widget(),
            R.string.title_cannot_contain_bad_words,
            R.string.description_cannot_contain_bad_words,
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
