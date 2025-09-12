package com.wisme.nova

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Nova Application Class
 * 
 * Entry point for the Nova podcast application.
 * Configures Hilt dependency injection, Firebase, and notification channels.
 * 
 * @author Wisme Labs
 * @since 1.0.0
 */
@HiltAndroidApp
class App : Application() {

    companion object {
        const val MEDIA_NOTIFICATION_CHANNEL_ID = "media_playback_channel"
        const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"
        const val GENERAL_NOTIFICATION_CHANNEL_ID = "general_channel"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        initializeFirebase()
        
        // Initialize logging
        initializeLogging()
        
        // Create notification channels
        createNotificationChannels()
        
        // Initialize Crashlytics
        initializeCrashlytics()
    }

    /**
     * Initialize Firebase services
     */
    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            Timber.d("Firebase initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Firebase")
        }
    }

    /**
     * Initialize logging with Timber
     */
    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // Plant a custom tree for release builds
            Timber.plant(ReleaseTree())
        }
        Timber.d("Nova App started - Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
    }

    /**
     * Create notification channels for different types of notifications
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Media playback channel
            val mediaChannel = NotificationChannel(
                MEDIA_NOTIFICATION_CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for podcast playback"
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // Download channel
            val downloadChannel = NotificationChannel(
                DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Podcast download progress and completion"
                setShowBadge(true)
            }

            // General notifications channel
            val generalChannel = NotificationChannel(
                GENERAL_NOTIFICATION_CHANNEL_ID,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
                setShowBadge(true)
            }

            // Register channels
            notificationManager.createNotificationChannels(
                listOf(mediaChannel, downloadChannel, generalChannel)
            )
            
            Timber.d("Notification channels created")
        }
    }

    /**
     * Initialize Firebase Crashlytics
     */
    private fun initializeCrashlytics() {
        try {
            FirebaseCrashlytics.getInstance().apply {
                setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
                setCustomKey("app_version", BuildConfig.VERSION_NAME)
                setCustomKey("build_type", BuildConfig.BUILD_TYPE)
            }
            Timber.d("Crashlytics initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Crashlytics")
        }
    }

    /**
     * Custom Timber tree for release builds
     * Filters out debug and verbose logs in production
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == android.util.Log.VERBOSE || priority == android.util.Log.DEBUG) {
                return
            }

            // Log to Firebase Crashlytics for ERROR level
            if (priority == android.util.Log.ERROR) {
                FirebaseCrashlytics.getInstance().apply {
                    if (t != null) {
                        recordException(t)
                    } else {
                        log("$tag: $message")
                    }
                }
            }
        }
    }
}