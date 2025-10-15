# Ba## 📊 FEEDBACK SYSTEM STRUCTURES

We have **3 types of feedback** in our app:

### 📺 **1. EPISODE FEEDBACK** (`feedback_type: "episode"`)
- **Triggered**: After completing an episode  
- **Context**: Specific episode ID (e.g., `"episode_1"`)
- **Questions**: 2 questions about that specific episode
- **Purpose**: Rate individual episode experience

### 🎯 **2. JOURNEY FEEDBACK** (`feedback_type: "journey"`)
- **Triggered**: After completing a full journey
- **Context**: Specific journey ID (e.g., `"journey_dsa"`)  
- **Questions**: 3 comparison questions (vs other learning platforms)
- **Purpose**: Compare Wisme journeys to alternatives

### 🌟 **3. GENERAL FEEDBACK** (`feedback_type: "general"`)
- **Triggered**: Periodically or on app completion
- **Context**: Always `"general"` (no specific context)
- **Questions**: 3 questions about overall platform experience
- **Purpose**: Overall satisfaction and business insights

---

### Database Table Structure
**Endpoint**: `GET /api/v1/feedback/questions/{feedback_type}`

**Database Table**: `feedback_questions`
```sql
CREATE TABLE feedback_questions (
    question_id VARCHAR(50) PRIMARY KEY,
    feedback_type VARCHAR(50) NOT NULL, -- 'episode', 'journey', 'general'
    question_text TEXT NOT NULL,
    response_type VARCHAR(20) NOT NULL, -- 'multiple_choice', 'number'
    options JSON NULL, -- Array of strings for multiple choice questions
    order_index INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Required Database Seed Data
```sql
-- Episode Feedback Questions
INSERT INTO feedback_questions (question_id, feedback_type, question_text, response_type, options, order_index) VALUES
('episode_enjoyment', 'episode', 'Did you enjoy this episode?', 'multiple_choice', '["Yes", "Somewhat", "No"]', 1),
('episode_clarity', 'episode', 'Was this episode clear and easy to follow?', 'multiple_choice', '["Very clear", "Somewhat clear", "Confusing"]', 2);

-- Journey Feedback Questions  
INSERT INTO feedback_questions (question_id, feedback_type, question_text, response_type, options, order_index) VALUES
('journey_moocs_comparison', 'journey', 'Compared to MOOCs (like Coursera, Udemy), how would you rate this journey?', 'multiple_choice', '["Much better", "Slightly better", "About the same", "Worse"]', 1),
('journey_youtube_comparison', 'journey', 'Compared to YouTube, how would you rate this journey?', 'multiple_choice', '["Much better", "Slightly better", "About the same", "Worse"]', 2),
('journey_blogs_comparison', 'journey', 'Compared to blogs/articles, how would you rate this journey?', 'multiple_choice', '["Much better", "Slightly better", "About the same", "Worse"]', 3);

