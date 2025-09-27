package com.wisme.firstapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room entity for storing user profile data locally
 */
@Entity(
    tableName = "user_profiles",
    indices = [Index(value = ["firebaseUid"], unique = true)]
)
data class UserProfileEntity(
    @PrimaryKey
    val firebaseUid: String,
    val avatarId: Int? = null,
    val name: String? = null,
    val displayName: String? = null,
    val dateOfBirth: String? = null, // ISO date format
    val gender: String? = null,
    val profession: String? = null,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1
)