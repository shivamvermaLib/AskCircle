package com.ask.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.ask.analytics.AnalyticsLogger
import com.ask.common.ConnectionState
import com.ask.common.observeConnectivityAsFlow
import com.ask.workmanager.NOTIFICATION_CHANNEL_ID
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AskApplication : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var remoteConfigRepository: com.ask.core.RemoteConfigRepository

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private val scope =
        CoroutineScope(Dispatchers.Main + CoroutineExceptionHandler { _, throwable ->
            FirebaseCrashlytics.getInstance().recordException(throwable)
        })

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        adMob()
        createNotificationChannel()
        scope.launch {
            launch {
                applicationContext.observeConnectivityAsFlow().collect { connectionState ->
                    if (connectionState == ConnectionState.Available) {
                        remoteConfigRepository.fetchInit().also {
                            analyticsLogger.remoteConfigFetchEvent(it)
                        }
                    }
                }
            }
        }
    }

    private fun adMob() {
        MobileAds.initialize(this) {}
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Channel"
            val descriptionText = "Channel for new widget notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(30 * 1024 * 1024)
                    .build()
            }
//            .logger(DebugLogger())
            .respectCacheHeaders(false)
            .build()
    }
}