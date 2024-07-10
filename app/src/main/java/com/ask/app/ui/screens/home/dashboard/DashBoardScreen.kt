package com.ask.app.ui.screens.home.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.ask.app.R
import com.ask.app.ui.screens.WidgetWithUserView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashBoardScreen(
    uiState: DashboardUiState,
    onOptionClick: (String, String) -> Unit = { _, _ -> },
    onFilterTypeChange: (FilterType) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Ask Circle")
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

}
