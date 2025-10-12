package com.wisme.research.data.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * Manages background sync of feedback submissions using WorkManager
 */
@Singleton
class FeedbackSyncManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "FeedbackSyncManager"
        private const val SYNC_WORK_NAME = "feedback_sync_work"
        private const val PERIODIC_SYNC_WORK_NAME = "feedback_periodic_sync_work"
    }

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule immediate sync when network becomes available
     */
    fun scheduleImmediateSync() {
        Log.d(TAG, "Scheduling immediate feedback sync")
        
        val syncRequest = OneTimeWorkRequestBuilder<FeedbackSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Schedule periodic sync every 4 hours when connected
     */
    fun schedulePeriodicSync() {
        Log.d(TAG, "Scheduling periodic feedback sync")
        
        val periodicSyncRequest = PeriodicWorkRequestBuilder<FeedbackSyncWorker>(
            4, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }

    /**
     * Cancel all sync work
     */
    fun cancelAllSync() {
        Log.d(TAG, "Cancelling all feedback sync work")
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
    }

    /**
     * Check if sync is currently running
     */
    fun isSyncRunning(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME)
        return try {
            workInfos.get().any { it.state == WorkInfo.State.RUNNING }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get sync status
     */
    suspend fun getSyncStatus(): SyncStatus {
        return try {
            val workInfos = workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME).get()
            val latestWork = workInfos.firstOrNull()
            
            when (latestWork?.state) {
                WorkInfo.State.RUNNING -> SyncStatus.SYNCING
                WorkInfo.State.SUCCEEDED -> SyncStatus.COMPLETED
                WorkInfo.State.FAILED -> SyncStatus.FAILED
                WorkInfo.State.ENQUEUED -> SyncStatus.PENDING
                WorkInfo.State.BLOCKED -> SyncStatus.WAITING
                WorkInfo.State.CANCELLED -> SyncStatus.CANCELLED
                else -> SyncStatus.IDLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sync status", e)
            SyncStatus.IDLE
        }
    }

    enum class SyncStatus {
        IDLE,       // No sync scheduled
        PENDING,    // Sync scheduled but not started
        WAITING,    // Waiting for network/constraints
        SYNCING,    // Currently syncing
        COMPLETED,  // Last sync completed successfully
        FAILED,     // Last sync failed
        CANCELLED   // Sync was cancelled
    }
}
