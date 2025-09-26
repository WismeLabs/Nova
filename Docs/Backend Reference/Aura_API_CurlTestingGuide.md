Aura API - cURL Testing Guide
 Live API URL: https://8eb80c8f633d.ngrok-free.app
 Monitor Dashboard: http://127.0.0.1:4040 (for developers)
SSL Issue Fix for Ngrok Free Tier
If you encounter SSL errors like EPROTO or TLSV1_ALERT_UNRECOGNIZED_NAME , add the -
-insecure flag to bypass SSL verification:
# Add --insecure flag for ngrok free tier SSL issues
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/health
Alternative Solutions:
1. Use --insecure flag (recommended for testing)
2. Use HTTP instead of HTTPS (if available)
3. Add --ssl-no-revoke flag on Windows
Quick Start Commands
1. Health Checks
# Main health check (with SSL fix)
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/health
# Authentication service health
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/auth/health
# Users service health
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/users/health/check
Core Features Testing
2. Journey System
Get all available journeys:
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/journeys/test
Start episode and get audio streaming URL:
# DSA - Arrays episode
curl --insecure -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/dsa_coding_interviews/episodes/arrays_basics/start
# DSA - Linked Lists episode
curl --insecure -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/dsa_coding_interviews/episodes/linked_lists/start
# Finance - Budgeting episode
curl --insecure -X POST https://f49815240383.ngrok-
free.app/api/v1/journeys/test/personal_finance/episodes/budgeting_basics/start
# Science - Quantum Mechanics episode
curl --insecure -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/science_mystery/episodes/quantum_mechanics/start
# Psychology - Cognitive Biases episode (FIXED - This was causing your SSL error)
curl --insecure -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/human_psychology/episodes/cognitive_biases/start
Update episode progress:
curl --insecure -X PUT https://f49815240383.ngrokfree.app/api/v1/journeys/test/dsa_coding_interviews/episodes/arrays_basics/progress \
-H "Content-Type: application/json" \
-d '{
"progress_percentage": 65.5,
"play_position_seconds": 314
}'
3. Authentication Status
# Get authentication status (without token)
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/auth/status
# Get login information
curl --insecure -X POST https://8eb80c8f633d.ngrok-free.app/api/v1/auth/login
4. Feedback System
Get feedback questions:
# Episode feedback questions
curl --insecure -X GET https://f49815240383.ngrokfree.app/api/v1/feedback/questions/episode
# Journey feedback questions
curl --insecure -X GET https://f49815240383.ngrokfree.app/api/v1/feedback/questions/journey
# General app feedback questions
curl --insecure -X GET https://f49815240383.ngrokfree.app/api/v1/feedback/questions/persistent
Get feedback analytics:
# Episode analytics
curl --insecure -X GET "https://f49815240383.ngrokfree.app/api/v1/feedback/analytics/episode?context_id=arrays_basics"
# Journey analytics
curl --insecure -X GET "https://f49815240383.ngrokfree.app/api/v1/feedback/analytics/journey?context_id=dsa_coding_interviews"
5. Suggested Topics
# Get suggested topics
curl -X GET "https://8eb80c8f633d.ngrok-free.app/api/v1/suggested-topics/?limit=5"
# Get more topics
curl -X GET "https://8eb80c8f633d.ngrok-free.app/api/v1/suggested-topics/?limit=10"
6. Playback System
# Get continue listening recommendations
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/playback/continue-listening
# Get episode stream URL
curl -X GET https://f49815240383.ngrokfree.app/api/v1/playback/episodes/arrays_basics/stream
7. Podcast Management
# Get podcasts list
curl -X GET "https://8eb80c8f633d.ngrok-free.app/api/v1/podcasts/?limit=10"
# Get podcast recommendations
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/podcasts/recommendations
Error Handling Tests
8. Testing Error Responses
Advanced Testing
9. Complete Audio Streaming Test
# Test non-existent journey
curl -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/non_existent_journey/episodes/test/start
# Test non-existent episode
curl -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/dsa_coding_interviews/episodes/non_existent_episode/start
# Test invalid feedback type
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/feedback/questions/invalid_type
# Test authentication required endpoints (will return "Not authenticated")
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/journeys/
# Get episode with audio URL and test the streaming URL
curl -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/dsa_coding_interviews/episodes/arrays_basics/start |
grep -o '"audio_url":"[^"]*"'
10. JSON Response Formatting
Pretty print JSON responses:
# Install jq for JSON formatting (optional)
# Windows: winget install jqlang.jq
# Then use:
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/journeys/test | jq '.'
Without jq (basic formatting):
curl -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/journeys/test | python -m
json.tool
Sample Responses
Journey List Response Sample:
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
"episodes": [...]
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
Episode Start Response Sample:
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
Available Content
 Complete Journeys Ready for Testing:
