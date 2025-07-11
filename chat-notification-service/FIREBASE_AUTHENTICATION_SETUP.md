# Firebase Authentication Setup Guide

## Overview

This guide explains how to set up Firebase Authentication for the Chat and Notification Microservice. The backend uses Firebase Admin SDK to verify ID tokens from your frontend Firebase Authentication.

## Backend Setup

### 1. Firebase Admin SDK Configuration

The backend is already configured with Firebase Admin SDK. You need to provide your Firebase credentials.

### 2. Service Account Setup

#### Option A: Service Account File (Recommended)

1. **Download Service Account Key:**

   - Go to Firebase Console → Project Settings → Service Accounts
   - Click "Generate new private key"
   - Download the JSON file

2. **Place the File:**
   - Save as `firebase-service-account.json` in `src/main/resources/`
   - Update `application.properties`:
   ```properties
   firebase.project.id=your-actual-firebase-project-id
   firebase.config.path=firebase-service-account.json
   ```

#### Option B: Environment Variables

Set these environment variables:

```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/firebase-service-account.json
export FIREBASE_PROJECT_ID=your-firebase-project-id
```

### 3. Configuration Properties

Update `src/main/resources/application.properties`:

```properties
# Firebase Configuration
firebase.config.path=firebase-service-account.json
firebase.project.id=islandhop-12345  # Your actual project ID

# Security Configuration
security.cors.allowed-origins=http://localhost:3000,http://localhost:3001,https://your-frontend-domain.com
```

## Frontend Integration

### 1. Existing Firebase Setup

Your current Firebase configuration in `src/firebase.js` is perfect and compatible:

```javascript
// Your existing firebase.js works as-is
const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY,
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID,
  // ... other config
};
```

### 2. Getting ID Tokens for API Calls

Create a utility function to get Firebase ID tokens:

```javascript
// utils/authToken.js
import { auth } from "../firebase";

export const getFirebaseToken = async () => {
  const user = auth.currentUser;
  if (user) {
    try {
      // Get fresh token (automatically refreshes if needed)
      const idToken = await user.getIdToken(true);
      return idToken;
    } catch (error) {
      console.error("Error getting Firebase token:", error);
      throw new Error("Failed to get authentication token");
    }
  }
  throw new Error("User not authenticated");
};

// Check if user is authenticated
export const isAuthenticated = () => {
  return auth.currentUser !== null;
};

// Get current user info
export const getCurrentUser = () => {
  return auth.currentUser;
};
```

### 3. API Request Integration

Update your API calls to include Firebase tokens:

```javascript
// utils/apiClient.js
import { getFirebaseToken } from "./authToken";

class ApiClient {
  constructor() {
    this.baseURL = "http://localhost:8083/api/v1";
  }

  async request(endpoint, options = {}) {
    try {
      // Get Firebase ID token
      const token = await getFirebaseToken();

      const config = {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
          ...options.headers,
        },
        ...options,
      };

      const response = await fetch(`${this.baseURL}${endpoint}`, config);

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("Authentication failed - please sign in again");
        }
        throw new Error(`API request failed: ${response.statusText}`);
      }

      return await response.json();
    } catch (error) {
      console.error("API request error:", error);
      throw error;
    }
  }

  // Personal Chat Methods
  async sendPersonalMessage(messageData) {
    return this.request("/chat/personal/send", {
      method: "POST",
      body: JSON.stringify(messageData),
    });
  }

  async getMessageHistory(senderId, receiverId, page = 0, size = 20) {
    return this.request(
      `/chat/personal/messages?senderId=${senderId}&receiverId=${receiverId}&page=${page}&size=${size}`
    );
  }

  // Notification Methods
  async getUserNotifications(userId) {
    return this.request(`/notifications/user/${userId}`);
  }

  async markNotificationAsRead(notificationId) {
    return this.request(`/notifications/${notificationId}/read`, {
      method: "PUT",
    });
  }
}

export const apiClient = new ApiClient();
```

### 4. React Integration Example

