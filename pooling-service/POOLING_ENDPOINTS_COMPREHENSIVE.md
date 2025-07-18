# Comprehensive Pooling Service Endpoints

This document provides a detailed list of all endpoints in the Pooling Service, including their full URLs, example requests, responses, and all possible variations.

## Public Pooling Endpoints

### 1. Pre-check Compatible Groups
- **URL**: `POST /v1/public-pooling/pre-check`
- **Description**: Pre-checks for compatible public groups before creating a new one.
- **Request**:
  ```json
  {
    "userId": "user_123",
    "baseCity": "Colombo",
    "startDate": "2025-07-20",
    "endDate": "2025-07-25",
    "budgetLevel": "Medium",
    "preferredActivities": ["Hiking", "Cultural Tours"],
    "preferredTerrains": ["Beach", "Mountain"],
    "activityPacing": "Normal",
    "multiCityAllowed": true
  }
  ```
- **Response**:
  ```json
  {
    "status": "success",
    "compatibleGroups": [
      {
        "groupId": "group_001",
        "groupName": "Adventure Seekers",
        "baseCity": "Colombo"
      }
    ]
  }
  ```

### 2. Create a New Public Pooling Group
- **URL**: `POST /v1/public-pooling/groups`
- **Description**: Creates a new public pooling group.
- **Request**:
  ```json
  {
    "userId": "user_123",
    "groupName": "Beach Lovers",
    "baseCity": "Galle",
    "startDate": "2025-07-20",
    "endDate": "2025-07-25"
  }
  ```
- **Response**:
  ```json
  {
    "status": "success",
    "groupId": "group_002",
    "message": "Group created successfully"
  }
  ```

### 3. Save Trip and Get Suggestions
- **URL**: `POST /v1/public-pooling/groups/{groupId}/save-trip`
- **Description**: Saves a trip and gets suggestions for similar public pooling groups.
- **Request**:
  ```json
  {
    "userId": "user_123",
    "tripDetails": {
      "tripName": "Galle Adventure",
      "places": ["Beach", "Fort"]
    },
    "optionalField": "value" // Optional field example
  }
  ```
- **Response**:
  ```json
  {
    "status": "success",
    "suggestions": [
      {
        "groupId": "group_003",
        "groupName": "History Buffs",
        "baseCity": "Galle"
      }
    ]
  }
  ```

### 4. Join an Existing Public Pooling Group
- **URL**: `POST /v1/public-pooling/groups/{groupId}/join-existing`
- **Description**: Joins an existing public pooling group.
- **Request**:
  ```json
  {
    "userId": "user_123",
    "targetGroupId": "group_003",
    "optionalField": "value" // Optional field example
  }
  ```
- **Response**:
  ```json
  {
    "status": "success",
    "message": "Joined group successfully"
  }
  ```

### 5. Finalize a Public Pooling Group
- **URL**: `POST /v1/public-pooling/groups/{groupId}/finalize`
- **Description**: Finalizes a public pooling group.
- **Request Variations**:
  1. **Action: Finalize**
     ```json
     {
       "userId": "user_123",
       "action": "finalize"
     }
     ```
  2. **Action: Cancel**
     ```json
     {
       "userId": "user_123",
       "action": "cancel",
       "reason": "User changed plans" // Optional field
     }
     ```
- **Response**:
  ```json
  {
    "status": "success",
    "message": "Group finalized successfully"
  }
  ```

### 6. Get Compatible Groups for a Trip
- **URL**: `GET /v1/public-pooling/groups/compatible/{tripId}`
- **Description**: Gets compatible public pooling groups for a trip.
- **Request Parameters**:
  - `tripId`: `trip_001`
  - `userId`: `user_123`
- **Response**:
  ```json
  [
    {
      "groupId": "group_004",
      "groupName": "Nature Enthusiasts",
      "baseCity": "Kandy"
    }
  ]
  ```

### 7. Health Check
- **URL**: `GET /v1/public-pooling/health`
- **Description**: Health check endpoint for the public pooling service.
- **Response**:
  ```json
  {
    "status": "ok",
    "service": "public-pooling-service"
  }
  ```

## Group Management Endpoints

### 1. Create a New Travel Group
- **URL**: `POST /v1/groups`
- **Description**: Creates a new travel group.
- **Request**:
  ```json
  {
    "userId": "user_123",
    "groupName": "Mountain Explorers",
    "tripId": "trip_001",
    "visibility": "private",
    "preferences": {
      "maxMembers": 6,
      "ageRange": "25-35"
    }
  }
  ```
- **Response**:
  ```json
  {
    "status": "success",
    "groupId": "group_005",
    "tripId": "trip_001",
    "message": "Group created successfully"
  }
  ```

