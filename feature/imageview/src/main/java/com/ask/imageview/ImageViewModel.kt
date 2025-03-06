package com.ask.imageview

import com.ask.analytics.AnalyticsLogger
import com.ask.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(private val analyticsLogger: AnalyticsLogger) :
    BaseViewModel(analyticsLogger) {

    fun onImageOpen(imageUrl: String) {
        analyticsLogger.imageOpenEvent(imageUrl)
    }
}