```jsx
// components/ChatComponent.jsx
import React, { useState, useEffect } from "react";
import { onAuthStateChanged } from "firebase/auth";
import { auth } from "../firebase";
import { apiClient } from "../utils/apiClient";

const ChatComponent = () => {
  const [user, setUser] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");

  useEffect(() => {
    // Listen for authentication state changes
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      setUser(user);
      if (user) {
        loadMessages();
      }
    });

    return () => unsubscribe();
  }, []);

  const loadMessages = async () => {
    try {
      if (user) {
        const history = await apiClient.getMessageHistory(
          user.uid,
          "targetUserId"
        );
        setMessages(history.content || []);
      }
    } catch (error) {
      console.error("Failed to load messages:", error);
    }
  };

  const sendMessage = async () => {
    try {
      if (user && newMessage.trim()) {
        await apiClient.sendPersonalMessage({
          senderId: user.uid,
          receiverId: "targetUserId",
          content: newMessage,
          messageType: "TEXT",
        });
        setNewMessage("");
        loadMessages(); // Refresh messages
      }
    } catch (error) {
      console.error("Failed to send message:", error);
    }
  };

  if (!user) {
    return <div>Please sign in to use chat</div>;
  }

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
          onKeyPress={(e) => e.key === "Enter" && sendMessage()}
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
};

export default ChatComponent;
```

## WebSocket Authentication

WebSocket connections also support Firebase authentication:

```javascript
// utils/websocket.js
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import { getFirebaseToken } from "./authToken";

class ChatWebSocket {
  constructor() {
    this.stompClient = null;
    this.connected = false;
  }

  async connect() {
    try {
      const token = await getFirebaseToken();

      const socket = new SockJS("http://localhost:8083/api/ws");
      this.stompClient = Stomp.over(socket);

      // Set headers with Firebase token
      const headers = {
        Authorization: `Bearer ${token}`,
      };

      this.stompClient.connect(
        headers,
        (frame) => {
          console.log("WebSocket connected:", frame);
          this.connected = true;

          // Subscribe to personal messages
          this.stompClient.subscribe("/user/queue/messages", (message) => {
            const messageData = JSON.parse(message.body);
            this.handleNewMessage(messageData);
          });

          // Subscribe to notifications
          this.stompClient.subscribe(
            "/user/queue/notifications",
            (notification) => {
              const notificationData = JSON.parse(notification.body);
              this.handleNewNotification(notificationData);
            }
          );
        },
        (error) => {
          console.error("WebSocket connection error:", error);
          this.connected = false;
        }
      );
    } catch (error) {
      console.error("Failed to connect WebSocket:", error);
    }
  }

  handleNewMessage(message) {
    // Handle incoming messages
    console.log("New message:", message);
  }

  handleNewNotification(notification) {
    // Handle incoming notifications
    console.log("New notification:", notification);
  }

  disconnect() {
    if (this.stompClient && this.connected) {
      this.stompClient.disconnect();
      this.connected = false;
    }
  }
}

export const chatWebSocket = new ChatWebSocket();
```

## Security Considerations

### 1. Token Refresh

Firebase ID tokens expire after 1 hour. The frontend automatically handles refresh:

```javascript
// The token is automatically refreshed when you call getIdToken()
const token = await user.getIdToken(true); // Force refresh
```

### 2. Error Handling

Handle authentication errors gracefully:

```javascript
// utils/errorHandler.js
export const handleApiError = (error) => {
  if (error.message.includes("Authentication failed")) {
    // Redirect to login or refresh token
    window.location.href = "/login";
  }
  // Handle other errors...
};
```

### 3. CORS Configuration

The backend is configured to allow your frontend origins. Update production URLs in `application.properties`:

```properties
security.cors.allowed-origins=https://your-production-domain.com,http://localhost:3000
```

## Testing Authentication

### 1. Test with Postman

1. Sign in to your frontend application
2. Open browser dev tools → Network tab
3. Make an API request and copy the Authorization header
4. Use this token in Postman:
   ```
   Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

### 2. Test Token Verification

Create a test endpoint to verify your setup:

```bash
curl -X GET http://localhost:8083/api/v1/chat/personal/health \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

## Troubleshooting

### Common Issues

1. **"Authentication failed"**

   - Check if Firebase project ID matches in both frontend and backend
   - Verify service account file is correctly placed
   - Ensure user is signed in on frontend

2. **CORS errors**

   - Update allowed origins in SecurityConfig
   - Check if frontend URL matches configuration

3. **Token expired**
   - Frontend automatically refreshes tokens
   - Implement proper error handling for token refresh

### Debug Logging

Enable debug logging to troubleshoot:

```properties
# application.properties
logging.level.com.islandhop.chat.security=DEBUG
logging.level.com.islandhop.chat.service.FirebaseAuthService=DEBUG
```

This setup provides secure, scalable authentication that integrates seamlessly with your existing Firebase frontend configuration.
