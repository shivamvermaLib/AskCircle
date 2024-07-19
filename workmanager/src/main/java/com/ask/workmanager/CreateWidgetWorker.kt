package com.ask.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ask.common.getExtension
import com.ask.common.getResizedImageByteArray
import com.ask.common.preLoadImages
import com.ask.core.getAllImages
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
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            setProgress(workDataOf(STATUS to WorkerStatus.Loading.name))
            val widgetString = inputData.getString(WIDGET)
            widgetString?.let {
                Json.decodeFromString<WidgetWithOptionsAndVotesForTargetAudience>(it)
            }?.let { widgetWithOptionsAndVotesForTargetAudience ->
                createWidgetUseCase.invoke(widgetWithOptionsAndVotesForTargetAudience, {
                    applicationContext.getExtension(it)!!
                }, {
                    applicationContext.getResizedImageByteArray(it)
                }
                ).let { it ->
                    applicationContext.preLoadImages(it.options.map { it.option.imageUrl.getAllImages() }
                        .flatten())
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
            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
//                .setRequiresStorageNotLow(true)
                .build()
            val workRequest =
                OneTimeWorkRequestBuilder<CreateWidgetWorker>()
                    .setConstraints(constraints)
                    .setInputData(workData.build())
                    .addTag(CREATE_WIDGET)
                    .build()
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

