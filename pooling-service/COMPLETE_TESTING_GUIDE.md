# IslandHop Pooling Service - Complete Testing Guide

## 🎯 Overview
This guide provides comprehensive testing scenarios for the IslandHop Pooling Service, covering all user flows and edge cases. Use this alongside the Postman collection for thorough testing.

---

## 🧪 Test Environment Setup

### Prerequisites
1. **Service Running**: Pooling Service on `http://localhost:8086`
2. **Database**: MongoDB with pooling data
3. **Dependencies**: Trip Planning Service, User Service (for external calls)
4. **Postman**: Import `POOLING_SERVICE_COMPLETE_TEST_COLLECTION.postman_collection.json`

### Environment Variables
```
base_url = http://localhost:8086/api/v1
user_id = test-user-123
friend_user_id = friend-user-456
user_email = testuser@example.com
```

---

## 🌊 Scenario 1: Complete Public Pooling Flow

### User Story
*"As a traveler, I want to create a public group for my Sri Lanka trip, but first check if there are compatible existing groups I can join instead."*

### Test Steps

#### Step 1: Pre-check Compatible Groups
```
POST /public-pooling/pre-check
```

**Test Data:**
```json
{
  "userId": "test-user-123",
  "baseCity": "Colombo",
  "startDate": "2025-10-15",
  "endDate": "2025-10-20",
  "budgetLevel": "Medium",
  "preferredActivities": ["Cultural Tours", "Beach Activities"],
  "preferredTerrains": ["Beach", "Cultural Sites"],
  "activityPacing": "Normal",
  "multiCityAllowed": true
}
```

**Expected Results:**
- ✅ Status 200
- ✅ Response contains `compatibleGroups` array
- ✅ Each group has `compatibilityScore` between 0-1
- ✅ Groups are sorted by compatibility score (descending)

**Validation Points:**
- Compatible groups should match user preferences
- Compatibility scores should be realistic (0.6+ for good matches)
- Response time should be under 2 seconds

#### Step 2: Create Group with Trip Planning
```
POST /groups/with-trip
```

**Test Data:**
```json
{
  "userId": "test-user-123",
  "userEmail": "testuser@example.com",
  "groupName": "Sri Lanka Cultural Explorer",
  "tripName": "Amazing Sri Lanka Cultural Journey",
  "startDate": "2025-10-15",
  "endDate": "2025-10-20",
  "baseCity": "Colombo",
  "arrivalTime": "14:30",
  "multiCityAllowed": true,
  "activityPacing": "Normal",
  "budgetLevel": "Medium",
  "preferredTerrains": ["Beach", "Cultural Sites", "Mountain"],
  "preferredActivities": ["Cultural Tours", "Beach Activities", "Hiking"],
  "visibility": "public",
  "maxMembers": 8,
  "requiresApproval": true
}
```

**Expected Results:**
- ✅ Status 201 (Created)
- ✅ Response contains both `groupId` and `tripId`
- ✅ Group status is `awaiting_trip_completion`
- ✅ Trip is created in trip planning service

**Store Variables:**
- `created_group_id`
- `created_trip_id`

#### Step 3: Complete Trip Planning (Simulated)
*This step simulates the user completing their trip planning in the trip planning service.*

#### Step 4: Save Trip with Suggestions
```
POST /public-pooling/groups/{groupId}/save-trip
```

**Test Data:**
```json
{
  "userId": "test-user-123",
  "tripId": "{{created_trip_id}}",
  "tripData": {
    "tripName": "Amazing Sri Lanka Cultural Journey",
    "startDate": "2025-10-15",
    "endDate": "2025-10-20",
    "baseCity": "Colombo",
    "cities": ["Colombo", "Kandy", "Galle"],
    "destinations": ["Colombo", "Kandy", "Galle"],
    "places": [
      {
        "name": "Gangaramaya Temple",
        "city": "Colombo",
        "type": "Cultural",
        "day": 1
      },
      {
        "name": "Temple of the Tooth",
        "city": "Kandy",
        "type": "Cultural",
        "day": 2
      }
    ],
    "activities": ["Cultural Tours", "Photography"],
    "terrains": ["Cultural Sites", "Coastal Areas"],
    "itinerary": [
      {
        "day": 1,
        "date": "2025-10-15",
        "city": "Colombo",
        "activities": ["Gangaramaya Temple", "National Museum"]
      }
    ]
  }
}
```

**Expected Results:**
- ✅ Status 200
- ✅ Response contains suggestions for similar groups
- ✅ Trip data is saved to the group
- ✅ Compatibility scores are calculated for suggestions

#### Step 5A: Finalize Group (Keep New Group)
```
POST /public-pooling/groups/{groupId}/finalize
```

**Test Data:**
```json
{
  "userId": "test-user-123",
  "action": "finalize",
  "reason": "Proceeding with our planned group trip"
}
```

**Expected Results:**
- ✅ Status 200
- ✅ Group status changes to `finalized`
- ✅ Group becomes visible in public listings
- ✅ Group is ready to accept join requests

