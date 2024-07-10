package com.ask.app.ui.screens.home.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ask.app.ui.screens.WidgetWithUserView

@Composable
fun MyWidgetsScreen(
    uiState: MyWidgetsUiState,
    onOptionClick: (String, String) -> Unit = { _, _ -> }
) {
    Column(modifier = Modifier.fillMaxSize()) {
        for (widget in uiState.widgets) {
            WidgetWithUserView(widget, onOptionClick)
        }
    }
}