package com.ask.app.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ask.app.analytics.AnalyticsLogger
import com.ask.app.domain.SyncUsersAndWidgetsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWidgetWorker @AssistedInject constructor(
    private val syncWidgetUseCase: SyncUsersAndWidgetsUseCase,
    private val analyticsLogger: AnalyticsLogger,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val time = System.currentTimeMillis()
            syncWidgetUseCase(false)
            val duration = System.currentTimeMillis() - time
            analyticsLogger.syncUsersAndWidgetsEventDuration(duration)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

}