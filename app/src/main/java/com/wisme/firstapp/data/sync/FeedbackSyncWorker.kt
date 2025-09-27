package com.wisme.firstapp.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wisme.firstapp.data.repository.FeedbackRepository
import com.wisme.firstapp.data.local.dao.FeedbackSubmissionDao
import com.wisme.firstapp.data.local.entities.SyncStatus
import com.wisme.firstapp.data.network.NetworkConnectivityManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker to sync pending feedback submissions when network is available
 */
@HiltWorker
class FeedbackSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val feedbackRepository: FeedbackRepository,
    private val feedbackSubmissionDao: FeedbackSubmissionDao,
    private val networkConnectivityManager: NetworkConnectivityManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "FeedbackSyncWorker"
        const val WORK_NAME = "feedback_sync_work"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting feedback sync work")
            
            // Check if we have network connectivity
            if (!networkConnectivityManager.isCurrentlyOnline()) {
                Log.d(TAG, "No network connectivity, skipping sync")
                return Result.retry()
            }

            // Get all submissions that need to be synced
            val pendingSubmissions = feedbackSubmissionDao.getSubmissionsNeedingSync()
            
            if (pendingSubmissions.isEmpty()) {
                Log.d(TAG, "No pending submissions to sync")
                return Result.success()
            }

            Log.d(TAG, "Found ${pendingSubmissions.size} submissions to sync")

            var successCount = 0
            var failCount = 0

            // Sync each pending submission
            for (submission in pendingSubmissions) {
                try {
                    // Convert to API format and submit
                    val responses = submission.responses.map { local ->
                        com.wisme.firstapp.data.repository.FeedbackResponse(
                            question_id = local.questionId,
                            response_value = local.responseValue
                        )
                    }
                    
                    val result = when(submission.feedbackType) {
                        "episode" -> {
                            if (responses.size >= 2) {
                                feedbackRepository.submitEpisodeFeedback(
                                    episodeId = submission.contextId,
                                    enjoyment = responses.find { response -> response.question_id.contains("enjoyment") }?.response_value ?: "",
                                    clarity = responses.find { response -> response.question_id.contains("clarity") }?.response_value ?: ""
                                )
                            } else null
                        }
                        "journey" -> {
                            if (responses.size >= 3) {
                                feedbackRepository.submitJourneyFeedback(
                                    journeyId = submission.contextId,
                                    moocsComparison = responses.find { response -> response.question_id.contains("moocs") }?.response_value ?: "",
                                    youtubeComparison = responses.find { response -> response.question_id.contains("youtube") }?.response_value ?: "",
                                    blogsComparison = responses.find { response -> response.question_id.contains("blogs") }?.response_value ?: ""
                                )
                            } else null
                        }
                        "general" -> {
                            if (responses.size >= 3) {
                                feedbackRepository.submitGeneralFeedback(
                                    wouldRevisit = responses.find { response -> response.question_id.contains("revisit") }?.response_value ?: "",
                                    wouldRecommend = responses.find { response -> response.question_id.contains("recommend") }?.response_value ?: "",
                                    willingnessToPay = responses.find { response -> response.question_id.contains("pay") }?.response_value ?: ""
                                )
                            } else null
                        }
                        else -> null
                    }

                    result?.let { syncResult ->
                        when (syncResult) {
                            is com.wisme.firstapp.data.repository.FeedbackResult.Success -> {
                                // Update sync status to SYNCED
                                feedbackSubmissionDao.updateSyncStatusWithTimestamp(
                                    submission.submissionId,
                                    SyncStatus.SYNCED,
                                    System.currentTimeMillis()
                                )
                                successCount++
                                Log.d(TAG, "Successfully synced submission: ${submission.submissionId}")
                            }
                            is com.wisme.firstapp.data.repository.FeedbackResult.Error -> {
                                // Update sync status to FAILED
                                feedbackSubmissionDao.updateSyncStatus(
                                    submission.submissionId,
                                    SyncStatus.FAILED
                                )
                                failCount++
                                Log.e(TAG, "Failed to sync submission: ${submission.submissionId} - ${syncResult.message}")
                            }
                            else -> {
                                failCount++
                                Log.w(TAG, "Unexpected result for submission: ${submission.submissionId}")
                            }
                        }
                    } ?: run {
                        failCount++
                        Log.e(TAG, "Could not determine sync method for submission: ${submission.submissionId}")
                    }
                    
                } catch (e: Exception) {
                    // Update sync status to FAILED
                    feedbackSubmissionDao.updateSyncStatus(
                        submission.submissionId,
                        SyncStatus.FAILED
                    )
                    failCount++
                    Log.e(TAG, "Exception syncing submission: ${submission.submissionId}", e)
                }
            }

            Log.d(TAG, "Sync completed: $successCount successful, $failCount failed")

            // Return success if at least some submissions were synced
            if (successCount > 0) {
                Result.success()
            } else if (failCount > 0) {
                // Retry if all failed (might be temporary network issue)
                Result.retry()
            } else {
                Result.success()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in sync worker", e)
            Result.failure()
        }
    }
}