#### Step 5B: Alternative - Join Existing Group
```
POST /public-pooling/groups/{groupId}/join-existing
```

**Test Data:**
```json
{
  "userId": "test-user-123",
  "targetGroupId": "existing-group-from-suggestions",
  "message": "I'd like to join your group instead!"
}
```

**Expected Results:**
- ✅ Status 200
- ✅ User is added to target group
- ✅ Original group is discarded
- ✅ User receives confirmation message

---

## 🏠 Scenario 2: Private Group Management

### User Story
*"As a group organizer, I want to create a private family trip and invite specific family members via email."*

### Test Steps

#### Step 1: Create Private Group
```
POST /groups/with-trip
```

**Test Data:**
```json
{
  "userId": "test-user-123",
  "userEmail": "organizer@family.com",
  "groupName": "Family Adventure Sri Lanka",
  "tripName": "Family-Friendly Sri Lanka Tour",
  "startDate": "2025-11-10",
  "endDate": "2025-11-16",
  "baseCity": "Colombo",
  "visibility": "private",
  "maxMembers": 6,
  "requiresApproval": false,
  "budgetLevel": "High",
  "preferredActivities": ["Wildlife Safari", "Beach Activities"]
}
```

**Expected Results:**
- ✅ Private group created
- ✅ Only creator is member initially
- ✅ Group not visible in public listings

#### Step 2: Invite Family Member
```
POST /groups/{groupId}/invite
```

**Test Data:**
```json
{
  "userId": "test-user-123",
  "invitedEmail": "family.member@email.com",
  "message": "Join our family trip to Sri Lanka!",
  "expirationDays": 7
}
```

**Expected Results:**
- ✅ Invitation created and sent
- ✅ Invitation ID returned
- ✅ Expiration date set correctly

#### Step 3: View Invitations (As Invited User)
```
GET /groups/invitations/{userId}
```

**Expected Results:**
- ✅ Invitation appears in user's list
- ✅ All invitation details present
- ✅ Status is `pending`

#### Step 4: Accept Invitation
```
POST /groups/invitations/respond
```

**Test Data:**
```json
{
  "userId": "friend-user-456",
  "invitationId": "{{invitation_id}}",
  "action": "accept",
  "message": "Excited to join!"
}
```

**Expected Results:**
- ✅ User added to group
- ✅ Invitation status updated
- ✅ Group member count increased

---

## 🔍 Scenario 3: Group Discovery & Joining

### User Story
*"As a solo traveler, I want to browse available public groups and join one that matches my preferences."*

### Test Steps

#### Step 1: Browse All Public Groups
```
GET /groups/public/enhanced
```

**Expected Results:**
- ✅ List of all public groups
- ✅ Each group has complete information
- ✅ Only `public` and `finalized` groups shown

#### Step 2: Browse with Filters
```
GET /groups/public/enhanced?baseCity=Colombo&budgetLevel=Medium&preferredActivities=Cultural Tours
```

**Expected Results:**
- ✅ Filtered results match criteria
- ✅ Results are relevant to user preferences
- ✅ Empty array if no matches (not error)

#### Step 3: Request to Join Group
```
POST /groups/{groupId}/join
```

**Test Data:**
```json
{
  "userId": "friend-user-456",
  "userEmail": "joiner@example.com",
  "userName": "Sarah Johnson",
  "message": "I'm interested in joining your cultural tour!",
  "userProfile": {
    "age": 28,
    "interests": ["Cultural Tours", "Photography"],
    "travelExperience": "Experienced"
  }
}
```

**Expected Results:**
- ✅ Join request submitted
- ✅ Status indicates if approval needed
- ✅ Appropriate response for group settings

---

## 🤝 Scenario 4: Member Voting System

### User Story
*"As a group member, I want to vote on join requests to ensure we maintain group compatibility."*

### Test Steps

#### Step 1: View Pending Requests (Group Admin)
```
GET /groups/{groupId}/join-requests/pending?userId={userId}
```

**Expected Results:**
- ✅ All pending requests for the group
- ✅ Request details including user profile
- ✅ Current vote status for requesting user

#### Step 2: View All Pending Votes (Member)
```
GET /groups/my-pending-requests?userId={userId}
```

**Expected Results:**
- ✅ All groups where user needs to vote
- ✅ Summary counts
- ✅ Individual request details

#### Step 3: Vote on Request
```
POST /groups/{groupId}/join-requests/{requestUserId}/vote
```

**Test Data:**
```json
{
  "userId": "test-user-123",
  "approved": true,
  "comment": "Great fit for our group!"
}
```

**Expected Results:**
- ✅ Vote recorded successfully
- ✅ Request status updated if enough votes
- ✅ Requester notified of final decision

---

## 🗺️ Scenario 5: Trip Details Access

### User Story
*"As a user, I want to view comprehensive trip details to understand what the group plans to do."*

### Test Steps

#### Step 1: Authenticated User Access
```
GET /public-pooling/trips/{tripId}/comprehensive?userId={userId}
```

