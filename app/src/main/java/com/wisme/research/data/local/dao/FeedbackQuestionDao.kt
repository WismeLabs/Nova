package com.wisme.research.data.local.dao

import androidx.room.*
import com.wisme.research.data.local.entities.FeedbackQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackQuestionDao {
    
    @Query("SELECT * FROM feedback_questions WHERE feedbackType = :feedbackType")
    suspend fun getQuestionsByType(feedbackType: String): List<FeedbackQuestionEntity>
    
    @Query("SELECT * FROM feedback_questions WHERE feedbackType = :feedbackType")
    fun getQuestionsByTypeFlow(feedbackType: String): Flow<List<FeedbackQuestionEntity>>
    
    @Query("SELECT * FROM feedback_questions")
    suspend fun getAllQuestions(): List<FeedbackQuestionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<FeedbackQuestionEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: FeedbackQuestionEntity)
    
    @Update
    suspend fun updateQuestion(question: FeedbackQuestionEntity)
    
    @Delete
    suspend fun deleteQuestion(question: FeedbackQuestionEntity)
    
    @Query("DELETE FROM feedback_questions WHERE feedbackType = :feedbackType")
    suspend fun deleteQuestionsByType(feedbackType: String)
    
    @Query("DELETE FROM feedback_questions")
    suspend fun deleteAllQuestions()
    
    @Query("SELECT COUNT(*) FROM feedback_questions WHERE feedbackType = :feedbackType")
    suspend fun getQuestionCountByType(feedbackType: String): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM feedback_questions WHERE feedbackType = :feedbackType)")
    suspend fun hasQuestionsForType(feedbackType: String): Boolean
}
