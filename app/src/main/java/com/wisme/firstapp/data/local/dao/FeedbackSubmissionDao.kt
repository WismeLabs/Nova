package com.wisme.firstapp.data.local.dao

import androidx.room.*
import com.wisme.firstapp.data.local.entities.FeedbackSubmissionEntity
import com.wisme.firstapp.data.local.entities.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackSubmissionDao {
    
    @Query("SELECT * FROM feedback_submissions WHERE feedbackType = :feedbackType AND contextId = :contextId")
    suspend fun getSubmission(feedbackType: String, contextId: String): FeedbackSubmissionEntity?
    
    @Query("SELECT * FROM feedback_submissions WHERE feedbackType = :feedbackType AND contextId = :contextId")
    fun getSubmissionFlow(feedbackType: String, contextId: String): Flow<FeedbackSubmissionEntity?>
    
    @Query("SELECT * FROM feedback_submissions WHERE feedbackType = :feedbackType")
    suspend fun getSubmissionsByType(feedbackType: String): List<FeedbackSubmissionEntity>
    
    @Query("SELECT * FROM feedback_submissions WHERE feedbackType = :feedbackType")
    fun getSubmissionsByTypeFlow(feedbackType: String): Flow<List<FeedbackSubmissionEntity>>
    
    @Query("SELECT * FROM feedback_submissions WHERE userId = :userId")
    suspend fun getSubmissionsByUser(userId: String): List<FeedbackSubmissionEntity>
    
    @Query("SELECT * FROM feedback_submissions WHERE userId = :userId")
    fun getSubmissionsByUserFlow(userId: String): Flow<List<FeedbackSubmissionEntity>>
    
    @Query("SELECT * FROM feedback_submissions")
    suspend fun getAllSubmissions(): List<FeedbackSubmissionEntity>
    
    @Query("SELECT * FROM feedback_submissions")
    fun getAllSubmissionsFlow(): Flow<List<FeedbackSubmissionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: FeedbackSubmissionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(submissions: List<FeedbackSubmissionEntity>)
    
    @Update
    suspend fun updateSubmission(submission: FeedbackSubmissionEntity)
    
    @Delete
    suspend fun deleteSubmission(submission: FeedbackSubmissionEntity)
    
    @Query("DELETE FROM feedback_submissions WHERE submissionId = :submissionId")
    suspend fun deleteSubmissionById(submissionId: String)
    
    @Query("DELETE FROM feedback_submissions")
    suspend fun deleteAllSubmissions()
    
    // Sync-related queries
    @Query("SELECT * FROM feedback_submissions WHERE syncStatus = :status")
    suspend fun getSubmissionsByStatus(status: SyncStatus): List<FeedbackSubmissionEntity>
    
    @Query("SELECT * FROM feedback_submissions WHERE syncStatus = :status")
    fun getSubmissionsByStatusFlow(status: SyncStatus): Flow<List<FeedbackSubmissionEntity>>
    
    @Query("SELECT * FROM feedback_submissions WHERE syncStatus IN (:statuses)")
    suspend fun getSubmissionsByStatuses(statuses: List<SyncStatus>): List<FeedbackSubmissionEntity>
    
    @Query("UPDATE feedback_submissions SET syncStatus = :status WHERE submissionId = :submissionId")
    suspend fun updateSyncStatus(submissionId: String, status: SyncStatus)
    
    @Query("UPDATE feedback_submissions SET syncStatus = :status, lastModified = :timestamp WHERE submissionId = :submissionId")
    suspend fun updateSyncStatusWithTimestamp(submissionId: String, status: SyncStatus, timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM feedback_submissions WHERE syncStatus = :status")
    suspend fun getCountByStatus(status: SyncStatus): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM feedback_submissions WHERE feedbackType = :feedbackType AND contextId = :contextId)")
    suspend fun hasSubmission(feedbackType: String, contextId: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM feedback_submissions WHERE syncStatus = :status)")
    suspend fun hasSubmissionsWithStatus(status: SyncStatus): Boolean
    
    // Get submissions that need syncing (PENDING or FAILED)
    @Query("SELECT * FROM feedback_submissions WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY lastModified ASC")
    suspend fun getSubmissionsNeedingSync(): List<FeedbackSubmissionEntity>
    
    @Query("SELECT * FROM feedback_submissions WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY lastModified ASC")
    fun getSubmissionsNeedingSyncFlow(): Flow<List<FeedbackSubmissionEntity>>
}