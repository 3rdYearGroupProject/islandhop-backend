# Chat and Notification Microservice - Frontend Integration Guide

## Overview

This guide provides comprehensive documentation for integrating with the Chat and Notification Microservice REST APIs. The service provides real-time messaging capabilities including personal chat, group chat, and notifications with Firebase Authentication.

## Base URL

```
http://localhost:8083/api/v1
```

## Firebase Authentication Setup

### Backend Configuration

1. **Add Firebase Service Account:**

   - Download your Firebase service account key from Firebase Console
   - Save it as `firebase-service-account.json` in `src/main/resources/`
   - Update `application.properties` with your Firebase project ID:

   ```properties
   firebase.project.id=your-firebase-project-id
   firebase.config.path=firebase-service-account.json
   ```

2. **Environment Variables (Alternative):**
   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/firebase-service-account.json
   export FIREBASE_PROJECT_ID=your-firebase-project-id
   ```

### Frontend Integration

Your existing Firebase configuration should work seamlessly:

```javascript
// Your existing firebase.js is compatible
import { auth } from "./firebase";
import { signInWithEmailAndPassword, onAuthStateChanged } from "firebase/auth";

// Authentication state listener
onAuthStateChanged(auth, async (user) => {
  if (user) {
    // User is signed in, get ID token for API calls
    const idToken = await user.getIdToken();
    // Store token for API requests
    localStorage.setItem("firebaseToken", idToken);
  } else {
    // User is signed out
    localStorage.removeItem("firebaseToken");
  }
});
```

## Authentication

This microservice uses **Firebase Authentication** for securing API endpoints. All requests must include a valid Firebase ID token in the Authorization header.

### Getting Firebase ID Token

On your frontend, after a user signs in with Firebase Auth, get the ID token:

```javascript
import { auth } from "./firebase"; // Your Firebase config
import { getUserData } from "./utils/userStorage"; // Your user storage

// Get current user's ID token
async function getAuthToken() {
  const user = auth.currentUser;
  if (user) {
    try {
      const idToken = await user.getIdToken();
      return idToken;
    } catch (error) {
      console.error("Error getting ID token:", error);
      return null;
    }
  }
  return null;
}

