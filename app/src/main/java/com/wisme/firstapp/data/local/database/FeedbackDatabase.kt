package com.wisme.firstapp.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.wisme.firstapp.data.local.dao.FeedbackQuestionDao
import com.wisme.firstapp.data.local.dao.FeedbackSubmissionDao
import com.wisme.firstapp.data.local.entities.FeedbackQuestionEntity
import com.wisme.firstapp.data.local.entities.FeedbackSubmissionEntity
import com.wisme.firstapp.data.local.entities.StringListConverter
import com.wisme.firstapp.data.local.entities.SafeFeedbackResponseListConverter

@Database(
    entities = [
        FeedbackQuestionEntity::class,
        FeedbackSubmissionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    SafeFeedbackResponseListConverter::class
)
abstract class FeedbackDatabase : RoomDatabase() {
    
    abstract fun feedbackQuestionDao(): FeedbackQuestionDao
    abstract fun feedbackSubmissionDao(): FeedbackSubmissionDao
    
    companion object {
        private const val DATABASE_NAME = "feedback_database"
        
        @Volatile
        private var INSTANCE: FeedbackDatabase? = null
        
        fun getInstance(context: Context): FeedbackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FeedbackDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development, in production add proper migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}