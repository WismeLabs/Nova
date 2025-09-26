Aura API Complete Integration Guide
Comprehensive documentation for all Aura application endpoints including
authentication, users, journeys, podcasts, feedback, and more.
Table of Contents
1. Quick Start
2. Authentication
3. User Management
4. Journey System
5. Podcast Management
6. Feedback System
7. Suggested Topics
8. Playback & Analytics
9. Error Handling
10. SDK Examples
Quick Start
Base Configuration
const API_BASE_URL = 'https://8eb80c8f633d.ngrok-free.app/api/v1';
const headers = {
'Content-Type': 'application/json',
'Authorization': `Bearer ${firebaseToken}` // When authentication required
};
Health Check
curl -X GET https://8eb80c8f633d.ngrok-free.app/health
Authentication
Overview
Aura uses Firebase Authentication. All protected endpoints require a Firebase ID token
in the Authorization header.
1. Verify Token
Endpoint: POST /auth/verify-token
Authentication: None required
curl -X POST https://8eb80c8f633d.ngrok-free.app/api/v1/auth/verify-token \
-H "Content-Type: application/json" \
-d '{"token": "firebase_id_token_here"}'
Response:
{
"success": true,
"message": "Token verified successfully",
"user": {
"uid": "firebase_user_id",
"email": "user@example.com",
"email_verified": true
}
}
2. Get User Profile
Endpoint: GET /auth/profile
Authentication: Required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/auth/profile \
-H "Authorization: Bearer <firebase_token>"
3. Get Current User (Alias)
Endpoint: GET /auth/me
Authentication: Required
4. Get User Claims
Endpoint: GET /auth/claims
Authentication: Required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/auth/claims \
-H "Authorization: Bearer <firebase_token>"
5. Revoke Tokens
Endpoint: POST /auth/revoke-tokens
Authentication: Required
6. Authentication Status
Endpoint: GET /auth/status
Authentication: Optional
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/auth/status \
-H "Authorization: Bearer <firebase_token>"
Response:
{
"authenticated": true,
"user_id": "firebase_uid",
"email": "user@example.com",
"email_verified": true
}
7. Login Information
Endpoint: POST /auth/login
Authentication: None required
8. Authentication Health Check
Endpoint: GET /auth/health
Authentication: None required
User Management
1. Create User Profile
Endpoint: POST /users/profile
Authentication: Required
curl -X POST https://8eb80c8f633d.ngrok-free.app/api/v1/users/profile \
-H "Authorization: Bearer <firebase_token>" \
-H "Content-Type: application/json" \
-d '{
"avatar_id": 5,
"name": "John Doe",
"display_name": "Johnny",
"date_of_birth": "1990-01-15",
"gender": "male",
"profession": "Software Engineer"
}'
Response:
{
"success": true,
"message": "User profile created successfully",
"user_id": "firebase_uid_here",
"profile": {
"user_id": "firebase_uid_here",
"avatar_id": 5,
"name": "John Doe",
"display_name": "Johnny",
"date_of_birth": "1990-01-15",
"gender": "male",
"profession": "Software Engineer",
"created_at": "2025-09-25T10:00:00Z"
}
}
2. Get My Profile
Endpoint: GET /users/profile/me
Authentication: Required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/users/profile/me \
-H "Authorization: Bearer <firebase_token>"
3. Update My Profile
Endpoint: PUT /users/profile/me
Authentication: Required
curl -X PUT https://8eb80c8f633d.ngrok-free.app/api/v1/users/profile/me \
-H "Authorization: Bearer <firebase_token>" \
-H "Content-Type: application/json" \
-d '{
"display_name": "John D",
"profession": "Senior Software Engineer"
}'
4. Get User ID
Endpoint: GET /users/user-id
Authentication: Required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/users/user-id \
-H "Authorization: Bearer <firebase_token>"
Response:
{
"user_id": "firebase_uid_here",
"profile_exists": true,
"profile_complete": true,
"message": "Store this user_id in localStorage for subsequent requests"
}
5. Verify User ID
Endpoint: GET /users/verify/{user_id}
Authentication: None required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/users/verify/firebase_uid_here
6. Get User Profile by ID
Endpoint: GET /users/{user_id}
Authentication: None required
7. Get User Progress
Endpoint: GET /users/{user_id}/progress
Authentication: Required (can only access own progress)
8. Users Health Check
Endpoint: GET /users/health/check
Authentication: None required
Journey System
1. Get All Journeys (Authenticated)
Endpoint: GET /journeys
Authentication: Required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/journeys \
-H "Authorization: Bearer <firebase_token>"
2. Get All Journeys (Test)
Endpoint: GET /journeys/test
Authentication: None required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/journeys/test
Response:
{
"success": true,
"message": "Retrieved 5 journeys",
"journeys": [
{
"journey": {
"journey_id": "dsa_coding_interviews",
"title": "DSA & Coding Interviews",
"description": "Master data structures and algorithms for coding interviews",
"category": "technology",
"difficulty": "intermediate",
"total_duration_minutes": 33,
"episodes": [
{
"episode_id": "arrays_basics",
"title": "Arrays and Strings",
"description": "Master array manipulations and string operations",
"duration_minutes": 8,
"order_index": 1,
"audio_file_path": "journeys/dsa_coding_interviews/dsa_episode_1.mp3"
}
]
},
"user_progress": {
"completion_percentage": 16.67,
"episodes_completed": 1,
"total_episodes": 6
}
}
],
"total_count": 5
}
3. Get Specific Journey (Authenticated)
Endpoint: GET /journeys/{journey_id}
Authentication: Required
4. Get Specific Journey (Test)
Endpoint: GET /journeys/test/{journey_id}
Authentication: None required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/journeys/test/dsa_coding_interviews
5. Start Episode (Authenticated)
Endpoint: POST /journeys/{journey_id}/episodes/{episode_id}/start
Authentication: Required
6. Start Episode (Test)
Endpoint: POST /journeys/test/{journey_id}/episodes/{episode_id}/start
Authentication: None required
Response:
{
"success": true,
"message": "Episode ready for streaming",
"episode": {
"episode_id": "arrays_basics",
"title": "Arrays and Strings",
"description": "Master array manipulations and string operations",
"duration_minutes": 8,
"order_index": 1,
"audio_file_path": "journeys/dsa_coding_interviews/dsa_episode_1.mp3"
},
"audio_url": "https://storage.googleapis.com/wismemvpv1.firebasestorage.app/journeys/dsa_coding_interviews/dsa_episode_1.mp3?
Expires=1758800779&GoogleAccessId=...",
"user_progress": {
"user_id": "test_user_123",
"journey_id": "dsa_coding_interviews",
"episode_id": "arrays_basics",
"status": "in_progress",
"progress_percentage": 0.0,
"play_position_seconds": 0,
"started_at": "2025-09-25T09:37:16.910554Z"
}
}
7. Update Episode Progress (Authenticated)
Endpoint: PUT /journeys/{journey_id}/episodes/{episode_id}/progress
Authentication: Required
curl -X POST
https://8eb80c8f633d.ngrok-free.app/api/v1/journeys/test/dsa_coding_interviews/episodes/arrays_basics/
8. Update Episode Progress (Test)
Endpoint: PUT /journeys/test/{journey_id}/episodes/{episode_id}/progress
Authentication: None required
Podcast Management
1. Get Podcasts
Endpoint: GET /podcasts/
Authentication: None required
curl -X GET "https://8eb80c8f633d.ngrok-free.app/api/v1/podcasts/?limit=10"
2. Get Podcast Details
Endpoint: GET /podcasts/{podcast_id}
Authentication: None required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/podcasts/example_podcast_id
3. Get Podcast Episodes
Endpoint: GET /podcasts/{podcast_id}/episodes
Authentication: None required
curl -X GET "https://8eb80c8f633d.ngrok-free.app/api/v1/podcasts/example_podcast_id/episodes?
limit=10"
4. Submit Podcast Feedback
Endpoint: POST /podcasts/feedback
Authentication: None required
5. Get Recommendations
Endpoint: GET /podcasts/recommendations
Authentication: None required
Feedback System
1. Submit Feedback
Endpoint: POST /feedback/submit
Authentication: Required
curl -X PUT
https://8eb80c8f633d.ngrok-free.app/api/v1/journeys/test/dsa_coding_interviews/episodes/arrays_basics/
\
-H "Content-Type: application/json" \
-d '{
"progress_percentage": 65.5,
"play_position_seconds": 314
}'
curl -X POST https://8eb80c8f633d.ngrok-free.app/api/v1/feedback/submit \
-H "Authorization: Bearer <firebase_token>" \
-H "Content-Type: application/json" \
-d '{
"feedback_type": "episode",
"context_id": "arrays_basics",
"responses": [
{
"question_id": "rating",
"response_value": "5"
},
{
"question_id": "comments",
"response_value": "Great episode!"
}
]
}'
Response:
{
"submission_id": "feedback_submission_id",
"feedback_type": "episode",
"context_id": "arrays_basics",
"user_id": "firebase_uid",
"responses": [...],
"submitted_at": "2025-09-25T10:30:00Z"
}
2. Get Feedback Questions
Endpoint: GET /feedback/questions/{feedback_type}
Authentication: None required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/feedback/questions/episode
Response:
[
{
"question_id": "rating",
"question_text": "How would you rate this episode?",
"response_type": "scale",
"options": ["1", "2", "3", "4", "5"]
},
{
"question_id": "comments",
"question_text": "Any additional comments?",
"response_type": "text",
"options": null
}
]
3. Get My Feedback Submissions
Endpoint: GET /feedback/my-submissions
Authentication: Required
curl -X GET "https://8eb80c8f633d.ngrok-free.app/api/v1/feedback/my-submissions?limit=50&offset=0" \
-H "Authorization: Bearer <firebase_token>"
4. Get Feedback Analytics
Endpoint: GET /feedback/analytics/{feedback_type}
Authentication: None required
curl -X GET "https://8eb80c8f633d.ngrok-free.app/api/v1/feedback/analytics/episode?
context_id=arrays_basics"
5. Get Feedback by Context
Endpoint: GET /feedback/by-context/{feedback_type}/{context_id}
Authentication: None required
6. Delete My Feedback Submission
Endpoint: DELETE /feedback/my-submissions/{submission_id}
Authentication: Required
Feedback Types
episode - Feedback for specific episodes
journey - Feedback for entire journeys
research_survey - Research survey responses (general feedback questions)
Suggested Topics
1. Get Suggested Topics
Endpoint: GET /suggested-topics/
Authentication: None required
curl -X GET "https://8eb80c8f633d.ngrok-free.app/api/v1/suggested-topics/?limit=5"
Response:
{
"success": true,
"topics": [
{
"name": "Machine Learning",
"count": 45,
"last_updated": "2025-09-25T10:00:00Z"
},
{
"name": "Web Development",
"count": 38,
"last_updated": "2025-09-25T09:45:00Z"
}
],
"total_count": 2,
"is_dummy_data": false
}
2. Increment Topic Count
Endpoint: POST /suggested-topics/increment/{topic_name}
Authentication: Required
curl -X POST "https://8eb80c8f633d.ngrok-free.app/api/v1/suggestedtopics/increment/Machine%20Learning?increment=1" \
-H "Authorization: Bearer <firebase_token>"
3. Create Suggested Topic (Admin)
Endpoint: POST /suggested-topics/admin/create
Authentication: Required
curl -X POST https://8eb80c8f633d.ngrok-free.app/api/v1/suggested-topics/admin/create \
-H "Authorization: Bearer <firebase_token>" \
-H "Content-Type: application/json" \
-d '{
"name": "Artificial Intelligence",
"count": 0
}'
4. Update Suggested Topic (Admin)
Endpoint: PUT /suggested-topics/admin/{topic_id}
Authentication: Required
Playback & Analytics
1. Get Stream URL
Endpoint: GET /playback/episodes/{episode_id}/stream
Authentication: None required
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/playback/episodes/arrays_basics/stream
2. Update Playback Progress
Endpoint: POST /playback/episodes/{episode_id}/progress
Authentication: None required
3. Get Continue Listening
Endpoint: GET /playback/continue-listening
Authentication: None required
4. Record Listen Event
Endpoint: POST /playback/episodes/{episode_id}/analytics
Authentication: None required
Error Handling
Common HTTP Status Codes
200 OK: Successful request
201 Created: Resource created successfully
400 Bad Request: Invalid request data
401 Unauthorized: Missing or invalid authentication
403 Forbidden: Access denied (e.g., accessing other user's data)
404 Not Found: Resource not found
422 Unprocessable Entity: Validation error
500 Internal Server Error: Server error
Error Response Format
{
"detail": "Error message describing what went wrong"
}
Validation Error Format
{
"detail": [
{
"loc": ["body", "field_name"],
"msg": "field required",
"type": "value_error.missing"
}
]
}
SDK Examples
Complete JavaScript SDK
class AuraAPI {
constructor(baseURL, firebaseToken = null) {
this.baseURL = baseURL;
this.firebaseToken = firebaseToken;
}
setAuthToken(token) {
this.firebaseToken = token;
}
async request(endpoint, options = {}) {
const url = `${this.baseURL}${endpoint}`;
const headers = {
'Content-Type': 'application/json',
...options.headers
};
if (this.firebaseToken && !options.noAuth) {
headers.Authorization = `Bearer ${this.firebaseToken}`;
}
const config = {
...options,
headers
};
try {
const response = await fetch(url, config);
const data = await response.json();
if (!response.ok) {
throw new Error(data.detail || `HTTP ${response.status}`);
}
return data;
} catch (error) {
console.error('API Request failed:', error);
throw error;
}
}
// Authentication
async verifyToken(token) {
return this.request('/auth/verify-token', {
method: 'POST',
body: JSON.stringify({ token }),
noAuth: true
});
}
async getProfile() {
return this.request('/auth/profile');
}
async getAuthStatus() {
return this.request('/auth/status');
}
// User Management
async createUserProfile(profileData) {
return this.request('/users/profile', {
method: 'POST',
body: JSON.stringify(profileData)
});
}
async getMyProfile() {
return this.request('/users/profile/me');
}
async updateMyProfile(profileData) {
return this.request('/users/profile/me', {
method: 'PUT',
body: JSON.stringify(profileData)
});
}
async getUserId() {
return this.request('/users/user-id');
}
// Journey System
async getAllJourneys(useTest = false) {
const endpoint = useTest ? '/journeys/test' : '/journeys';
return this.request(endpoint, useTest ? { noAuth: true } : {});
}
async getJourney(journeyId, useTest = false) {
const endpoint = useTest ? `/journeys/test/${journeyId}` :
`/journeys/${journeyId}`;
return this.request(endpoint, useTest ? { noAuth: true } : {});
}
async startEpisode(journeyId, episodeId, useTest = false) {
const endpoint = useTest
? `/journeys/test/${journeyId}/episodes/${episodeId}/start`
: `/journeys/${journeyId}/episodes/${episodeId}/start`;
return this.request(endpoint, {
method: 'POST',
...(useTest ? { noAuth: true } : {})
});
}
async updateEpisodeProgress(journeyId, episodeId, progressData, useTest = false) {
const endpoint = useTest
? `/journeys/test/${journeyId}/episodes/${episodeId}/progress`
: `/journeys/${journeyId}/episodes/${episodeId}/progress`;
return this.request(endpoint, {
method: 'PUT',
body: JSON.stringify(progressData),
...(useTest ? { noAuth: true } : {})
});
}
// Feedback System
async submitFeedback(feedbackData) {
return this.request('/feedback/submit', {
method: 'POST',
body: JSON.stringify(feedbackData)
});
}
async getFeedbackQuestions(feedbackType) {
return this.request(`/feedback/questions/${feedbackType}`, { noAuth: true });
}
async getMyFeedbackSubmissions(limit = 50, offset = 0) {
return this.request(`/feedback/my-submissions?limit=${limit}&offset=${offset}`);
}
// Suggested Topics
async getSuggestedTopics(limit = 5) {
return this.request(`/suggested-topics/?limit=${limit}`, { noAuth: true });
}
async incrementTopicCount(topicName, increment = 1) {
return this.request(`/suggested-topics/increment/${encodeURIComponent(topicName)}?
increment=${increment}`, {
method: 'POST'
});
}
// Health Checks
async healthCheck() {
return this.request('/health', { noAuth: true });
}
async authHealthCheck() {
return this.request('/auth/health', { noAuth: true });
}
async usersHealthCheck() {
return this.request('/users/health/check', { noAuth: true });
}
}
Usage Example
// Initialize SDK
const aura = new AuraAPI('https://8eb80c8f633d.ngrok-free.app/api/v1');
// Set authentication token (after Firebase auth)
aura.setAuthToken(firebaseIdToken);
// Use the API
try {
// Get all journeys
const journeys = await aura.getAllJourneys();
console.log('Available journeys:', journeys);
// Start an episode
const episode = await aura.startEpisode('dsa_coding_interviews', 'arrays_basics');
console.log('Episode started:', episode);
// Update progress
await aura.updateEpisodeProgress('dsa_coding_interviews', 'arrays_basics', {
progress_percentage: 50.0,
play_position_seconds: 240
});
// Submit feedback
await aura.submitFeedback({
feedback_type: 'episode',
context_id: 'arrays_basics',
responses: [
{ question_id: 'rating', response_value: '5' }
]
});
} catch (error) {
console.error('API Error:', error);
}
React Hook Example
import { useState, useEffect } from 'react';
export const useAuraAPI = (firebaseToken) => {
const [aura, setAura] = useState(null);
useEffect(() => {
const api = new AuraAPI('https://8eb80c8f633d.ngrok-free.app/api/v1', firebaseToken);
setAura(api);
}, [firebaseToken]);
return aura;
};
// Usage in component
const JourneysComponent = () => {
const aura = useAuraAPI(firebaseToken);
const [journeys, setJourneys] = useState([]);
useEffect(() => {
if (aura) {
aura.getAllJourneys().then(setJourneys);
}
}, [aura]);
return (
<div>
{journeys.map(journey => (
<div key={journey.journey_id}>{journey.title}</div>
))}
</div>
);
};
Available Journey Content
1. DSA / Cracking Coding Interviews (Career-focused, Skill-Building)

Goal: Help students crack coding interviews, from basics to advanced topics.

Ep 1: Why companies ask DSA questions (5 min)

Ep 2: Arrays & Strings refresher – common pitfalls (6 min)

Ep 3: Recursion & Backtracking basics (6 min)

Ep 4: Dynamic Programming (Intro + patterns) (6 min)

Ep 5: Tricks to approach any coding problem (5 min)

Ep 6: Mock interview mindset & time management (5 min)


Total: ~33 min


2. Personal Finance (Students + Young Professionals + Adults)

Goal: Teach foundational personal finance and modern investment trends.

Ep 1: Budgeting & Saving basics (5 min)

Ep 2: Emergency funds & debt management (6 min)

Ep 3: Understanding credit scores & loans (6 min)

Ep 4: Investing 101 – stocks, mutual funds, ETFs (7 min)

Ep 5: Trends & alternatives – crypto, fractional investing, SIP automation (6 min)

Ep 6: Tax planning basics & common mistakes (6 min)

Ep 7: Actionable plan – first 3 months of financial health (5 min)


Total: ~47 min


3. How to win Hackathons

 Goal: Equip students with proven strategies to consistently perform and win hackathons.
Ep 1: The Winning Mindset (5 min)
Ep 2: Choosing the Right Problem (6 min)
Ep 3: Team Building & Role Clarity (6 min)
Ep 4: Execution Strategy – MVP, Tools & Time Management (7 min)
Ep 5: Pitching & Presentation to Judges (6 min)
Ep 6: Common Mistakes & How to Avoid Them (6 min)
Ep 7: Action Plan – Preparing for Your Next Hackathon (5 min)

Total: ~25 min
