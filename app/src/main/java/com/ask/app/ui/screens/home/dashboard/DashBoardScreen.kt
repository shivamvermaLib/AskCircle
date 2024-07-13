package com.ask.app.ui.screens.home.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ask.app.R
import com.ask.app.data.models.User
import com.ask.app.data.models.Widget
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.app.ui.screens.WidgetWithUserView

@Preview(name = "phone", device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Preview(name = "pixel4", device = "id:pixel_4")
@Preview(name = "tablet", device = "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun DashBoardScreen(
    @PreviewParameter(DashBoardScreenPreviewParameterProvider::class) uiState: DashboardUiState,
    sizeClass: WindowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Zero),
    onOptionClick: (String, String) -> Unit = { _, _ -> },
    onFilterTypeChange: (FilterType) -> Unit = {},
) {
    var showMenu by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_filter_list_24),
                            contentDescription = stringResource(R.string.filter)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        FilterType.entries.forEach {
                            DropdownMenuItem(
                                text = { Text(text = it.name) },
                                onClick = {
                                    onFilterTypeChange(it)
                                    showMenu = false
                                },
                            )
                        }
                    }
                },
            )
        },
    ) {
        val widthClass = sizeClass.widthSizeClass
        val heightClass = sizeClass.heightSizeClass
        println("width>$widthClass, height>$heightClass")
        if (widthClass == WindowWidthSizeClass.Compact) {
            DashboardList(it, uiState, onOptionClick)
        } else {
            val columnCount =
                if (widthClass == WindowWidthSizeClass.Medium || heightClass == WindowHeightSizeClass.Medium) 2
                else if (widthClass == WindowWidthSizeClass.Expanded || heightClass == WindowHeightSizeClass.Expanded) 3
                else 2

            DashboardGrid(it, uiState, columnCount, onOptionClick)
        }
    }
}

@Composable
fun DashboardList(
    it: PaddingValues,
    uiState: DashboardUiState,
    onOptionClick: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(it)
    ) {
        items(uiState.widgets) { widget ->
            WidgetWithUserView(widget, onOptionClick)
        }
        item {
            Box(modifier = Modifier.size(100.dp))
        }
    }
}

@Composable
fun DashboardGrid(
    it: PaddingValues,
    uiState: DashboardUiState,
    columnCount: Int,
    onOptionClick: (String, String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount), modifier = Modifier
            .fillMaxSize()
            .padding(it)
    ) {
        items(uiState.widgets) { widget ->
            WidgetWithUserView(widget, onOptionClick)
        }
        item {
            Box(modifier = Modifier.size(100.dp))
        }
    }
}

class DashBoardScreenPreviewParameterProvider : PreviewParameterProvider<DashboardUiState> {
    override val values: Sequence<DashboardUiState>
        get() = sequenceOf(
            DashboardUiState(
                widgets = listOf(
                    WidgetWithOptionsAndVotesForTargetAudience(
                        widget = Widget(
                            title = "Find the answer?"
                        ),
                        options = listOf(
                            WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                                option = Widget.Option(text = "option1"),
                                votes = listOf()
                            ),
                            WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                                option = Widget.Option(text = "Option 2"),
                                votes = listOf()
                            ),
                        ),
                        targetAudienceAgeRange = Widget.TargetAudienceAgeRange(),
                        targetAudienceGender = Widget.TargetAudienceGender(),
                        targetAudienceLocations = listOf(),
                        user = User()
                    )
                )
            )
        )
}