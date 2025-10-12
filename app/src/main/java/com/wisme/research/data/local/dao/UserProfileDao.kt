package com.wisme.research.data.local.dao

import androidx.room.*
import com.wisme.research.data.local.entities.UserProfileEntity
import com.wisme.research.data.local.entities.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO for user profile operations
 */
@Dao
interface UserProfileDao {
    
    // Basic CRUD operations
    @Query("SELECT * FROM user_profiles WHERE firebaseUid = :firebaseUid")
    suspend fun getProfile(firebaseUid: String): UserProfileEntity?
    
    @Query("SELECT * FROM user_profiles WHERE firebaseUid = :firebaseUid")
    fun getProfileFlow(firebaseUid: String): Flow<UserProfileEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)
    
    @Update
    suspend fun updateProfile(profile: UserProfileEntity)
    
    @Delete
    suspend fun deleteProfile(profile: UserProfileEntity)
    
    @Query("DELETE FROM user_profiles WHERE firebaseUid = :firebaseUid")
    suspend fun deleteProfileById(firebaseUid: String)
    
    // Sync-related operations
    @Query("SELECT * FROM user_profiles WHERE syncStatus = :status")
    suspend fun getProfilesByStatus(status: SyncStatus): List<UserProfileEntity>
    
    @Query("SELECT * FROM user_profiles WHERE syncStatus = :status")
    fun getProfilesByStatusFlow(status: SyncStatus): Flow<List<UserProfileEntity>>
    
    @Query("UPDATE user_profiles SET syncStatus = :status, lastUpdated = :timestamp WHERE firebaseUid = :firebaseUid")
    suspend fun updateSyncStatus(firebaseUid: String, status: SyncStatus, timestamp: Long)
    
    @Query("SELECT * FROM user_profiles WHERE syncStatus IN (:statuses)")
    suspend fun getProfilesNeedingSync(vararg statuses: SyncStatus = arrayOf(SyncStatus.PENDING, SyncStatus.FAILED)): List<UserProfileEntity>
    
    @Query("SELECT * FROM user_profiles WHERE syncStatus IN (:statuses)")
    fun getProfilesNeedingSyncFlow(vararg statuses: SyncStatus = arrayOf(SyncStatus.PENDING, SyncStatus.FAILED)): Flow<List<UserProfileEntity>>
    
    // Utility operations
    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getProfileCount(): Int
    
    @Query("SELECT COUNT(*) FROM user_profiles WHERE syncStatus = :status")
    suspend fun getCountByStatus(status: SyncStatus): Int
    
    @Query("DELETE FROM user_profiles")
    suspend fun deleteAllProfiles()
}
