# Chat and Notification Microservice - Frontend Integration Guide

## Overview

This guide provides comprehensive documentation for integrating with the Chat and Notification Microservice REST APIs. The service provides real-time messaging capabilities including personal chat, group chat, and notifications.

## Base URL

```
http://localhost:8083/api/v1
```

## Authentication

All endpoints require proper authentication. Include the user's authentication token in the request headers:

```
Authorization: Bearer <your-jwt-token>
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

**Frontend Integration Example:**

```javascript
async function sendPersonalMessage(senderId, receiverId, content) {
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

#### 2. Get Message History

**Endpoint:** `GET /chat/personal/messages`

**Parameters:**

- `senderId` (required): The sender's ID
- `receiverId` (required): The receiver's ID
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response:**

```json
{
  "content": [
    {
      "id": "msg_123456789",
      "senderId": "user123",
      "receiverId": "user456",
      "content": "Hello! How are you?",
      "messageType": "TEXT",
      "timestamp": "2024-01-15T10:30:00Z",
      "isRead": true,
      "isDelivered": true
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

**Frontend Integration Example:**

```javascript
async function getMessageHistory(senderId, receiverId, page = 0, size = 20) {
  const response = await fetch(
    `/api/v1/chat/personal/messages?senderId=${senderId}&receiverId=${receiverId}&page=${page}&size=${size}`,
    {
      headers: {
        Authorization: `Bearer ${authToken}`,
      },
    }
  );

  return await response.json();
}
```

#### 3. Get User Conversations

**Endpoint:** `GET /chat/personal/conversations/{userId}`

**Response:**

```json
["user456", "user789", "user101"]
```

#### 4. Mark Messages as Read

**Endpoint:** `PUT /chat/personal/mark-read`

**Parameters:**

- `senderId` (required): The sender's ID
- `receiverId` (required): The receiver's ID

**Response:**

```json
"Messages marked as read"
```

#### 5. Get Unread Message Count

**Endpoint:** `GET /chat/personal/unread-count/{userId}`

**Response:**

```json
15
```

#### 6. Search Messages

**Endpoint:** `GET /chat/personal/search`

**Parameters:**

- `senderId` (required): The sender's ID
- `receiverId` (required): The receiver's ID
- `searchTerm` (required): The search term
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

### Group Chat APIs

#### 1. Create Group

**Endpoint:** `POST /chat/group/create`

**Request Body:**

```json
{
  "groupName": "Travel Buddies",
  "description": "Planning our next adventure",
  "groupType": "PRIVATE",
  "createdBy": "user123",
  "members": ["user456", "user789"]
}
```

**Response:**

```json
{
  "id": "group_123456789",
  "groupName": "Travel Buddies",
  "description": "Planning our next adventure",
  "groupType": "PRIVATE",
  "createdBy": "user123",
  "members": ["user123", "user456", "user789"],
  "createdAt": "2024-01-15T10:30:00Z",
  "isActive": true
}
```

**Frontend Integration Example:**

```javascript
async function createGroup(groupData) {
  const response = await fetch("/api/v1/chat/group/create", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${authToken}`,
    },
    body: JSON.stringify(groupData),
  });

  return await response.json();
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
  "messageType": "TEXT"
}
```

#### 3. Get Group Messages

**Endpoint:** `GET /chat/group/{groupId}/messages`

**Parameters:**

- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

#### 4. Add Member to Group

**Endpoint:** `POST /chat/group/add-member`

**Request Body:**

```json
{
  "groupId": "group_123456789",
  "userId": "user999",
  "addedBy": "user123"
}
```

#### 5. Get User Groups

**Endpoint:** `GET /chat/group/user/{userId}`

**Response:**

```json
[
  {
    "id": "group_123456789",
    "groupName": "Travel Buddies",
    "description": "Planning our next adventure",
    "groupType": "PRIVATE",
    "createdBy": "user123",
    "members": ["user123", "user456", "user789"],
    "createdAt": "2024-01-15T10:30:00Z",
    "isActive": true
  }
]
```

### Notification APIs

#### 1. Send Notification

**Endpoint:** `POST /notifications/send`

**Request Body:**

```json
{
  "userId": "user123",
  "title": "New Message",
  "message": "You have received a new message from John",
  "type": "MESSAGE",
  "priority": "MEDIUM",
  "relatedEntityId": "msg_123456789"
}
```

**Response:**

```json
{
  "id": 1,
  "userId": "user123",
  "title": "New Message",
  "message": "You have received a new message from John",
  "type": "MESSAGE",
  "priority": "MEDIUM",
  "relatedEntityId": "msg_123456789",
  "isRead": false,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### 2. Get User Notifications

**Endpoint:** `GET /notifications/user/{userId}`

**Response:**

```json
[
  {
    "id": 1,
    "userId": "user123",
    "title": "New Message",
    "message": "You have received a new message from John",
    "type": "MESSAGE",
    "priority": "MEDIUM",
    "isRead": false,
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

#### 3. Get Unread Notifications

**Endpoint:** `GET /notifications/user/{userId}/unread`

#### 4. Mark Notification as Read

**Endpoint:** `PUT /notifications/{notificationId}/read`

#### 5. Get Unread Notification Count

**Endpoint:** `GET /notifications/user/{userId}/unread-count`

**Response:**

```json
5
```

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
