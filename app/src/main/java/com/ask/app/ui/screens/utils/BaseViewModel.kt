package com.ask.app.ui.screens.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ask.app.analytics.AnalyticsLogger
import kotlinx.coroutines.launch

open class BaseViewModel(private val analyticsLogger: AnalyticsLogger) : ViewModel() {
    fun <T> safeApiCall(onInit: () -> Unit, api: suspend () -> T, onError: (String) -> Unit) {
        viewModelScope.launch {
            onInit()
            try {
                api()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Something went wrong")
            }
        }
    }

    fun screenOpenEvent(name: String?) {
        analyticsLogger.screenOpenEvent(name ?: "No Screen Name")
    }

}