**Expected Results:**
- ✅ Complete trip details
- ✅ Full itinerary information
- ✅ Group member information
- ✅ Personalized compatibility data

#### Step 2: Anonymous User Access
```
GET /public-pooling/trips/{tripId}/comprehensive
```

**Expected Results:**
- ✅ Public trip information accessible
- ✅ No sensitive data exposed
- ✅ Basic itinerary and group info available

#### Step 3: Check Trip Compatibility
```
GET /public-pooling/groups/compatible/{tripId}?userId={userId}
```

**Expected Results:**
- ✅ List of compatible groups
- ✅ Compatibility scores calculated
- ✅ Sorted by compatibility relevance

---

## ❌ Error Scenarios Testing

### Invalid Data Testing

#### Test 1: Pre-check with Invalid Data
```json
{
  "userId": "",
  "baseCity": "",
  "startDate": "invalid-date",
  "budgetLevel": "InvalidBudget"
}
```

**Expected:**
- ❌ Status 400
- ❌ Validation error response
- ❌ Specific field error messages

#### Test 2: Create Group with Missing Required Fields
```json
{
  "userId": "test-user-123"
  // Missing required fields
}
```

**Expected:**
- ❌ Status 400
- ❌ Field validation errors listed

### Authorization Testing

#### Test 3: Access Non-existent Group
```
GET /groups/non-existent-id/join-requests/pending?userId=test-user-123
```

**Expected:**
- ❌ Status 404
- ❌ "Group not found" error message

#### Test 4: Unauthorized Group Access
```json
{
  "userId": "unauthorized-user",
  "action": "finalize"
}
```

**Expected:**
- ❌ Status 403
- ❌ "Unauthorized access" error message

### Business Logic Testing

#### Test 5: Join Full Group
*Test joining a group that has reached max capacity*

**Expected:**
- ❌ Status 400
- ❌ "Group is full" error message

#### Test 6: Double Join Prevention
*Test user trying to join same group twice*

**Expected:**
- ❌ Status 400
- ❌ "Already a member" or "Request already pending"

---

## 🔄 Performance Testing

### Load Testing Scenarios

1. **Concurrent Pre-checks**: 50+ simultaneous pre-check requests
2. **Group Creation**: 20+ groups created simultaneously
3. **Browse Public Groups**: 100+ concurrent browse requests
4. **Vote Processing**: Multiple votes on same request

### Performance Benchmarks

- **Pre-check Response**: < 2 seconds
- **Group Creation**: < 3 seconds
- **Browse Groups**: < 1 second
- **Vote Processing**: < 500ms
- **Trip Details**: < 1.5 seconds

---

## 📊 Test Data Management

### Test Data Setup
```sql
-- Example test groups (create via API)
Group 1: "Cultural Sri Lanka" (public, 3/8 members, Colombo-Kandy-Galle)
Group 2: "Beach Adventure" (public, 2/6 members, Colombo-Bentota-Mirissa)
Group 3: "Family Safari" (private, 4/6 members, Colombo-Yala-Ella)
```

### Test User Profiles
```
User 1: Cultural enthusiast, Medium budget, Experienced traveler
User 2: Beach lover, High budget, First-time visitor
User 3: Family organizer, Medium budget, Safety-focused
User 4: Adventure seeker, Low budget, Solo traveler
```

### Cleanup After Testing
- Delete test groups
- Remove test invitations
- Clear test join requests
- Reset test user states

---

## 🎯 Test Results Validation

### Success Criteria Checklist

#### Functional Testing
- [ ] All happy path scenarios work end-to-end
- [ ] Error handling is consistent and user-friendly
- [ ] Data validation prevents invalid submissions
- [ ] Authorization prevents unauthorized access
- [ ] Business rules are enforced correctly

#### Performance Testing
- [ ] Response times meet benchmarks
- [ ] System handles concurrent requests
- [ ] No memory leaks during load testing
- [ ] Database queries are optimized

#### Integration Testing
- [ ] External service calls work correctly
- [ ] Database operations are consistent
- [ ] Event publishing/consuming works
- [ ] Frontend integration is seamless

#### Security Testing
- [ ] User data is properly protected
- [ ] Authentication is required where needed
- [ ] Input validation prevents injection attacks
- [ ] Sensitive data is not exposed in logs

---

## 🚀 Automated Testing Script

### Postman Collection Runner
1. Import collection into Postman
2. Set up environment with test variables
3. Run collection with iterations
4. Review test results and performance metrics

### Newman CLI Testing
```bash
# Install Newman
npm install -g newman

# Run collection
newman run POOLING_SERVICE_COMPLETE_TEST_COLLECTION.postman_collection.json \
  --environment test-environment.json \
  --reporters cli,html \
  --reporter-html-export test-results.html
```

### CI/CD Integration
```yaml
# Example GitHub Actions workflow
name: Pooling Service Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start Services
        run: docker-compose up -d
      - name: Run Tests
        run: newman run postman-collection.json
```

This comprehensive testing guide ensures all aspects of the Pooling Service are thoroughly validated before deployment.