// Alternative: Get token from your user storage if available
function getStoredAuthToken() {
  const userData = getUserData();
  return userData ? userData.accessToken : null;
}
```

### Request Headers

Include the Firebase ID token in the Authorization header for all API requests:

```
Authorization: Bearer <firebase-id-token>
```

### Frontend Integration with Authentication

```javascript
// Enhanced function with Firebase authentication
async function sendPersonalMessage(senderId, receiverId, content) {
  // Get Firebase ID token
  const authToken = await getAuthToken();

  if (!authToken) {
    throw new Error("User not authenticated");
  }

  const response = await fetch("/api/v1/chat/personal/send", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${authToken}`, // Firebase ID token
    },
    body: JSON.stringify({
      senderId,
      receiverId,
      content,
      messageType: "TEXT",
    }),
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error("Authentication failed - please sign in again");
    }
    throw new Error("Failed to send message");
  }

  return await response.json();
}
```

## API Endpoints

### Personal Chat APIs

#### 1. Send Personal Message

**Endpoint:** `POST /chat/personal/send`

**Description:** Send a personal message between two users

**Request Body:**

```json
{
  "senderId": "user123",
  "receiverId": "user456",
  "content": "Hello! How are you?",
  "messageType": "TEXT"
}
```

**Response:**

```json
{
  "id": "msg_123456789",
  "senderId": "user123",
  "receiverId": "user456",
  "content": "Hello! How are you?",
  "messageType": "TEXT",
  "timestamp": "2024-01-15T10:30:00Z",
  "isRead": false,
  "isDelivered": true
}
```

#### 2. Get Message History with Pagination

**Endpoint:** `GET /chat/personal/messages`

**Query Parameters:**

- `senderId` (required): First user's ID
- `receiverId` (required): Second user's ID
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size

**Example:** `/chat/personal/messages?senderId=user123&receiverId=user456&page=0&size=20`

#### 3. Get User Conversations

**Endpoint:** `GET /chat/personal/conversations/{userId}`

**Description:** Get all conversation partners and their latest messages for a user

**Response:**

```json
[
  {
    "partnerId": "user456",
    "latestMessage": {
      "id": "msg_123",
      "content": "Hey there!",
      "timestamp": "2024-01-15T10:30:00Z"
    },
    "unreadCount": 3
  }
]
```

#### 4. Get Recent Messages

**Endpoint:** `GET /chat/personal/recent/{userId}`

**Query Parameters:**

- `limit` (optional, default: 10): Number of recent messages to retrieve

#### 5. Mark Messages as Read

**Endpoint:** `PUT /chat/personal/mark-read`

**Query Parameters:**

- `senderId` (required): Sender's user ID
- `receiverId` (required): Receiver's user ID

#### 6. Get Unread Message Count

**Endpoint:** `GET /chat/personal/unread-count/{userId}`

**Response:**

```json
{
  "count": 5
}
```

#### 7. Search Messages

**Endpoint:** `GET /chat/personal/search`

**Query Parameters:**

- `senderId` (required): First user's ID
- `receiverId` (required): Second user's ID
- `searchTerm` (required): Text to search for
- `page` (optional): Page number
- `size` (optional): Page size

#### 8. Delete Message

**Endpoint:** `DELETE /chat/personal/message/{messageId}`

**Description:** Delete a specific message by ID

#### 9. Health Check

**Endpoint:** `GET /chat/personal/health`

**Response:**

```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Group Chat APIs

#### 1. Create Group

**Endpoint:** `POST /chat/group/create`

**Request Body:**

```json
{
  "groupName": "Travel Buddies",
  "description": "Planning our next adventure",
  "groupType": "PRIVATE",
  "memberIds": ["user456", "user789"],
  "adminId": "user123"
}
```

#### 2. Send Group Message

**Endpoint:** `POST /chat/group/send`

**Request Body:**

```json
{
  "groupId": "group_123456789",
  "senderId": "user123",
  "content": "Hey everyone! Ready for the trip?",
  "messageType": "TEXT",
  "senderName": "John Doe"
}
```

#### 3. Get Group Messages

**Endpoint:** `GET /chat/group/{groupId}/messages`

**Query Parameters:**

- `page` (optional): Page number
- `size` (optional): Page size

#### 4. Add Member to Group

**Endpoint:** `POST /chat/group/add-member`

**Request Body:**

```json
{
  "groupId": "group_123456789",
  "userId": "user999",
  "requesterId": "user123"
}
```

#### 5. Remove Member from Group

**Endpoint:** `DELETE /chat/group/{groupId}/member/{userId}`

**Query Parameters:**

- `removedBy` (required): ID of user performing the removal

#### 6. Get User Groups

**Endpoint:** `GET /chat/group/user/{userId}`

**Description:** Get all groups that a user belongs to

#### 7. Get Group Details

**Endpoint:** `GET /chat/group/{groupId}`

**Description:** Get detailed information about a specific group

#### 8. Update Group

**Endpoint:** `PUT /chat/group/{groupId}`

**Request Body:**

```json
{
  "groupName": "Updated Group Name",
  "description": "Updated description",
  "groupType": "PRIVATE"
}
```

#### 9. Delete Group

**Endpoint:** `DELETE /chat/group/{groupId}`

**Query Parameters:**

- `requesterId` (required): ID of user requesting deletion (must be admin)

#### 10. Search Group Messages

**Endpoint:** `GET /chat/group/{groupId}/search`

**Query Parameters:**

- `searchTerm` (required): Text to search for
- `page` (optional): Page number
- `size` (optional): Page size

#### 11. Get Group Members

**Endpoint:** `GET /chat/group/{groupId}/members`

**Response:**

```json
["user123", "user456", "user789"]
```

#### 12. Leave Group

**Endpoint:** `POST /chat/group/{groupId}/leave`

**Query Parameters:**

- `userId` (required): ID of user leaving the group

#### 13. Group Health Check

**Endpoint:** `GET /chat/group/health`

### Notification APIs

#### 1. Send Notification

**Endpoint:** `POST /notifications/send`

**Request Body:**

```json
{
  "userId": "user123",
  "title": "New Message",
  "message": "You have received a new message from John",
  "type": "CHAT",
  "priority": "MEDIUM",
  "relatedEntityId": "msg_123456789"
}
```

#### 2. Get User Notifications

**Endpoint:** `GET /notifications/user/{userId}`

**Description:** Get all notifications for a user

#### 3. Get Unread Notifications

**Endpoint:** `GET /notifications/user/{userId}/unread`

**Description:** Get only unread notifications for a user

#### 4. Mark Notification as Read

**Endpoint:** `PUT /notifications/{notificationId}/read`

**Description:** Mark a specific notification as read

#### 5. Mark All Notifications as Read

**Endpoint:** `PUT /notifications/user/{userId}/read-all`

**Description:** Mark all notifications for a user as read

#### 6. Delete Notification

**Endpoint:** `DELETE /notifications/{notificationId}`

**Description:** Delete a specific notification

#### 7. Get Unread Notification Count

**Endpoint:** `GET /notifications/user/{userId}/unread-count`

**Response:**

```json
{
  "count": 8
}
```

#### 8. Get Notifications by Type

**Endpoint:** `GET /notifications/user/{userId}/type/{type}`

**Description:** Get notifications of a specific type (CHAT, SYSTEM, ALERT, etc.)

#### 9. Cleanup Old Notifications (Admin)

**Endpoint:** `DELETE /notifications/admin/cleanup`

**Query Parameters:**

- `daysOld` (required): Delete notifications older than specified days

#### 10. Batch Send Notifications

**Endpoint:** `POST /notifications/batch-send`

**Query Parameters:**

- `userIds` (required): Comma-separated list of user IDs

**Request Body:**

```json
{
  "title": "System Update",
  "message": "We have updated our terms of service.",
  "type": "SYSTEM",
  "priority": "HIGH"
}
```

#### 11. Notification Health Check

**Endpoint:** `GET /notifications/health`

**Frontend Integration Example:**

```javascript
async function sendPersonalMessage(senderId, receiverId, content) {
  const authToken = await getAuthToken();

  const response = await fetch("/api/v1/chat/personal/send", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${authToken}`,
    },
    body: JSON.stringify({
      senderId,
      receiverId,
      content,
      messageType: "TEXT",
    }),
  });

  return await response.json();
}
```

## Postman Integration Guide

### Authentication Setup

1. **Sign in to your frontend app and get a Firebase ID token:**

   - Open browser dev tools → Application → Local Storage
   - Copy the value of `firebaseToken` (or use your own method to get the token)
   - Alternative: Use Firebase Auth REST API to get a token

2. **In Postman, set the Authorization header for every request:**

   ```
   Key: Authorization
   Value: Bearer <your-firebase-id-token>
   ```

   **Tip:** You can set this as a collection variable:

   - Go to your collection → Variables tab
   - Add variable `authToken` with your Firebase token
   - Use `{{authToken}}` in Authorization headers

### Personal Chat API Examples

#### 1. Send Personal Message

- **Method:** POST
- **URL:** `http://localhost:8083/api/v1/chat/personal/send`
- **Headers:**
  - Content-Type: application/json
  - Authorization: Bearer {{authToken}}
- **Body (raw, JSON):**

```json
{
  "senderId": "user123",
  "receiverId": "user456",
  "content": "Hello! How are you doing today?",
  "messageType": "TEXT"
}
```

#### 2. Get Message History

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/personal/messages?senderId=user123&receiverId=user456&page=0&size=20`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 3. Get User Conversations

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/personal/conversations/user123`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 4. Get Recent Messages

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/personal/recent/user123?limit=10`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 5. Mark Messages as Read

- **Method:** PUT
- **URL:** `http://localhost:8083/api/v1/chat/personal/mark-read?senderId=user456&receiverId=user123`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 6. Get Unread Message Count

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/personal/unread-count/user123`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 7. Search Messages

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/personal/search?senderId=user123&receiverId=user456&searchTerm=hello&page=0&size=20`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 8. Delete Message

- **Method:** DELETE
- **URL:** `http://localhost:8083/api/v1/chat/personal/message/msg_123456789`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 9. Personal Chat Health Check

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/personal/health`
- **Headers:** None required

### Group Chat API Examples

#### 1. Create Group

- **Method:** POST
- **URL:** `http://localhost:8083/api/v1/chat/group/create`
- **Headers:**
  - Content-Type: application/json
  - Authorization: Bearer {{authToken}}
- **Body (raw, JSON):**

```json
{
  "groupName": "Travel Buddies Sri Lanka",
  "description": "Planning our amazing trip to Sri Lanka",
  "groupType": "PRIVATE",
  "memberIds": ["user456", "user789", "user101"],
  "adminId": "user123"
}
```

#### 2. Send Group Message

- **Method:** POST
- **URL:** `http://localhost:8083/api/v1/chat/group/send`
- **Headers:**
  - Content-Type: application/json
  - Authorization: Bearer {{authToken}}
- **Body (raw, JSON):**

```json
{
  "groupId": "group_123456789",
  "senderId": "user123",
  "content": "Hey everyone! Ready for our Sri Lanka adventure?",
  "messageType": "TEXT",
  "senderName": "John Doe"
}
```

#### 3. Get Group Messages

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/group/group_123456789/messages?page=0&size=20`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 4. Add Member to Group

- **Method:** POST
- **URL:** `http://localhost:8083/api/v1/chat/group/add-member`
- **Headers:**
  - Content-Type: application/json
  - Authorization: Bearer {{authToken}}
- **Body (raw, JSON):**

```json
{
  "groupId": "group_123456789",
  "userId": "user999",
  "requesterId": "user123"
}
```

#### 5. Remove Member from Group

- **Method:** DELETE
- **URL:** `http://localhost:8083/api/v1/chat/group/group_123456789/member/user456?removedBy=user123`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 6. Get User Groups

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/group/user/user123`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 7. Get Group Details

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/group/group_123456789`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 8. Update Group

- **Method:** PUT
- **URL:** `http://localhost:8083/api/v1/chat/group/group_123456789`
- **Headers:**
  - Content-Type: application/json
  - Authorization: Bearer {{authToken}}
- **Body (raw, JSON):**

```json
{
  "groupName": "Updated Travel Group Name",
  "description": "Updated description for our travel group",
  "groupType": "PRIVATE"
}
```

#### 9. Delete Group

- **Method:** DELETE
- **URL:** `http://localhost:8083/api/v1/chat/group/group_123456789?requesterId=user123`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 10. Search Group Messages

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/group/group_123456789/search?searchTerm=travel&page=0&size=20`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 11. Get Group Members

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/group/group_123456789/members`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 12. Leave Group

- **Method:** POST
- **URL:** `http://localhost:8083/api/v1/chat/group/group_123456789/leave?userId=user456`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 13. Group Chat Health Check

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/chat/group/health`
- **Headers:** None required

### Notification API Examples

#### 1. Send Notification

- **Method:** POST
- **URL:** `http://localhost:8083/api/v1/notifications/send`
- **Headers:**
  - Content-Type: application/json
  - Authorization: Bearer {{authToken}}
- **Body (raw, JSON):**

```json
{
  "userId": "user123",
  "title": "New Message Received",
  "message": "You have received a new message from John Doe in Travel Buddies group",
  "type": "CHAT",
  "priority": "MEDIUM",
  "relatedEntityId": "msg_123456789"
}
```

#### 2. Get User Notifications

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/notifications/user/user123`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 3. Get Unread Notifications

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/notifications/user/user123/unread`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 4. Mark Notification as Read

- **Method:** PUT
- **URL:** `http://localhost:8083/api/v1/notifications/1/read`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 5. Mark All Notifications as Read

- **Method:** PUT
- **URL:** `http://localhost:8083/api/v1/notifications/user/user123/read-all`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 6. Delete Notification

- **Method:** DELETE
- **URL:** `http://localhost:8083/api/v1/notifications/1`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 7. Get Unread Notification Count

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/notifications/user/user123/unread-count`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 8. Get Notifications by Type

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/notifications/user/user123/type/CHAT`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 9. Cleanup Old Notifications (Admin)

- **Method:** DELETE
- **URL:** `http://localhost:8083/api/v1/notifications/admin/cleanup?daysOld=30`
- **Headers:**
  - Authorization: Bearer {{authToken}}

#### 10. Batch Send Notifications

- **Method:** POST
- **URL:** `http://localhost:8083/api/v1/notifications/batch-send?userIds=user123,user456,user789`
- **Headers:**
  - Content-Type: application/json
  - Authorization: Bearer {{authToken}}
- **Body (raw, JSON):**

```json
{
  "title": "System Maintenance",
  "message": "The system will undergo maintenance from 2 AM to 4 AM tomorrow.",
  "type": "SYSTEM",
  "priority": "HIGH"
}
```

#### 11. Notification Health Check

- **Method:** GET
- **URL:** `http://localhost:8083/api/v1/notifications/health`
- **Headers:** None required

### Postman Collection Setup Tips

1. **Create Environment Variables:**

   - `baseUrl`: `http://localhost:8083/api/v1`
   - `authToken`: Your Firebase ID token
   - `userId`: Test user ID (e.g., `user123`)
   - `groupId`: Test group ID (e.g., `group_123456789`)

2. **Use Variables in Requests:**

   - URL: `{{baseUrl}}/chat/personal/send`
   - Authorization: `Bearer {{authToken}}`
   - Body: Use `{{userId}}` for user IDs

3. **Pre-request Scripts:**

   ```javascript
   // Auto-refresh Firebase token if needed
   if (!pm.environment.get("authToken")) {
     console.log("Warning: No auth token set");
   }
   ```

4. **Test Scripts:**

   ```javascript
   // Validate response
   pm.test("Status code is 200", function () {
     pm.response.to.have.status(200);
   });

   pm.test("Response has required fields", function () {
     var jsonData = pm.response.json();
     pm.expect(jsonData).to.have.property("id");
   });
   ```

**Note:** For all protected endpoints, always set the `Authorization` header with your Firebase ID token. For POST/PUT requests, set `Content-Type: application/json` and use raw JSON bodies as shown above.

## WebSocket Integration

### Connection Setup

```javascript
// Connect to WebSocket
const socket = new SockJS("/ws");
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
  console.log("Connected: " + frame);

  // Subscribe to personal messages
  stompClient.subscribe("/user/queue/messages", function (message) {
    const personalMessage = JSON.parse(message.body);
    handleNewPersonalMessage(personalMessage);
  });

  // Subscribe to group messages
  stompClient.subscribe("/topic/group/" + groupId, function (message) {
    const groupMessage = JSON.parse(message.body);
    handleNewGroupMessage(groupMessage);
  });

  // Subscribe to notifications
  stompClient.subscribe("/user/queue/notifications", function (notification) {
    const notificationData = JSON.parse(notification.body);
    handleNewNotification(notificationData);
  });
});
```

### Sending Messages via WebSocket

```javascript
// Send personal message
function sendPersonalMessageWS(senderId, receiverId, content) {
  stompClient.send(
    "/app/chat/personal",
    {},
    JSON.stringify({
      senderId: senderId,
      receiverId: receiverId,
      content: content,
      messageType: "TEXT",
    })
  );
}

// Send group message
function sendGroupMessageWS(groupId, senderId, content) {
  stompClient.send(
    "/app/chat/group",
    {},
    JSON.stringify({
      groupId: groupId,
      senderId: senderId,
      content: content,
      messageType: "TEXT",
    })
  );
}
```

## Frontend Implementation Examples

### React Component Example

```jsx
import React, { useState, useEffect } from "react";
import SockJS from "sockjs-client";
import Stomp from "stompjs";

const ChatComponent = () => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [stompClient, setStompClient] = useState(null);

  useEffect(() => {
    // Initialize WebSocket connection
    const socket = new SockJS("/ws");
    const client = Stomp.over(socket);

    client.connect({}, (frame) => {
      console.log("Connected: " + frame);
      setStompClient(client);

      // Subscribe to messages
      client.subscribe("/user/queue/messages", (message) => {
        const newMsg = JSON.parse(message.body);
        setMessages((prev) => [...prev, newMsg]);
      });
    });

    return () => {
      if (client) {
        client.disconnect();
      }
    };
  }, []);

  const sendMessage = async () => {
    const messageData = {
      senderId: currentUserId,
      receiverId: targetUserId,
      content: newMessage,
      messageType: "TEXT",
    };

    // Send via REST API
    await fetch("/api/v1/chat/personal/send", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${authToken}`,
      },
      body: JSON.stringify(messageData),
    });

    setNewMessage("");
  };

  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map((msg) => (
          <div key={msg.id} className="message">
            <strong>{msg.senderId}:</strong> {msg.content}
          </div>
        ))}
      </div>
      <div className="message-input">
        <input
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="Type a message..."
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
};
```

### Vue.js Component Example

```vue
<template>
  <div class="chat-container">
    <div class="messages">
      <div v-for="message in messages" :key="message.id" class="message">
        <strong>{{ message.senderId }}:</strong> {{ message.content }}
      </div>
    </div>
    <div class="message-input">
      <input
        v-model="newMessage"
        @keyup.enter="sendMessage"
        placeholder="Type a message..."
      />
      <button @click="sendMessage">Send</button>
    </div>
  </div>
