package com.wisme.research.data.local.dao

import androidx.room.*
import com.wisme.research.data.local.entities.UserProgressEntity
import com.wisme.research.data.local.entities.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO for user progress operations
 */
@Dao
interface UserProgressDao {
    
    // Basic CRUD operations
    @Query("SELECT * FROM user_progress WHERE progressId = :progressId")
    suspend fun getProgress(progressId: String): UserProgressEntity?
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND journeyId = :journeyId AND episodeId = :episodeId")
    suspend fun getEpisodeProgress(userId: String, journeyId: String, episodeId: String): UserProgressEntity?
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND journeyId = :journeyId AND episodeId = :episodeId")
    fun getEpisodeProgressFlow(userId: String, journeyId: String, episodeId: String): Flow<UserProgressEntity?>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND journeyId = :journeyId ORDER BY lastAccessedAt DESC")
    suspend fun getJourneyProgress(userId: String, journeyId: String): List<UserProgressEntity>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND journeyId = :journeyId ORDER BY lastAccessedAt DESC")
    fun getJourneyProgressFlow(userId: String, journeyId: String): Flow<List<UserProgressEntity>>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId ORDER BY lastAccessedAt DESC")
    suspend fun getAllUserProgress(userId: String): List<UserProgressEntity>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId ORDER BY lastAccessedAt DESC")
    fun getAllUserProgressFlow(userId: String): Flow<List<UserProgressEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgressEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressList(progressList: List<UserProgressEntity>)
    
    @Update
    suspend fun updateProgress(progress: UserProgressEntity)
    
    @Delete
    suspend fun deleteProgress(progress: UserProgressEntity)
    
    @Query("DELETE FROM user_progress WHERE progressId = :progressId")
    suspend fun deleteProgressById(progressId: String)
    
    // Progress-specific queries
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND status = :status ORDER BY lastAccessedAt DESC")
    suspend fun getProgressByStatus(userId: String, status: String): List<UserProgressEntity>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND status = :status ORDER BY lastAccessedAt DESC")
    fun getProgressByStatusFlow(userId: String, status: String): Flow<List<UserProgressEntity>>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND status = 'completed' ORDER BY completedAt DESC")
    suspend fun getCompletedEpisodes(userId: String): List<UserProgressEntity>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND status = 'in_progress' ORDER BY lastAccessedAt DESC")
    suspend fun getInProgressEpisodes(userId: String): List<UserProgressEntity>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND status = 'in_progress' ORDER BY lastAccessedAt DESC")
    fun getInProgressEpisodesFlow(userId: String): Flow<List<UserProgressEntity>>
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM user_progress WHERE userId = :userId AND status = 'completed'")
    suspend fun getCompletedEpisodesCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM user_progress WHERE userId = :userId AND status = 'in_progress'")
    suspend fun getInProgressEpisodesCount(userId: String): Int
    
    @Query("SELECT COUNT(DISTINCT journeyId) FROM user_progress WHERE userId = :userId AND status = 'completed'")
    suspend fun getCompletedJourneysCount(userId: String): Int
    
    @Query("SELECT SUM(totalTimeSpentSeconds) FROM user_progress WHERE userId = :userId")
    suspend fun getTotalListeningTimeSeconds(userId: String): Long?
    
    @Query("""
        SELECT journeyId, COUNT(*) as episodeCount, 
               AVG(progressPercentage) as avgProgress,
               SUM(totalTimeSpentSeconds) as totalTime
        FROM user_progress 
        WHERE userId = :userId 
        GROUP BY journeyId
    """)
    suspend fun getJourneyStatistics(userId: String): List<JourneyStatistics>
    
    // Sync-related operations
    @Query("SELECT * FROM user_progress WHERE syncStatus = :status")
    suspend fun getProgressBySync(status: SyncStatus): List<UserProgressEntity>
    
    @Query("SELECT * FROM user_progress WHERE syncStatus = :status")
    fun getProgressBySyncFlow(status: SyncStatus): Flow<List<UserProgressEntity>>
    
    @Query("UPDATE user_progress SET syncStatus = :status, lastUpdated = :timestamp WHERE progressId = :progressId")
    suspend fun updateSyncStatus(progressId: String, status: SyncStatus, timestamp: Long)
    
    @Query("SELECT * FROM user_progress WHERE syncStatus IN (:statuses)")
    suspend fun getProgressNeedingSync(vararg statuses: SyncStatus = arrayOf(SyncStatus.PENDING, SyncStatus.FAILED)): List<UserProgressEntity>
    
    @Query("SELECT * FROM user_progress WHERE syncStatus IN (:statuses)")
    fun getProgressNeedingSyncFlow(vararg statuses: SyncStatus = arrayOf(SyncStatus.PENDING, SyncStatus.FAILED)): Flow<List<UserProgressEntity>>
    
    // Utility operations
    @Query("DELETE FROM user_progress WHERE userId = :userId")
    suspend fun deleteAllUserProgress(userId: String)
    
    @Query("DELETE FROM user_progress")
    suspend fun deleteAllProgress()
    
    @Query("SELECT COUNT(*) FROM user_progress WHERE syncStatus = :status")
    suspend fun getCountByStatus(status: SyncStatus): Int
}

/**
 * Data class for journey statistics
 */
data class JourneyStatistics(
    val journeyId: String,
    val episodeCount: Int,
    val avgProgress: Double,
    val totalTime: Long
)
