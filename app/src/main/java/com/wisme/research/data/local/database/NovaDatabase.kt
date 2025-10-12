package com.wisme.research.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.wisme.research.data.local.dao.*
import com.wisme.research.data.local.entities.*

/**
 * Main Room database for Nova app - comprehensive offline storage
 */
@Database(
    entities = [
        // Feedback system
        FeedbackQuestionEntity::class,
        FeedbackSubmissionEntity::class,
        
        // User management
        UserProfileEntity::class,
        UserSessionEntity::class,
        UserProgressEntity::class,
        
        // Journey & Episode content
        JourneyEntity::class,
        EpisodeEntity::class
    ],
    version = 2, // Incremented for new entities
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    SafeFeedbackResponseListConverter::class
)
abstract class NovaDatabase : RoomDatabase() {
    
    // Feedback system DAOs
    abstract fun feedbackQuestionDao(): FeedbackQuestionDao
    abstract fun feedbackSubmissionDao(): FeedbackSubmissionDao
    
    // User management DAOs
    abstract fun userProfileDao(): UserProfileDao
    abstract fun userProgressDao(): UserProgressDao
    
    // Journey & Episode DAOs (to be created)
    // abstract fun journeyDao(): JourneyDao
    // abstract fun episodeDao(): EpisodeDao
    // abstract fun userSessionDao(): UserSessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null
        
        fun getInstance(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova_database"
                )
                .fallbackToDestructiveMigration() // For development - remove for production
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
