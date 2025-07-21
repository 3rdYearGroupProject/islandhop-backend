# Pooling Service Endpoints

This document provides a comprehensive list of all endpoints in the Pooling Service, including their full URLs, example requests, and responses.

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
    "endDate": "2025-07-25"
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
    }
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
    "targetGroupId": "group_003"
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
- **Request**:
  ```json
  {
    "userId": "user_123"
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
    "baseCity": "Nuwara Eliya"
  }
  ```
- **Response**:
  ```json
  {
    "status": "success",
    "groupId": "group_005",
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
- **Request**:
  ```json
  {
    "userId": "user_123",
    "action": "finalize"
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
- **Request**:
  ```json
  {
    "userId": "user_123",
    "inviteeId": "user_456"
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
- **Description**: Requests to join a public group.
- **Request**:
  ```json
  {
    "userId": "user_123"
  }
  ```
- **Response**:
  ```json
  {
    "status": "success",
    "message": "Join request submitted successfully"
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
      "groupName": "Mountain Explorers"
    }
  }
  ```

### 8. Health Check
- **URL**: `GET /v1/groups/health`
- **Description**: Health check endpoint for the group service.
- **Response**:
  ```json
  {
    "status": "ok",
    "service": "group-service"
  }
  ```
