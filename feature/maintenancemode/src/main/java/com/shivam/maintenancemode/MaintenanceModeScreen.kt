package com.shivam.maintenancemode

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp


@Composable
fun MaintenanceModeScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.maintenance_repair_service_svgrepo_com),
                contentDescription = stringResource(id = R.string.maintenance_mode)
            )
            Spacer(modifier = Modifier.size(20.dp))
            Text(
                text = stringResource(R.string.maintenance_mode),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = stringResource(R.string.maintenance_mode_desc),
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
            )
        }
    }
}