1. DSA & Coding Interviews ( dsa_coding_interviews ) - 6 episodes
arrays_basics , linked_lists , stacks_queues , trees_graphs ,
dynamic_programming , advanced_algorithms
2. Personal Finance Mastery ( personal_finance ) - 7 episodes
budgeting_basics , investment_fundamentals , retirement_planning ,
tax_optimization , real_estate , insurance_planning , wealth_building
3. Hackathon Success Guide ( hackathon_success ) - 7 episodes
preparation_strategy , team_building , idea_validation ,
rapid_prototyping , presentation_skills , technical_execution ,
post_hackathon
4. Human Psychology Insights ( human_psychology ) - 4 episodes
cognitive_biases , emotional_intelligence , social_psychology ,
decision_making
5. Science Mystery Adventures ( science_mystery ) - 8 episodes
quantum_mechanics , black_holes , artificial_intelligence ,
climate_science , genetic_engineering , space_exploration ,
neuroscience , renewable_energy
Quick Copy-Paste Test Suite
Run all core tests (SSL-safe):
# Health check
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/health
# Get journeys
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/journeys/test
# Start episode
curl --insecure -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/dsa_coding_interviews/episodes/arrays_basics/start
# Get feedback questions
curl --insecure -X GET https://f49815240383.ngrokfree.app/api/v1/feedback/questions/episode
# Get suggested topics
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/suggested-topics/
# Test the Psychology episode that was failing
curl --insecure -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/human_psychology/episodes/cognitive_biases/start
JavaScript Testing Example
// Modern fetch API testing
const API_BASE = 'https://8eb80c8f633d.ngrok-free.app/api/v1';
// Test journey system
async function testAuraAPI() {
try {
// Get all journeys
const journeys = await fetch(`${API_BASE}/journeys/test`).then(r => r.json());
console.log(' Journeys loaded:', journeys.total_count);
// Start an episode
const episode = await
fetch(`${API_BASE}/journeys/test/dsa_coding_interviews/episodes/arrays_basics/start`,
{
method: 'POST'
}).then(r => r.json());
console.log(' Episode started, audio URL:', episode.audio_url.substring(0, 50) +
'...');
// Get feedback questions
const questions = await fetch(`${API_BASE}/feedback/questions/episode`).then(r =>
r.json());
console.log(' Feedback questions loaded:', questions.length);
} catch (error) {
console.error(' API Test failed:', error);
}
}
testAuraAPI();
Troubleshooting Common Issues
SSL/TLS Errors (EPROTO, TLSV1_ALERT_UNRECOGNIZED_NAME)
Problem: Getting SSL errors when using ngrok free tier
Solution: Add --insecure flag to all curl commands
# Instead of this (causes SSL error):
curl -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/human_psychology/episodes/cognitive_biases/start
# Use this (SSL-safe):
curl --insecure -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/test/human_psychology/episodes/cognitive_biases/start
Alternative SSL Fixes:
# Windows specific SSL fix
curl --ssl-no-revoke --insecure -X GET https://8eb80c8f633d.ngrok-free.app/health
# Verbose output for debugging
curl --insecure -v -X GET https://8eb80c8f633d.ngrok-free.app/health
# Using specific TLS version
curl --insecure --tlsv1.2 -X GET https://8eb80c8f633d.ngrok-free.app/health
Testing Different Clients:
PowerShell (Windows):
# PowerShell equivalent (bypasses SSL issues)
Invoke-WebRequest -Uri "https://8eb80c8f633d.ngrok-free.app/health" -
SkipCertificateCheck
Python requests:
import requests
import urllib3
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
response = requests.get('https://8eb80c8f633d.ngrok-free.app/health', verify=False)
print(response.json())
HTTP Status Code Reference:
200: Success
404: Endpoint not found (check URL)
500: Server error (check ngrok connection)
SSL Error: Use --insecure flag
 REAL AUTHENTICATED ENDPOINTS (Production Ready)
