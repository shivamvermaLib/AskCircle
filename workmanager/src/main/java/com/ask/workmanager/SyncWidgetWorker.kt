package com.ask.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ask.widget.SyncUsersAndWidgetsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWidgetWorker @AssistedInject constructor(
    private val syncWidgetUseCase: SyncUsersAndWidgetsUseCase,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            setProgress(workDataOf(STATUS to WorkerStatus.Loading.name))
            syncWidgetUseCase.invoke()
            setProgress(workDataOf(STATUS to WorkerStatus.Success.name))
            Result.success()
        } catch (e: Exception) {
            setProgress(workDataOf(STATUS to WorkerStatus.Failed.name))
            e.printStackTrace()
            Result.failure(Data(workDataOf(ERROR to e.message)))
        }
    }

    companion object {
        fun sendRequest(context: Context, syncTimeInMinutes: Long) {
            val workManager = WorkManager.getInstance(context)
            val myWork = PeriodicWorkRequestBuilder<SyncWidgetWorker>(
                syncTimeInMinutes,
                TimeUnit.MINUTES
            ).build()
            workManager.enqueueUniquePeriodicWork(
                SYNC_WIDGET_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                myWork
            )
        }

        fun getWorkerFlow(context: Context): Flow<List<WorkInfo>> {
            val workManager = WorkManager.getInstance(context)
            return workManager.getWorkInfosByTagFlow(SYNC_WIDGET_WORK)
        }

    }

}