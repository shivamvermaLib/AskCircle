package com.ask.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ask.analytics.AnalyticsLogger
import com.ask.common.getByteArray
import com.ask.common.getExtension
import com.ask.widget.CreateWidgetUseCase
import com.ask.widget.WIDGET
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
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
            setProgress(workDataOf(STATUS to WorkerStatus.Loading.name))
            val widgetString = inputData.getString(WIDGET)
            widgetString?.let {
                Json.decodeFromString<WidgetWithOptionsAndVotesForTargetAudience>(
                    it
                )
            }
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
            setProgress(workDataOf(STATUS to WorkerStatus.Success.name))
            Result.success(workDataOf(STATUS to WorkerStatus.Success.name))
        } catch (e: Exception) {
            setProgress(workDataOf(STATUS to WorkerStatus.Failed.name))
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        fun sendRequest(
            context: Context,
            widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience
        ) {
            val workManager = WorkManager.getInstance(context)
            val workData = Data.Builder().apply {
                putString(
                    WIDGET,
                    Json.encodeToString(widgetWithOptionsAndVotesForTargetAudience)
                )
            }
            val workRequest =
                OneTimeWorkRequestBuilder<CreateWidgetWorker>().setInputData(
                    workData.build()
                ).addTag(CREATE_WIDGET).build()
            workManager.enqueueUniqueWork(
                CREATE_WIDGET, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest
            )
        }

        fun getWorkerFlow(context: Context): Flow<List<WorkInfo>> {
            val workManager = WorkManager.getInstance(context)
            return workManager.getWorkInfosByTagFlow(CREATE_WIDGET)
        }
    }
}

