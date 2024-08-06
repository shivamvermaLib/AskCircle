package com.ask.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

open class BaseViewModel(private val analyticsLogger: AnalyticsLogger) : ViewModel() {
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
        FirebaseCrashlytics.getInstance().recordException(exception)
    }

    fun <T> safeApiCall(onInit: () -> Unit, api: suspend () -> T, onError: (String) -> Unit) {
        viewModelScope.launch(coroutineExceptionHandler) {
            onInit()
            try {
                api()
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
                onError(e.message ?: "Something went wrong")
            }
        }
    }

    fun screenOpenEvent(name: String?) {
        analyticsLogger.screenOpenEvent(name ?: "No Screen Name")
    }

}