-- General Feedback Questions
INSERT INTO feedback_questions (question_id, feedback_type, question_text, response_type, options, order_index) VALUES
('general_would_revisit', 'general', 'Would you want to revisit any journey to revise?', 'multiple_choice', '["Yes", "Maybe", "No"]', 1),
('general_would_recommend', 'general', 'Would you recommend Wisme to a friend?', 'multiple_choice', '["Yes", "Maybe", "No"]', 2),
('general_willingness_to_pay', 'general', 'If Wisme offered a fully personalized journey for any topic you want, how much would you pay?', 'number', null, 3);
```ctures for Frontend-Backend Sync

This document provides the exact database structures needed for perfect synchronization between the frontend and backend APIs.

## 📊 FEEDBACK SYSTEM STRUCTURES

### 1. Feedback Questions Structure
**Endpoint**: `GET /api/v1/feedback/questions/{feedback_type}`

**Database Table**: `feedback_questions`
```sql
CREATE TABLE feedback_questions (
    question_id VARCHAR(50) PRIMARY KEY,
    feedback_type VARCHAR(50) NOT NULL, -- 'episode', 'journey', 'general'
    question_text TEXT NOT NULL,
    response_type VARCHAR(20) NOT NULL, -- 'rating', 'multiple_choice', 'text', 'number'
    options JSON NULL, -- Array of strings for multiple choice questions
    order_index INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**JSON Response Format for Episode Feedback** (`feedback_type: "episode"`):
```json
[
    {
        "question_id": "episode_enjoyment",
        "question_text": "Did you enjoy this episode?",
        "response_type": "multiple_choice",
        "options": ["Yes", "Somewhat", "No"]
    },
    {
        "question_id": "episode_clarity",
        "question_text": "Was this episode clear and easy to follow?",
        "response_type": "multiple_choice",
        "options": ["Very clear", "Somewhat clear", "Confusing"]
    }
]
```

**JSON Response Format for Journey Feedback** (`feedback_type: "journey"`):
```json
[
    {
        "question_id": "journey_moocs_comparison",
        "question_text": "How does this journey compare to MOOCs (Coursera, edX, etc.)?",
        "response_type": "multiple_choice",
        "options": ["Much better", "Slightly better", "About the same", "Worse"]
    },
    {
        "question_id": "journey_youtube_comparison",
        "question_text": "How does this journey compare to YouTube tutorials?",
        "response_type": "multiple_choice",
        "options": ["Much better", "Slightly better", "About the same", "Worse"]
    },
    {
        "question_id": "journey_blogs_comparison",
        "question_text": "How does this journey compare to reading blogs/articles?",
        "response_type": "multiple_choice",
        "options": ["Much better", "Slightly better", "About the same", "Worse"]
    }
]
```

**JSON Response Format for General Feedback** (`feedback_type: "general"`):
```json
[
    {
        "question_id": "general_would_revisit",
        "question_text": "Would you revisit Wisme for future learning?",
        "response_type": "multiple_choice",
        "options": ["Yes", "Maybe", "No"]
    },
    {
        "question_id": "general_would_recommend",
        "question_text": "Would you recommend Wisme to a friend?",
        "response_type": "multiple_choice",
        "options": ["Yes", "Maybe", "No"]
    },
    {
        "question_id": "general_willingness_to_pay",
        "question_text": "If Wisme offered a fully personalized journey for any topic you want, how much would you pay?",
        "response_type": "number",
        "options": null
    }
]
```

### 2. Feedback Submissions Structure
**Endpoint**: `POST /api/v1/feedback/submit`

**Database Table**: `feedback_submissions`
```sql
CREATE TABLE feedback_submissions (
    submission_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    feedback_type VARCHAR(50) NOT NULL, -- 'episode', 'journey', 'general'
    context_id VARCHAR(100) NOT NULL, -- episode_id, journey_id, or 'general'
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_feedback (user_id, feedback_type),
    INDEX idx_context (feedback_type, context_id)
);
```

**Database Table**: `feedback_responses`
```sql
CREATE TABLE feedback_responses (
    response_id VARCHAR(50) PRIMARY KEY,
    submission_id VARCHAR(50) NOT NULL,
    question_id VARCHAR(50) NOT NULL,
    response_value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES feedback_submissions(submission_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES feedback_questions(question_id),
    INDEX idx_submission (submission_id)
);
```

**Request JSON Format Examples**:

**Episode Feedback Submission**:
```json
{
    "feedback_type": "episode",
    "context_id": "episode_1",
    "responses": [
        {
            "question_id": "episode_enjoyment",
            "response_value": "Yes"
        },
        {
            "question_id": "episode_clarity",
            "response_value": "Very clear"
        }
    ]
}
```

**Journey Feedback Submission**:
```json
{
    "feedback_type": "journey",
    "context_id": "journey_dsa",
    "responses": [
        {
            "question_id": "journey_moocs_comparison",
            "response_value": "Much better"
        },
        {
            "question_id": "journey_youtube_comparison",
            "response_value": "Slightly better"
        },
        {
            "question_id": "journey_blogs_comparison",
            "response_value": "Much better"
        }
    ]
}
```

**General Feedback Submission**:
```json
{
    "feedback_type": "general",
    "context_id": "general",
    "responses": [
        {
            "question_id": "general_would_revisit",
            "response_value": "Yes"
        },
        {
            "question_id": "general_would_recommend",
            "response_value": "Maybe"
        },
        {
            "question_id": "general_willingness_to_pay",
            "response_value": "299"
        }
    ]
}
```

**Response JSON Format**:
```json
{
    "submission_id": "sub_ep1_user123_20250927_143022",
    "feedback_type": "episode",
    "context_id": "episode_1",
    "user_id": "user123",
    "responses": [
        {
            "question_id": "episode_enjoyment",
            "response_value": "Yes"
        },
        {
            "question_id": "episode_clarity",
            "response_value": "Very clear"
        }
    ],
    "submitted_at": "2025-09-27T14:30:22Z"
}
```

### 3. Feedback Analytics Structure
**Endpoint**: `GET /api/v1/feedback/analytics/{feedback_type}?context_id={context_id}`

**Response JSON Format**:
```json
{
    "success": true,
    "feedback_type": "episode",
    "context_id": "episode_1",
    "analytics": {
        "total_submissions": 25,
        "average_rating": 4.2,
        "rating_distribution": {
            "1": 1,
            "2": 2,
            "3": 5,
            "4": 10,
            "5": 7
        }
    }
}
```

## 🚀 PROGRESS TRACKING STRUCTURES

### 1. Episode Progress Structure
**Endpoint**: `PUT /api/v1/journeys/{journeyId}/episodes/{episodeId}/progress`

**Database Table**: `user_episode_progress`
```sql
CREATE TABLE user_episode_progress (
    progress_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    journey_id VARCHAR(100) NOT NULL,
    episode_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL, -- 'not_started', 'in_progress', 'completed'
    progress_percentage DECIMAL(5,2) DEFAULT 0.00, -- 0.00 to 100.00
    play_position_seconds INT DEFAULT 0,
    total_duration_seconds INT DEFAULT 0,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_episode (user_id, journey_id, episode_id),
    INDEX idx_user_progress (user_id, journey_id),
    INDEX idx_episode_progress (episode_id, status)
);
```

**Request JSON Format**:
```json
{
    "progress_percentage": 75.5,
    "play_position_seconds": 1206
}
```

**Response JSON Format**:
```json
{
    "success": true,
    "message": "Progress updated successfully",
    "user_progress": {
        "user_id": "user123",
        "journey_id": "journey_dsa",
        "episode_id": "episode_1",
        "status": "in_progress",
        "progress_percentage": 75.5,
        "play_position_seconds": 1206,
        "started_at": "2025-09-27T14:00:00Z"
    }
}
```

### 2. Episode Start Structure
**Endpoint**: `POST /api/v1/journeys/{journeyId}/episodes/{episodeId}/start`

**Response JSON Format**:
```json
{
    "success": true,
    "message": "Episode started successfully",
    "episode": {
        "episode_id": "episode_1",
        "title": "Introduction to Data Structures",
        "description": "Learn the basics of data structures and their importance in programming.",
        "duration_minutes": 25,
        "order_index": 1,
        "audio_file_path": "/audio/dsa/episode_1.mp3"
    },
    "audio_url": "https://storage.example.com/audio/dsa/episode_1.mp3?expires=1727449822",
    "user_progress": {
        "user_id": "user123",
        "journey_id": "journey_dsa",
        "episode_id": "episode_1",
        "status": "in_progress",
        "progress_percentage": 0.0,
        "play_position_seconds": 0,
        "started_at": "2025-09-27T14:30:22Z"
    }
}
```

### 3. User Overall Progress Structure
**Endpoint**: `GET /api/v1/users/{user_id}/progress`

**Response JSON Format**:
```json
{
    "success": true,
    "user_id": "user123",
    "total_journeys": 3,
    "completed_journeys": 1,
    "in_progress_journeys": 2,
    "total_episodes_completed": 15,
    "total_listening_time_minutes": 425,
    "journey_progress": [
        {
            "journey_id": "journey_dsa",
            "title": "DSA / Cracking Coding Interviews",
            "completion_percentage": 60.5,
            "episodes_completed": 3,
            "total_episodes": 5,
            "last_accessed": "2025-09-27T14:30:22Z"
        },
        {
            "journey_id": "journey_finance",
            "title": "Personal Finance",
            "completion_percentage": 100.0,
            "episodes_completed": 8,
            "total_episodes": 8,
            "last_accessed": "2025-09-25T10:15:30Z"
        }
    ]
}
```

## 🎵 PLAYBACK & ANALYTICS STRUCTURES

### 1. Playback Progress Structure
**Endpoint**: `POST /api/v1/playback/episodes/{episode_id}/progress`

**Database Table**: `playback_sessions`
```sql
CREATE TABLE playback_sessions (
    session_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    episode_id VARCHAR(100) NOT NULL,
    journey_id VARCHAR(100) NOT NULL,
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    play_position_seconds INT DEFAULT 0,
    session_duration_seconds INT DEFAULT 0,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_sessions (user_id, episode_id)
);
```

**Request JSON Format**:
```json
{
    "progress_percentage": 45.8,
    "play_position_seconds": 687,
    "session_id": "session_123456789"
}
```

**Response JSON Format**:
```json
{
    "success": true,
    "message": "Playback progress updated",
    "updated_progress": {
        "episode_id": "episode_1",
        "progress_percentage": 45.8,
        "play_position_seconds": 687,
        "last_updated": "2025-09-27T14:35:45Z"
    }
}
```

### 2. Listen Analytics Structure
**Endpoint**: `POST /api/v1/playback/episodes/{episode_id}/analytics`

**Database Table**: `listen_events`
```sql
CREATE TABLE listen_events (
    event_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    episode_id VARCHAR(100) NOT NULL,
    journey_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(20) NOT NULL, -- 'start', 'pause', 'resume', 'complete', 'skip'
    play_position_seconds INT NOT NULL,
    session_id VARCHAR(50) NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_events (user_id, episode_id),
    INDEX idx_event_type (event_type, timestamp)
);
```

**Request JSON Format**:
```json
{
    "event_type": "pause",
    "timestamp": "2025-09-27T14:35:45Z",
    "play_position_seconds": 687,
    "session_id": "session_123456789"
}
```

**Response JSON Format**:
```json
{
    "success": true,
    "message": "Listen event recorded",
    "event_id": "event_987654321"
}
```

## 👤 USER PROFILE STRUCTURES

### 1. User Profile Structure
**Endpoints**: 
- `POST /api/v1/users/profile` (Create)
- `GET /api/v1/users/profile/me` (Get)
- `PUT /api/v1/users/profile/me` (Update)

**Database Table**: `user_profiles`
```sql
CREATE TABLE user_profiles (
    user_id VARCHAR(100) PRIMARY KEY,
    avatar_id INT DEFAULT 1,
    name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(50) NOT NULL,
    profession VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);
```

**Create Request JSON Format**:
```json
{
    "avatar_id": 3,
    "name": "John Doe",
    "display_name": "Johnny",
    "date_of_birth": "1995-06-15",
    "gender": "Male",
    "profession": "Software Engineer"
}
```

**Response JSON Format**:
```json
{
    "user_id": "user123",
    "avatar_id": 3,
    "name": "John Doe",
    "display_name": "Johnny",
    "date_of_birth": "1995-06-15",
    "gender": "Male",
    "profession": "Software Engineer",
    "created_at": "2025-09-27T14:30:22Z"
}
```

## 🔐 AUTHENTICATION STRUCTURES

### 1. Login Info Structure
**Endpoint**: `POST /api/v1/auth/login`

**Database Table**: `user_login_info`
```sql
CREATE TABLE user_login_info (
    login_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL, -- 'wisme-mvpv1'
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    login_count INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_provider (user_id, provider),
    INDEX idx_email (email)
);
```

**Request JSON Format**:
```json
{
    "email": "john.doe@example.com",
    "provider": "wisme-mvpv1"
}
```

**Response JSON Format**:
```json
{
    "success": true,
    "message": "Login info recorded successfully",
    "login_info": {
        "last_login": "2025-09-27T14:30:22Z",
        "login_count": 5,
        "provider": "wisme-mvpv1"
    }
}
```

## 🚨 CRITICAL IMPLEMENTATION NOTES

### Database Indexes for Performance
```sql
-- Essential indexes for feedback system
CREATE INDEX idx_feedback_user_type ON feedback_submissions(user_id, feedback_type);
CREATE INDEX idx_feedback_context ON feedback_submissions(feedback_type, context_id);
CREATE INDEX idx_response_submission ON feedback_responses(submission_id);

-- Essential indexes for progress tracking
CREATE INDEX idx_progress_user_journey ON user_episode_progress(user_id, journey_id);
CREATE INDEX idx_progress_episode_status ON user_episode_progress(episode_id, status);
CREATE INDEX idx_playback_user_episode ON playback_sessions(user_id, episode_id);

-- Essential indexes for analytics
CREATE INDEX idx_events_user_episode ON listen_events(user_id, episode_id);
CREATE INDEX idx_events_type_time ON listen_events(event_type, timestamp);
```

## 📝 COMPLETE FEEDBACK TYPES SUMMARY

### ✅ **All Three Feedback Types with Exact Questions**

**1. Episode Feedback (`feedback_type: "episode"`)**
- **Question 1**: "Did you enjoy this episode?" → Options: `["Yes", "Somewhat", "No"]`
- **Question 2**: "Was this episode clear and easy to follow?" → Options: `["Very clear", "Somewhat clear", "Confusing"]`
- **Context ID**: Specific episode ID (e.g., `"episode_1"`)
- **Purpose**: Rate individual episode experience

**2. Journey Feedback (`feedback_type: "journey"`)** 
- **Question 1**: "Compared to MOOCs (like Coursera, Udemy), how would you rate this journey?" → Options: `["Much better", "Slightly better", "About the same", "Worse"]`
- **Question 2**: "Compared to YouTube, how would you rate this journey?" → Options: `["Much better", "Slightly better", "About the same", "Worse"]`
- **Question 3**: "Compared to blogs/articles, how would you rate this journey?" → Options: `["Much better", "Slightly better", "About the same", "Worse"]`
- **Context ID**: Specific journey ID (e.g., `"journey_dsa"`)
- **Purpose**: Compare Wisme journeys to other learning platforms

**3. General Feedback (`feedback_type: "general"`)** 
- **Question 1**: "Would you want to revisit any journey to revise?" → Options: `["Yes", "Maybe", "No"]`
- **Question 2**: "Would you recommend Wisme to a friend?" → Options: `["Yes", "Maybe", "No"]`
- **Question 3**: "If Wisme offered a fully personalized journey for any topic you want, how much would you pay?" → Type: `number` (open text input)
- **Context ID**: Always `"general"` (no specific context)
- **Purpose**: Overall platform satisfaction and business insights

### Frontend-Backend Sync Rules

1. **Timestamps**: Always use ISO 8601 format (`YYYY-MM-DDTHH:mm:ssZ`)
2. **IDs**: Use consistent prefixing (`episode_`, `journey_`, `user_`, `sub_`, `event_`)
3. **Progress**: Always sync both `progress_percentage` and `play_position_seconds`
4. **Status Values**: Use exactly these values: `'not_started'`, `'in_progress'`, `'completed'`
5. **Feedback Types**: Use exactly: `'episode'`, `'journey'`, `'general'`
6. **Response Types**: Use exactly: `'multiple_choice'`, `'number'`
7. **Question IDs**: Use exact IDs as specified (e.g., `episode_enjoyment`, `journey_moocs_comparison`, `general_willingness_to_pay`)

### Error Handling Standards
All endpoints should return errors in this format:
```json
{
    "success": false,
    "error": "VALIDATION_ERROR",
    "message": "Invalid feedback_type. Must be one of: episode, journey, general",
    "details": {
        "field": "feedback_type",
        "provided": "invalid_type",
        "expected": ["episode", "journey", "general"]
    }
}
```

This ensures perfect synchronization between frontend and backend with no data mismatches! 🎯