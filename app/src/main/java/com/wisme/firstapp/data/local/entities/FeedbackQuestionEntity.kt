package com.wisme.firstapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.wisme.firstapp.data.repository.FeedbackQuestion
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Entity(tableName = "feedback_questions")
@TypeConverters(StringListConverter::class)
data class FeedbackQuestionEntity(
    @PrimaryKey
    val questionId: String,
    val feedbackType: String,
    val questionText: String,
    val responseType: String,
    val options: List<String>? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Convert FeedbackQuestion (API model) to FeedbackQuestionEntity (local storage)
 */
fun FeedbackQuestion.toEntity(feedbackType: String): FeedbackQuestionEntity {
    return FeedbackQuestionEntity(
        questionId = this.question_id,
        feedbackType = feedbackType,
        questionText = this.question_text,
        responseType = this.response_type,
        options = this.options,
        createdAt = System.currentTimeMillis()
    )
}

/**
 * Convert FeedbackQuestionEntity (local storage) to FeedbackQuestion (API model)
 */
fun FeedbackQuestionEntity.toApiModel(): FeedbackQuestion {
    return FeedbackQuestion(
        question_id = this.questionId,
        question_text = this.questionText,
        response_type = this.responseType,
        options = this.options
    )
}

/**
 * Type converter for storing List<String> in Room
 */
class StringListConverter {
    private val json = Json
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { json.decodeFromString(it) }
    }
}
