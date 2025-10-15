package com.wisme.firstapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room entity for storing journey information locally
 */
@Entity(
    tableName = "journeys",
    indices = [Index(value = ["journeyId"], unique = true)]
)
data class JourneyEntity(
    @PrimaryKey
    val journeyId: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val totalDurationMinutes: Int,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1
)
