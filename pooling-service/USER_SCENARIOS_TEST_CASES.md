# IslandHop Pooling Service - Real User Scenarios & Test Cases

## 🎭 User Personas

### 👩‍💼 Sarah (Solo Cultural Explorer)
- **Profile**: 28, marketing professional, loves cultural experiences
- **Travel Style**: Mid-range budget, prefers guided tours, safety-conscious
- **Goals**: Find like-minded travelers for cultural exploration

### 👨‍👩‍👧‍👦 The Johnson Family
- **Profile**: Parents with 2 kids (8, 12), first Sri Lanka trip
- **Travel Style**: Higher budget, family-friendly activities, comfort-focused
- **Goals**: Create private group for extended family trip

### 🎒 Alex (Budget Backpacker)
- **Profile**: 22, recent graduate, limited budget, adventure-seeking
- **Travel Style**: Budget-conscious, flexible, loves meeting new people
- **Goals**: Join existing groups to share costs and make friends

### 👥 University Friends Group
- **Profile**: 4 college friends, graduation trip
- **Travel Style**: Medium budget, party-friendly, Instagram-worthy spots
- **Goals**: Plan epic graduation trip with voting on decisions

---

## 🌟 Real-World User Scenarios

### Scenario 1: Sarah's Cultural Journey Discovery

#### Background
Sarah wants to explore Sri Lanka's cultural heritage but doesn't want to travel alone. She's planned a 6-day cultural tour and wants to see if there are compatible groups before creating her own.

#### User Journey

**Step 1: Check Existing Options**
```
Action: Pre-check compatible groups
API: POST /public-pooling/pre-check
User Thinking: "Let me see if anyone else is planning something similar"
```

**Request:**
```json
{
  "userId": "sarah_123",
  "baseCity": "Colombo",
  "startDate": "2025-12-01", 
  "endDate": "2025-12-06",
  "budgetLevel": "Medium",
  "preferredActivities": ["Cultural Tours", "Photography", "Local Cuisine"],
  "preferredTerrains": ["Cultural Sites", "Historical Places"],
  "activityPacing": "Normal",
  "multiCityAllowed": true
}
```

**Frontend Experience:**
```javascript
// Sarah fills out her trip preferences form
const preferences = {
  destinations: "Cultural sites across Sri Lanka",
  budget: "Medium ($200-300/day)",
  activities: ["Temple visits", "Cultural tours", "Food experiences"],
  dates: "Dec 1-6, 2025"
};

// System checks for compatible groups
const compatibleGroups = await PoolsApi.preCheckCompatibleGroups(preferences);

if (compatibleGroups.length > 0) {
  // Show Sarah existing options
  showExistingGroupsModal(compatibleGroups);
} else {
  // Proceed with group creation
  proceedToGroupCreation();
}
```

**Response Analysis:**
- Found 3 compatible groups (75%, 68%, 61% compatibility)
- Groups have similar dates and cultural focus
- Sarah reviews group details and member profiles

