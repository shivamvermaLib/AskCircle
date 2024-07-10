package com.ask.app.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ask.app.STATUS
import com.ask.app.WIDGET
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
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            println("status:progress")
            setProgress(workDataOf(STATUS to WorkerStatus.Loading.name))
            val widgetString = inputData.getString(WIDGET)
            widgetString?.let { Json.decodeFromString<WidgetWithOptionsAndVotesForTargetAudience>(it) }
                ?.let { widgetWithOptionsAndVotesForTargetAudience ->
                    createWidgetUseCase(widgetWithOptionsAndVotesForTargetAudience, {
                        applicationContext.getExtension(it)!!
                    }, {
                        applicationContext.getByteArray(it)!!
                    }
                    )
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