### 2. Create a Group with Trip Planning
- **URL**: `POST /v1/groups/with-trip`
- **Description**: Creates a new public pooling group with trip planning.
- **Request**:
  ```json
  {
    "userId": "user_123",
    "groupName": "Cultural Tour",
    "tripDetails": {
      "tripName": "Cultural Triangle",
      "places": ["Anuradhapura", "Polonnaruwa"]
    }
  }
  ```
- **Response**:
  ```json
  {
    "status": "success",
    "groupId": "group_006",
    "message": "Group with trip created successfully"
  }
  ```

### 3. Get Trip Suggestions for a Group
- **URL**: `GET /v1/groups/{groupId}/trip-suggestions`
- **Description**: Gets trip suggestions for a group based on compatibility.
- **Request Parameters**:
  - `groupId`: `group_005`
  - `userId`: `user_123`
- **Response**:
  ```json
  {
    "status": "success",
    "suggestions": [
      {
        "tripId": "trip_002",
        "tripName": "Hill Country Adventure"
      }
    ]
  }
  ```

### 4. Finalize a Trip
- **URL**: `POST /v1/groups/{groupId}/finalize-trip`
- **Description**: Finalizes a trip or joins an existing group.
- **Request Variations**:
  1. **Action: Finalize**
     ```json
     {
       "userId": "user_123",
       "action": "finalize"
     }
     ```
  2. **Action: Join Existing**
     ```json
     {
       "userId": "user_123",
       "action": "join",
       "targetGroupId": "group_007"
     }
     ```
  3. **Action: Check Suggestions**
     ```json
     {
       "userId": "user_123",
       "action": "checkSuggestions"
     }
     ```
- **Response**:
  ```json
  {
    "status": "success",
    "message": "Trip finalized successfully"
  }
  ```

### 5. Invite a User to a Group
- **URL**: `POST /v1/groups/{groupId}/invite`
- **Description**: Invites a user to a private group.
- **Request Variations**:
  1. **Invite by User ID**
     ```json
     {
       "userId": "user_123",
       "invitedUserId": "user_456",
       "message": "Join our amazing trip!",
       "expirationDays": 7
     }
     ```
  2. **Invite by Email**
     ```json
     {
       "userId": "user_123",
       "invitedEmail": "friend@example.com",
       "message": "Join our amazing trip!",
       "expirationDays": 7
     }
     ```
- **Response**:
  ```json
  {
    "status": "success",
    "message": "User invited successfully"
  }
  ```

### 6. Request to Join a Public Group
- **URL**: `POST /v1/groups/{groupId}/join`
- **Description**: Requests to join a public group. **Note: All existing group members must approve the new member before they can join.**
- **Request**:
  ```json
  {
    "userId": "user_123",
    "userEmail": "user123@example.com",
    "userName": "John Doe",
    "message": "I would love to join your trip! I have experience in hiking and cultural tours.",
    "userProfile": {
      "age": 28,
      "interests": ["hiking", "photography"],
      "experience": "intermediate"
    }
  }
  ```
- **Response**:
  ```json
  {
    "status": "pending",
    "groupId": "group_005",
    "message": "Join request submitted successfully. Waiting for approval from all group members."
  }
  ```

### 7. Get Group Details
- **URL**: `GET /v1/groups/{groupId}`
- **Description**: Gets group details.
- **Request Parameters**:
  - `groupId`: `group_005`
  - `userId`: `user_123`
- **Response**:
  ```json
  {
    "status": "success",
    "groupDetails": {
      "groupId": "group_005",
      "groupName": "Mountain Explorers",
      "members": [
        {
          "userId": "user_123",
          "userName": "John Doe",
          "role": "creator"
        }
      ],
      "tripId": "trip_001",
      "visibility": "private"
    }
  }
  ```

### 8. Get Public Groups (with filters)
- **URL**: `GET /v1/groups/public`
- **Description**: Gets list of public groups with optional filtering.
- **Request Parameters**:
  - `userId`: `user_123` (required)
  - `baseCity`: `Colombo` (optional)
  - `startDate`: `2025-07-20` (optional)
  - `endDate`: `2025-07-25` (optional)
  - `budgetLevel`: `Medium` (optional)
  - `preferredActivities`: `["Hiking", "Cultural Tours"]` (optional)
- **Response**:
  ```json
  [
    {
      "groupId": "group_008",
      "groupName": "Beach Enthusiasts",
      "baseCity": "Colombo",
      "memberCount": 3,
      "maxMembers": 6
    }
  ]
  ```