**Step 2: Create Her Own Group (She decides existing groups don't quite fit)**
```
Action: Create public group with trip planning
API: POST /groups/with-trip
User Thinking: "I'll create my own group focusing on ancient temples"
```

**Request:**
```json
{
  "userId": "sarah_123",
  "userEmail": "sarah.explorer@email.com",
  "groupName": "Ancient Temples & Cultural Heritage",
  "tripName": "Sri Lanka Cultural Discovery Tour",
  "startDate": "2025-12-01",
  "endDate": "2025-12-06", 
  "baseCity": "Colombo",
  "arrivalTime": "16:00",
  "visibility": "public",
  "maxMembers": 6,
  "requiresApproval": true,
  "budgetLevel": "Medium",
  "preferredActivities": ["Cultural Tours", "Photography", "Local Cuisine"],
  "preferredTerrains": ["Cultural Sites", "Historical Places"],
  "additionalPreferences": {
    "focusArea": "Ancient temples and Buddhist heritage",
    "photographyFriendly": true,
    "localGuidePreferred": true
  }
}
```

**Step 3: Complete Trip Planning**
*Sarah uses the integrated trip planning to design her 6-day cultural itinerary*

**Step 4: Save Trip and Get Suggestions**
```
Action: Save completed itinerary
API: POST /public-pooling/groups/{groupId}/save-trip
User Thinking: "Let me save my detailed itinerary and see final recommendations"
```

**Request:**
```json
{
  "userId": "sarah_123",
  "tripId": "trip_sarah_cultural_001",
  "tripData": {
    "tripName": "Sri Lanka Cultural Discovery Tour",
    "startDate": "2025-12-01",
    "endDate": "2025-12-06",
    "baseCity": "Colombo",
    "cities": ["Colombo", "Anuradhapura", "Polonnaruwa", "Kandy"],
    "places": [
      {
        "name": "Gangaramaya Temple",
        "city": "Colombo", 
        "type": "Cultural",
        "day": 1
      },
      {
        "name": "Ancient City of Anuradhapura",
        "city": "Anuradhapura",
        "type": "Historical",
        "day": 2
      },
      {
        "name": "Polonnaruwa Archaeological Site",
        "city": "Polonnaruwa", 
        "type": "Archaeological",
        "day": 3
      },
      {
        "name": "Temple of the Sacred Tooth Relic",
        "city": "Kandy",
        "type": "Cultural", 
        "day": 4
      }
    ],
    "itinerary": [
      {
        "day": 1,
        "date": "2025-12-01",
        "city": "Colombo",
        "activities": [
          "Arrival and hotel check-in",
          "Gangaramaya Temple visit",
          "Colombo National Museum",
          "Pettah Market cultural walk"
        ]
      },
      {
        "day": 2, 
        "date": "2025-12-02",
        "city": "Anuradhapura",
        "activities": [
          "Early departure to Anuradhapura",
          "Sri Maha Bodhi Tree",
          "Ruwanwelisaya Stupa", 
          "Jetavanaramaya ruins"
        ]
      }
    ]
  }
}
```

**System Response:**
- Found 2 similar groups with 82% and 76% compatibility
- Both groups have cultural focus but different dates
- Sarah decides to proceed with her group

**Step 5: Finalize Group**
```
Action: Keep her group active
API: POST /public-pooling/groups/{groupId}/finalize
User Thinking: "My itinerary is unique enough to attract the right people"
```

---

### Scenario 2: Johnson Family Private Trip

#### Background
The Johnson family wants to organize a private Sri Lanka trip for extended family (8 people total). They need approval control and want to coordinate with family members.

#### User Journey

**Step 1: Create Private Family Group**
```
Action: Create private group
API: POST /groups/with-trip
User Thinking: "We need a private space to plan our family adventure"
```

**Request:**
```json
{
  "userId": "dad_johnson_456",
  "userEmail": "johnson.family@email.com",
  "groupName": "Johnson Family Sri Lanka Adventure",
  "tripName": "Family Discovery Tour - 3 Generations",
  "startDate": "2025-08-15",
  "endDate": "2025-08-22",
  "baseCity": "Colombo",
  "visibility": "private",
  "maxMembers": 8,
  "requiresApproval": false,
  "budgetLevel": "High",
  "preferredActivities": ["Wildlife Safari", "Beach Activities", "Cultural Tours"],
  "preferredTerrains": ["Beach", "Wildlife Parks", "Cultural Sites"],
  "additionalPreferences": {
    "familyFriendly": true,
    "elderlyFriendly": true,
    "accommodationType": "family resorts",
    "transportPreference": "private vehicles"
  }
}
```

**Step 2: Invite Family Members**
```
Action: Send invitations to extended family
API: POST /groups/{groupId}/invite (multiple calls)
User Thinking: "Let me invite everyone and get them excited about the trip"
```

**Multiple Invitation Requests:**
```json
// Invitation 1: Grandparents
{
  "userId": "dad_johnson_456",
  "invitedEmail": "grandpa.johnson@email.com",
  "message": "We're planning an amazing family trip to Sri Lanka! The kids are so excited to explore with you. We've chosen family-friendly activities and comfortable accommodations.",
  "expirationDays": 10
}

// Invitation 2: Brother's Family  
{
  "userId": "dad_johnson_456",
  "invitedEmail": "brother.johnson@email.com", 
  "message": "Family trip to Sri Lanka! Perfect chance for cousins to bond and create memories. We're thinking wildlife safaris and beach time!",
  "expirationDays": 10
}
```

**Step 3: Family Members Respond**
```
Action: Accept invitations
API: POST /groups/invitations/respond
User Thinking: (As Grandpa) "This sounds wonderful! Count us in!"
```

**Response Example:**
```json
{
  "userId": "grandpa_johnson_789", 
  "invitationId": "invite_johnson_001",
  "action": "accept",
  "message": "We're so excited! This will be our first international trip with the grandkids. Looking forward to the adventure!"
}
```

**Step 4: Plan Trip Together**
*Family uses trip planning collaboratively*

**Step 5: Manage Group Communication**
*Ongoing family coordination through the platform*

---

### Scenario 3: Alex Joins Existing Adventure Group

#### Background
Alex, a budget-conscious backpacker, wants to join an existing group for a Sri Lanka adventure. He's flexible with dates and activities but needs to keep costs low.

#### User Journey

**Step 1: Browse Available Groups**
```
Action: Search for budget-friendly groups
API: GET /groups/public/enhanced
User Thinking: "Let me find groups that match my budget and vibe"
```

**Request with Filters:**
```
GET /groups/public/enhanced?userId=alex_backpacker_789&budgetLevel=Low&preferredActivities=Hiking&preferredActivities=Beach Activities&startDate=2025-09-01&endDate=2025-09-30
```

**Frontend Experience:**
```javascript
// Alex uses filter interface
const filters = {
  budget: "Low",
  activities: ["Hiking", "Beach Activities", "Adventure Sports"],
  dateRange: "September 2025",
  groupSize: "4-8 people",
  pace: "Active"
};

const availableGroups = await PoolsApi.getEnhancedPools(filters);
const suitableGroups = availableGroups.filter(group => 
  group.budgetLevel === 'Low' && 
  group.memberCount < group.maxMembers
);
```

**Step 2: Review Group Details**
```
Action: Get comprehensive trip details
API: GET /public-pooling/trips/{tripId}/comprehensive?userId=alex_backpacker_789
User Thinking: "This group looks cool! Let me see their exact itinerary"
```

**Response Analysis:**
- Group has hiking in Ella, surfing in Arugam Bay
- Budget breakdown shows $25-30/day
- Current members seem like similar travelers
- 3 spots available in 8-person group

**Step 3: Request to Join**
```
Action: Submit join request
API: POST /groups/{groupId}/join
User Thinking: "Perfect fit! I hope they accept me"
```

**Request:**
```json
{
  "userId": "alex_backpacker_789",
  "userEmail": "alex.adventure@email.com",
  "userName": "Alex Thompson",
  "message": "Hey! I'm a 22-year-old recent grad looking for adventure buddies in Sri Lanka! I'm super flexible, love hiking and surfing, and I'm great at making people laugh. I've backpacked through SE Asia and know how to travel on a budget. Would love to join your epic adventure!",
  "userProfile": {
    "age": 22,
    "interests": ["Hiking", "Surfing", "Photography", "Meeting locals"],
    "travelExperience": "Experienced backpacker",
    "languages": ["English", "Basic Spanish"],
    "funFact": "I can cook a mean pasta dinner for the group!"
  }
}
```

**Step 4: Group Members Vote**
```
Action: Current members vote on Alex's request
API: POST /groups/{groupId}/join-requests/{alex_user_id}/vote
User Thinking: (As group member) "Alex seems cool and fits our vibe!"
```

**Voting Requests:**
```json
// Member 1 votes
{
  "userId": "member_1_id",
  "approved": true,
  "comment": "Alex sounds awesome! Love that he can cook and has backpacking experience."
}

// Member 2 votes
{
  "userId": "member_2_id", 
  "approved": true,
  "comment": "Perfect addition to our group. His energy and humor would be great!"
}
```

**Step 5: Alex Gets Accepted**
- All members vote to approve
- Alex receives notification
- He's now part of the adventure group
- Group planning continues with Alex included

---

### Scenario 4: University Friends Group Decision Making

#### Background
Four college friends want to plan their graduation trip to Sri Lanka. They want everyone to have input on decisions and vote on group choices.

#### User Journey

**Step 1: One Friend Creates Group**
```
Action: Create group for friend coordination
API: POST /groups/with-trip
User Thinking: "Let me set up our group so we can plan together"
```

**Step 2: Invite Friends**
```
Action: Invite each friend
API: POST /groups/{groupId}/invite (3 invitations)
User Thinking: "Getting the gang together for our epic grad trip!"
```

**Step 3: Trip Planning with Voting**
*Friends collaborate on trip planning*

**Step 4: External Join Requests Come In**
```
Action: Other travelers request to join
API: POST /groups/{groupId}/join
User Thinking: (As requester) "This group looks fun! Hope they'll let me join!"
```

**Step 5: Friends Vote on New Members**
```
Action: All friends participate in voting
API: GET /groups/my-pending-requests?userId={friend_id}
API: POST /groups/{groupId}/join-requests/{requester_id}/vote
User Thinking: "Let's discuss if this person would fit our group dynamic"
```

**Voting Coordination:**
```javascript
// Each friend sees pending requests
const pendingRequests = await PoolsApi.getMyPendingRequests(friendId);

// Group chat discussion happens externally
// Then each friend votes
await PoolsApi.voteOnJoinRequestNew(groupId, requesterId, {
  userId: friendId,
  approved: groupDecision,
  comment: "We discussed as a group and think you'd be a great fit!"
});
```

---

## 🔄 Cross-Scenario Interactions

### Scenario: Sarah's Group Gets Join Requests

**Background**: After Sarah finalizes her cultural group, Alex and others discover it and want to join.

#### User Flow

**Alex discovers Sarah's group:**
```
GET /groups/public/enhanced?preferredActivities=Cultural Tours
```

**Alex requests to join:**
```json
{
  "userId": "alex_backpacker_789",
  "message": "I know I'm more of a budget traveler, but I'm really passionate about Buddhist culture and photography. I promise I can keep up with the cultural focus and would love to learn from the group!"
}
```

**Sarah manages requests:**
```
GET /groups/my-pending-requests?userId=sarah_123
```

**Sarah's decision process:**
- Reviews Alex's profile and message
- Considers group dynamics and cultural focus
- Votes based on cultural alignment vs budget concerns

---

## 🎯 Edge Case Scenarios

### Scenario: Group Becomes Full During Join Process

**Background**: Multiple users try to join the same group simultaneously, but only one spot remains.

#### System Behavior
1. First approved request gets the spot
2. Subsequent approvals receive "group full" message
3. Users are notified and offered similar group suggestions

### Scenario: Group Creator Leaves Before Trip

**Background**: Sarah creates a group but needs to leave before the trip happens.

#### System Behavior
1. Leadership transfer to another member
2. Group continues with new organizer
3. All planning and bookings remain intact

### Scenario: Last-Minute Trip Changes

**Background**: Group needs to change dates or destinations after finalization.

#### User Flow
1. Group discussion through external channels
2. Creator updates trip details
3. Members vote on major changes
4. System updates group compatibility scores

---

## 📊 Success Metrics per Scenario

### Sarah's Cultural Journey
- **Time to Group Creation**: < 10 minutes
- **Compatibility Match Accuracy**: > 80%
- **Group Fill Rate**: 4/6 members within 2 weeks
- **Trip Completion Rate**: 95%

### Johnson Family Trip
- **Invitation Response Rate**: 100% (family)
- **Planning Collaboration**: 8 family members active
- **Satisfaction Score**: 9.5/10
- **Repeat Usage**: 70% plan another trip

### Alex's Adventure
- **Search to Join Time**: < 30 minutes
- **Approval Rate**: 75% for quality requests
- **Group Integration**: 90% complete trip
- **Cost Savings**: 40% vs solo travel

### University Friends
- **Group Formation**: < 5 minutes
- **Decision Making**: 24-48 hours per major choice
- **Member Addition**: 2-3 external members typically
- **Group Satisfaction**: 8.5/10

---

## 🛠️ Testing Each Scenario

### Test Data Requirements

**Users:**
- Sarah: Cultural enthusiast, medium budget
- Johnson Family: 8 family members, various ages
- Alex: Budget backpacker, flexible
- University Friends: 4 friends, similar ages

**Groups:**
- 5-10 existing public groups (various themes)
- 2-3 private family groups
- Cultural/adventure/beach/budget focused groups

**Time Periods:**
- Multiple future date ranges
- Overlapping and non-overlapping trips
- Various trip durations (3-14 days)

### Scenario Test Scripts

Each scenario can be automated using the Postman collection with specific test data sets and user personas. The scripts validate not just technical functionality but also business logic and user experience flows.

---

## 📱 Scenario 6: User's Complete Group Management

### Background
**Persona**: David - Active traveler who creates groups AND joins other people's groups
**Story**: David wants to see all his travel groups in one place - both groups he created and groups he joined as a participant

### User Journey
**Step 1: View My Groups Dashboard**
*David checks his complete group overview*

```
Action: Get all user's groups (created + participant)
API: GET /groups/created-by/{userId}
User Thinking: "I want to see all my upcoming trips - both ones I organized and ones I joined"
```

**Expected Response:**
```json
[
  {
    "id": "group_david_created_001",
    "groupName": "Beach Hopping Adventure",
    "creatorUserId": "david_456",
    "userIds": ["david_456", "friend1", "friend2"],
    "isCreator": true,
    "visibility": "private",
    "status": "active",
    "startDate": "2025-11-15",
    "endDate": "2025-11-20",
    "memberCount": 3,
    "maxMembers": 5
  },
  {
    "id": "group_sarah_cultural_001", 
    "groupName": "Sri Lanka Cultural Discovery",
    "creatorUserId": "sarah_123",
    "userIds": ["sarah_123", "david_456", "mike_789"],
    "isCreator": false,
    "visibility": "public",
    "status": "active", 
    "startDate": "2025-12-01",
    "endDate": "2025-12-06",
    "memberCount": 3,
    "maxMembers": 6
  }
]
```

**Test Validations:**
1. ✅ Returns both created groups AND participant groups
2. ✅ Each group shows user's role (creator vs participant)
3. ✅ Groups are properly sorted by date
4. ✅ No duplicate groups appear
5. ✅ All groups contain the user in either creatorUserId or userIds array

**Frontend Benefits:**
- **Unified Dashboard**: Single view of all user's travel involvement
- **Role-based Actions**: Different UI for created vs joined groups
- **Complete Context**: User sees their full travel schedule

This comprehensive scenario testing ensures the pooling service works for real users in actual travel planning situations, not just technical API validation.
