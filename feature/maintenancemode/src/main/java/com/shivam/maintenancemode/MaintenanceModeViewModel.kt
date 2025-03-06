package com.shivam.maintenancemode

import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import com.ask.common.CheckMaintenanceModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MaintenanceModeViewModel @Inject constructor(
    analyticsLogger: AnalyticsLogger,
    maintenanceModeUseCase: CheckMaintenanceModeUseCase
) : BaseViewModel(analyticsLogger) {

    val maintenanceModeFlow =
        maintenanceModeUseCase().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            false
        )

}