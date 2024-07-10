package com.ask.app.ui.screens.home.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ask.app.ui.screens.WidgetWithUserView

@Composable
fun DashBoardScreen(
    uiState: DashboardUiState,
    onOptionClick: (String, String) -> Unit = { _, _ -> }
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(uiState.widgets) { widget ->
            WidgetWithUserView(widget, onOptionClick)
        }
        item {
            Box(modifier = Modifier.size(100.dp))
        }
    }
}