Important: The above sections show TEST endpoints for quick testing. Below are
the REAL production endpoints that require Firebase authentication and provide
user-specific data security.
Authentication Required
All real endpoints require a Firebase ID token in the Authorization header:
-H "Authorization: Bearer YOUR_FIREBASE_ID_TOKEN_HERE"
Real Journey System Endpoints
Get all journeys (user-specific):
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/journeys/ \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE"
Get specific journey (user-specific):
curl --insecure -X GET https://f49815240383.ngrokfree.app/api/v1/journeys/dsa_coding_interviews \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE"
Start episode (user-specific):
curl --insecure -X POST https://f49815240383.ngrokfree.app/api/v1/journeys/dsa_coding_interviews/episodes/arrays_basics/start \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE"
Update episode progress (user-specific)  SECURE:
curl --insecure -X PUT https://f49815240383.ngrokfree.app/api/v1/journeys/dsa_coding_interviews/episodes/arrays_basics/progress \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE" \
-H "Content-Type: application/json" \
-d '{
"progress_percentage": 65.5,
"play_position_seconds": 314
}'
Real User Management Endpoints
Create user profile:
curl --insecure -X POST https://8eb80c8f633d.ngrok-free.app/api/v1/users/profile \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE" \
-H "Content-Type: application/json" \
-d '{
"avatar_id": 5,
"name": "John Doe",
"display_name": "Johnny",
"date_of_birth": "1990-01-15",
"gender": "male",
"profession": "Software Engineer"
}'
Get my profile:
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/users/profile/me \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE"
Update my profile:
curl --insecure -X PUT https://8eb80c8f633d.ngrok-free.app/api/v1/users/profile/me \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE" \
-H "Content-Type: application/json" \
-d '{
"display_name": "John D",
"profession": "Senior Software Engineer"
}'
Get user ID:
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/users/user-id \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE"
Real Feedback System Endpoints
Submit feedback (user-specific):
curl --insecure -X POST https://8eb80c8f633d.ngrok-free.app/api/v1/feedback/submit \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE" \
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
Get my feedback submissions:
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/feedback/mysubmissions \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE"
Delete my feedback submission:
curl --insecure -X DELETE https://8eb80c8f633d.ngrok-free.app/api/v1/feedback/mysubmissions/SUBMISSION_ID \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE"
Real Authentication Endpoints
Verify Firebase token:
curl --insecure -X POST https://8eb80c8f633d.ngrok-free.app/api/v1/auth/verify-token \
-H "Content-Type: application/json" \
-d '{"token": "YOUR_FIREBASE_TOKEN_HERE"}'
Get user profile (from token):
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/auth/profile \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE"
Get user claims:
curl --insecure -X GET https://8eb80c8f633d.ngrok-free.app/api/v1/auth/claims \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE"
Real Suggested Topics Endpoints
Increment topic count (user-specific):
curl --insecure -X POST https://8eb80c8f633d.ngrok-free.app/api/v1/suggestedtopics/increment/Machine%20Learning \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE"
Create topic (admin):
curl --insecure -X POST https://8eb80c8f633d.ngrok-free.app/api/v1/suggestedtopics/admin/create \
-H "Authorization: Bearer YOUR_FIREBASE_TOKEN_HERE" \
-H "Content-Type: application/json" \
-d '{
"name": "Artificial Intelligence",
"count": 0
}'
 Security Comparison
Feature Test Endpoints (/test/) Real Endpoints (no /test/)
Authentication  None required  Firebase token required
User ID
 Hardcoded
test_user_123
 From Firebase token
(current_user["uid"])
Data Isolation  Shared test data  User-specific data
Production
Ready
 Testing only  Production ready
Progress
Updates
 No endpoint
available
 User-specific updates
Security  Public access  Authenticated access only
 How to Get Firebase Token
JavaScript/Frontend Example:
import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';
const auth = getAuth();
signInWithEmailAndPassword(auth, email, password)
.then(async (userCredential) => {
const token = await userCredential.user.getIdToken();
console.log('Firebase Token:', token);
// Use this token in your API calls
const response = await fetch('https://f49815240383.ngrokfree.app/api/v1/journeys/', {
headers: {
'Authorization': `Bearer ${token}`
}
});
});
Real API Integration Example:
class AuraAPI {
constructor(baseURL, firebaseToken) {
this.baseURL = baseURL;
this.firebaseToken = firebaseToken;
}
async updateProgress(journeyId, episodeId, progress) {
const response = await fetch(
`${this.baseURL}/journeys/${journeyId}/episodes/${episodeId}/progress`,
{
method: 'PUT',
headers: {
'Authorization': `Bearer ${this.firebaseToken}`,
'Content-Type': 'application/json'
},
body: JSON.stringify(progress)
}
);
return response.json();
}
}
// Usage
const aura = new AuraAPI('https://8eb80c8f633d.ngrok-free.app/api/v1', firebaseToken);
await aura.updateProgress('dsa_coding_interviews', 'arrays_basics', {
progress_percentage: 75.0,
play_position_seconds: 480
});
 API Status: All core features tested and working
 Last Updated: September 25, 2025
SSL Fix: Use --insecure flag for ngrok free tier
 Security: Test endpoints for development, Real endpoints for production
 Support: Test any endpoint and check real-time monitoring at http://127.0.0.1:4040

 Note: serving my localhost for now you can use the endpoint and test for your integration