### 9. Respond to Invitation
- **URL**: `POST /v1/groups/invitations/respond`
- **Description**: Responds to an invitation (accept or reject).
- **Request Variations**:
  1. **Accept Invitation**
     ```json
     {
       "userId": "user_456",
       "invitationId": "inv_001",
       "action": "accept"
     }
     ```
  2. **Reject Invitation**
     ```json
     {
       "userId": "user_456",
       "invitationId": "inv_001",
       "action": "reject",
       "message": "Sorry, I have other plans."
     }
     ```
- **Response**:
  ```json
  {
    "status": "success",
    "message": "Invitation accepted successfully"
  }
  ```

### 10. Approve/Reject Join Request (Legacy - Single Admin)
- **URL**: `POST /v1/groups/{groupId}/requests/approve`
- **Description**: Approves or rejects a join request. **Note: This is now a legacy endpoint. The new multi-member voting system is preferred.**
- **Request Variations**:
  1. **Approve Request**
     ```json
     {
       "userId": "user_123",
       "joinRequestId": "req_001",
       "action": "approve"
     }
     ```
  2. **Reject Request**
     ```json
     {
       "userId": "user_123",
       "joinRequestId": "req_001",
       "action": "reject",
       "reason": "Group is already full"
     }
     ```
- **Response**:
  ```json
  {
    "status": "success",
    "groupId": "group_005",
    "message": "Join request approved successfully"
  }
  ```

### 11. Vote on Join Request (Multi-Member Approval)
- **URL**: `POST /v1/groups/{groupId}/join-requests/vote`
- **Description**: Allows a group member to vote on a join request. **All group members must approve for the request to be accepted.**
- **Request Variations**:
  1. **Approve Vote**
     ```json
     {
       "userId": "user_123",
       "joinRequestId": "req_001",
       "action": "approve"
     }
     ```
  2. **Reject Vote**
     ```json
     {
       "userId": "user_123",
       "joinRequestId": "req_001",
       "action": "reject",
       "reason": "Concerned about group compatibility"
     }
     ```
- **Response Variations**:
  1. **Still Pending More Votes**
     ```json
     {
       "status": "success",
       "groupId": "group_005",
       "joinRequestId": "req_001",
       "message": "Your vote recorded. Waiting for votes from remaining members.",
       "requestStatus": "pending",
       "pendingMembers": ["user_456", "user_789"],
       "totalVotesReceived": 1,
       "totalMembersRequired": 3
     }
     ```
  2. **All Members Approved**
     ```json
     {
       "status": "success",
       "groupId": "group_005",
       "joinRequestId": "req_001",
       "message": "Join request approved by all members. User has been added to the group.",
       "requestStatus": "approved",
       "pendingMembers": [],
       "totalVotesReceived": 3,
       "totalMembersRequired": 3
     }
     ```
  3. **Request Rejected**
     ```json
     {
       "status": "success",
       "groupId": "group_005",
       "joinRequestId": "req_001",
       "message": "Join request rejected",
       "requestStatus": "rejected",
       "pendingMembers": [],
       "totalVotesReceived": 2,
       "totalMembersRequired": 3
     }
     ```

### 12. Get Pending Join Requests
- **URL**: `GET /v1/groups/{groupId}/join-requests/pending`
- **Description**: Gets pending join requests for a group that require member votes.
- **Request Parameters**:
  - `groupId`: `group_005`
  - `userId`: `user_123`
- **Response**:
  ```json
  {
    "status": "success",
    "pendingRequests": [
      {
        "joinRequestId": "req_001",
        "userId": "user_999",
        "userName": "Alice Johnson",
        "userEmail": "alice@example.com",
        "message": "I'd love to join your adventure trip!",
        "requestedAt": "2025-07-18T10:00:00Z",
        "pendingMembers": ["user_456", "user_789"],
        "totalVotesReceived": 1,
        "totalMembersRequired": 3,
        "hasCurrentUserVoted": true
      }
    ]
  }
  ```

### 13. Get User Invitations
- **URL**: `GET /v1/groups/invitations/{userId}`
- **Description**: Gets pending invitations for a user.
- **Request Parameters**:
  - `userId`: `user_456`
- **Response**:
  ```json
  {
    "status": "success",
    "invitations": [
      {
        "invitationId": "inv_002",
        "groupId": "group_009",
        "groupName": "Adventure Seekers",
        "inviterName": "Jane Smith",
        "message": "Join our hiking trip!",
        "expiresAt": "2025-07-25T10:00:00Z"
      }
    ]
  }
  ```

### 14. Health Check
- **URL**: `GET /v1/groups/health`
- **Description**: Health check endpoint for the group service.
- **Response**:
  ```json
  {
    "status": "ok",
    "service": "group-service"
  }
  ```