</template>

<script>
import SockJS from "sockjs-client";
import Stomp from "stompjs";

export default {
  data() {
    return {
      messages: [],
      newMessage: "",
      stompClient: null,
    };
  },

  mounted() {
    this.connectWebSocket();
  },

  methods: {
    connectWebSocket() {
      const socket = new SockJS("/ws");
      this.stompClient = Stomp.over(socket);

      this.stompClient.connect({}, (frame) => {
        console.log("Connected: " + frame);

        this.stompClient.subscribe("/user/queue/messages", (message) => {
          const newMsg = JSON.parse(message.body);
          this.messages.push(newMsg);
        });
      });
    },

    async sendMessage() {
      const messageData = {
        senderId: this.currentUserId,
        receiverId: this.targetUserId,
        content: this.newMessage,
        messageType: "TEXT",
      };

      await fetch("/api/v1/chat/personal/send", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${this.authToken}`,
        },
        body: JSON.stringify(messageData),
      });

      this.newMessage = "";
    },
  },
};
</script>
```

## Error Handling

### Common Error Responses

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid user ID format",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Frontend Error Handling Example

```javascript
async function sendMessageWithErrorHandling(messageData) {
  try {
    const response = await fetch("/api/v1/chat/personal/send", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${authToken}`,
      },
      body: JSON.stringify(messageData),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || "Failed to send message");
    }

    return await response.json();
  } catch (error) {
    console.error("Error sending message:", error);
    // Show user-friendly error message
    showErrorNotification("Failed to send message. Please try again.");
    throw error;
  }
}
```

## Best Practices

### 1. Connection Management

- Always handle WebSocket disconnections gracefully
- Implement automatic reconnection logic
- Use heartbeat/ping-pong to maintain connection

### 2. Message Handling

- Implement message queuing for offline scenarios
- Add message delivery confirmation
- Handle duplicate messages properly

### 3. Performance Optimization

- Use pagination for message history
- Implement virtual scrolling for large message lists
- Debounce typing indicators

### 4. Security

- Validate all user inputs
- Implement proper authentication checks
- Use HTTPS for all API calls

### 5. User Experience

- Show loading states during API calls
- Implement typing indicators
- Add message status indicators (sent, delivered, read)
- Provide offline message capabilities

## Testing

### API Testing with Postman/Curl

```bash
# Send personal message
curl -X POST http://localhost:8083/api/v1/chat/personal/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d '{
    "senderId": "user123",
    "receiverId": "user456",
    "content": "Hello!",
    "messageType": "TEXT"
  }'

# Get notifications
curl -X GET http://localhost:8083/api/v1/notifications/user/user123 \
  -H "Authorization: Bearer your-token"
```

### WebSocket Testing

Use tools like:

- WebSocket clients (e.g., wscat)
- Browser developer tools
- Postman WebSocket support

This integration guide provides everything needed to successfully integrate the Chat and Notification Microservice with your frontend application.
