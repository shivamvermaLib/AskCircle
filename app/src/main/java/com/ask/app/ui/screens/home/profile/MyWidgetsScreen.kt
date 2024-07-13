package com.ask.app.ui.screens.home.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ask.app.R
import com.ask.app.ui.screens.WidgetWithUserView

@Composable
fun MyWidgetsScreen(
    modifier: Modifier = Modifier,
    uiState: MyWidgetsUiState,
    onOptionClick: (String, String) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.widgets.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_widgets),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        items(uiState.widgets) { widget ->
            WidgetWithUserView(widget, onOptionClick)
        }
        item {
            Spacer(modifier = Modifier.size(60.dp))
        }
    }
}