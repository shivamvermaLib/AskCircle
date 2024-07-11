package com.ask.app.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ask.app.STATUS
import com.ask.app.WIDGET
import com.ask.app.analytics.AnalyticsLogger
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.app.domain.CreateWidgetUseCase
import com.ask.app.ui.screens.utils.getByteArray
import com.ask.app.ui.screens.utils.getExtension
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.json.Json

@HiltWorker
class CreateWidgetWorker @AssistedInject constructor(
    private val createWidgetUseCase: CreateWidgetUseCase,
    private val analyticsLogger: AnalyticsLogger,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    private fun createWidgetEvent(w: WidgetWithOptionsAndVotesForTargetAudience) {
        analyticsLogger.createWidgetEvent(
            w.widget.widgetType,
            w.widget.description.isNullOrBlank().not(),
            w.options.size,
            w.options.all { it.option.imageUrl != null },
            w.targetAudienceGender.gender,
            w.targetAudienceLocations.mapNotNull { it.country },
            w.targetAudienceAgeRange.min,
            w.targetAudienceAgeRange.max
        )
    }

    private fun createdWidgetEvent(w: WidgetWithOptionsAndVotesForTargetAudience) {
        analyticsLogger.createdWidgetEvent(
            w.widget.id,
            w.widget.widgetType,
            w.widget.description.isNullOrBlank().not(),
            w.options.size,
            w.options.all { it.option.imageUrl != null },
            w.targetAudienceGender.gender,
            w.targetAudienceLocations.mapNotNull { it.country },
            w.targetAudienceAgeRange.min,
            w.targetAudienceAgeRange.max
        )
    }

    override suspend fun doWork(): Result {
        return try {
            println("status:progress")
            setProgress(workDataOf(STATUS to WorkerStatus.Loading.name))
            val widgetString = inputData.getString(WIDGET)
            widgetString?.let { Json.decodeFromString<WidgetWithOptionsAndVotesForTargetAudience>(it) }
                ?.let { widgetWithOptionsAndVotesForTargetAudience ->
                    createWidgetEvent(widgetWithOptionsAndVotesForTargetAudience)
                    createWidgetUseCase(widgetWithOptionsAndVotesForTargetAudience, {
                        applicationContext.getExtension(it)!!
                    }, {
                        applicationContext.getByteArray(it)!!
                    }
                    ).let {
                        createdWidgetEvent(it)
                    }
                }
            println("status:success")
            setProgress(workDataOf(STATUS to WorkerStatus.Success.name))
            Result.success(workDataOf(STATUS to WorkerStatus.Success.name))
        } catch (e: Exception) {
            println("status:failed")
            setProgress(workDataOf(STATUS to WorkerStatus.Failed.name))
            e.printStackTrace()
            Result.failure()
        }
    }
}

enum class WorkerStatus {
    None, Loading, Success, Failed
}