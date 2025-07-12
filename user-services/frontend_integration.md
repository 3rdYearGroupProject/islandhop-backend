# IslandHop User Services - Frontend Integration Guide

## Base URL

```
Production: https://api.islandhop.com/api/v1
Development: http://localhost:8083/api/v1
```

## Authentication

All endpoints use **session-based authentication**. Include `credentials: 'include'` in fetch requests to send session cookies.

## Universal Role Controller Endpoints

### 1. GET /role

**Description:** Get current user's role and email from session

**URL:** `GET /api/v1/role`

**Headers:** None (session cookies automatically included)

**Request Body:** None

**Response (200 OK):**

```json
{
  "email": "user@example.com",
  "role": "SUPPORT"
}
```

**Error Response (401 Unauthorized):**

```json
{
  "message": "Not authenticated"
}
```

**JavaScript Implementation:**

```javascript
/**
 * Get current user's role and email from session
 * @returns {Promise<Object|null>} User info or null if not authenticated
 */
async function getUserRole() {
  try {
    const response = await fetch("/api/v1/role", {
      method: "GET",
      credentials: "include",
    });

    if (response.ok) {
      const data = await response.json();
      return data;
    } else if (response.status === 401) {
      return null;
    } else {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
  } catch (error) {
    console.error("Error fetching user role:", error);
    throw error;
  }
}

// Usage Example
getUserRole()
  .then((userInfo) => {
    if (userInfo) {
      console.log(`User: ${userInfo.email}, Role: ${userInfo.role}`);
      // Show appropriate UI based on role
      if (userInfo.role === "SUPPORT" || userInfo.role === "ADMIN") {
        document.getElementById("adminPanel").style.display = "block";
      }
    } else {
      // Redirect to login
      window.location.href = "/login";
    }
  })
  .catch((error) => {
    console.error("Authentication check failed:", error);
    window.location.href = "/login";
  });
```

### 2. GET /users

**Description:** Get all user accounts in the system (requires SUPPORT or ADMIN role)

**URL:** `GET /api/v1/users`

**Headers:** None (session cookies automatically included)

**Request Body:** None

**Authentication:** Required (SUPPORT or ADMIN role)

**Response (200 OK):**

```json
{
  "status": "success",
  "users": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "profilePicUrl": "https://example.com/profile1.jpg",
      "accountType": "TOURIST",
      "status": "ACTIVE"
    },
    {
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane.smith@example.com",
      "profilePicUrl": "https://example.com/profile2.jpg",
      "accountType": "DRIVER",
      "status": "PENDING"
    },
    {
      "firstName": "Bob",
      "lastName": "Johnson",
      "email": "bob.johnson@example.com",
      "profilePicUrl": "https://example.com/profile3.jpg",
      "accountType": "GUIDE",
      "status": "ACTIVE"
    },
    {
      "firstName": "Alice",
      "lastName": "Brown",
      "email": "alice.brown@example.com",
      "profilePicUrl": "https://example.com/profile4.jpg",
      "accountType": "SUPPORT",
      "status": "ACTIVE"
    }
  ]
}
```

**Error Responses:**

- **401 Unauthorized:** `{"message": "Not authenticated"}`
- **403 Forbidden:** `{"message": "Insufficient permissions"}`
- **500 Internal Server Error:** `{"message": "Internal server error"}`

**JavaScript Implementation:**

```javascript
/**
 * Get all users in the system
 * @returns {Promise<Array>} Array of user objects
 */
async function getAllUsers() {
  try {
    const response = await fetch("/api/v1/users", {
      method: "GET",
      credentials: "include",
    });

    if (response.ok) {
      const data = await response.json();
      if (data.status === "success") {
        return data.users;
      } else {
        throw new Error("Unexpected response format");
      }
    } else if (response.status === 401) {
      throw new Error("Not authenticated");
    } else if (response.status === 403) {
      throw new Error("Insufficient permissions");
    } else {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
  } catch (error) {
    console.error("Error fetching users:", error);
    throw error;
  }
}

/**
 * Display users in a table format
 */
async function displayUsers() {
  try {
    const users = await getAllUsers();

    const tableBody = document.getElementById("userTableBody");
    tableBody.innerHTML = "";

    users.forEach((user) => {
      const row = document.createElement("tr");
      row.innerHTML = `
                <td>
                    <img src="${user.profilePicUrl || "/default-avatar.png"}" 
                         alt="Profile" class="profile-pic" width="40" height="40">
                </td>
                <td>${user.firstName} ${user.lastName}</td>
                <td>${user.email}</td>
                <td>
                    <span class="account-type account-type-${user.accountType.toLowerCase()}">
                        ${user.accountType}
                    </span>
                </td>
                <td>
                    <span class="status-badge status-${user.status.toLowerCase()}">
                        ${user.status}
                    </span>
                </td>
                <td>
                    <button onclick="openStatusModal('${user.email}', '${
        user.status
      }')" 
                            class="btn btn-primary btn-sm">
                        Update Status
                    </button>
                </td>
            `;
      tableBody.appendChild(row);
    });

    console.log(`Loaded ${users.length} users`);
  } catch (error) {
    handleError(error);
  }
}

/**
 * Filter users by account type
 */
function filterUsersByType(accountType) {
  const rows = document.querySelectorAll("#userTableBody tr");
  rows.forEach((row) => {
    const typeCell = row.querySelector(".account-type");
    if (accountType === "ALL" || typeCell.textContent.trim() === accountType) {
      row.style.display = "";
    } else {
      row.style.display = "none";
    }
  });
}

/**
 * Filter users by status
 */
function filterUsersByStatus(status) {
  const rows = document.querySelectorAll("#userTableBody tr");
  rows.forEach((row) => {
    const statusCell = row.querySelector(".status-badge");
    if (status === "ALL" || statusCell.textContent.trim() === status) {
      row.style.display = "";
    } else {
      row.style.display = "none";
    }
  });
}
```

### 3. PUT /users/status

**Description:** Update user account status (requires SUPPORT or ADMIN role)

**URL:** `PUT /api/v1/users/status`

**Headers:**

- `Content-Type: application/json`

**Request Body:**

```json
{
  "email": "user@example.com",
  "status": "DEACTIVATED"
}
```

**Valid Status Values:**

- `ACTIVE` - User account is active and functional
- `DEACTIVATED` - User account is deactivated
- `SUSPENDED` - User account is temporarily suspended
- `PENDING` - User account is pending approval

**Authentication:** Required (SUPPORT or ADMIN role)

**Response (200 OK):**

```json
{
  "status": "success",
  "message": "User status updated successfully"
}
```

**Error Responses:**

- **400 Bad Request:**
  - `{"message": "Invalid status: INVALID_STATUS. Must be one of: ACTIVE, DEACTIVATED, SUSPENDED, PENDING"}`
  - `{"message": "User not found with email: user@example.com"}`
  - `{"message": "Email cannot be null or empty"}`
  - `{"message": "Invalid email format"}`
- **401 Unauthorized:** `{"message": "Not authenticated"}`
- **403 Forbidden:** `{"message": "Insufficient permissions"}`
- **500 Internal Server Error:** `{"message": "Internal server error"}`

**JavaScript Implementation:**

```javascript
/**
 * Update user account status
 * @param {string} email - User email to update
 * @param {string} newStatus - New status (ACTIVE, DEACTIVATED, SUSPENDED, PENDING)
 * @returns {Promise<Object>} Update result
 */
async function updateUserStatus(email, newStatus) {
  try {
    // Validate input
    if (!email || !newStatus) {
      throw new Error("Email and status are required");
    }

    const validStatuses = ["ACTIVE", "DEACTIVATED", "SUSPENDED", "PENDING"];
    if (!validStatuses.includes(newStatus.toUpperCase())) {
      throw new Error(
        `Invalid status. Must be one of: ${validStatuses.join(", ")}`
      );
    }

    const response = await fetch("/api/v1/users/status", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify({
        email: email,
        status: newStatus.toUpperCase(),
      }),
    });

    const data = await response.json();

    if (response.ok) {
      return data;
    } else {
      throw new Error(data.message || `HTTP error! status: ${response.status}`);
    }
  } catch (error) {
    console.error("Error updating user status:", error);
    throw error;
  }
}

/**
 * Open status update modal
 */
function openStatusModal(userEmail, currentStatus) {
  const modal = document.getElementById("statusModal");
  const emailSpan = document.getElementById("modalUserEmail");
  const statusSelect = document.getElementById("statusSelect");
  const currentStatusSpan = document.getElementById("currentStatus");

  emailSpan.textContent = userEmail;
  currentStatusSpan.textContent = currentStatus;
  statusSelect.value = currentStatus;

  modal.style.display = "block";

  // Store email for later use
  document.getElementById("confirmStatusUpdate").onclick = () => {
    confirmStatusUpdate(userEmail);
  };
}

/**
 * Confirm status update
 */
async function confirmStatusUpdate(userEmail) {
  const statusSelect = document.getElementById("statusSelect");
  const newStatus = statusSelect.value;

  if (!newStatus) {
    alert("Please select a status");
    return;
  }

  try {
    // Show loading state
    const confirmButton = document.getElementById("confirmStatusUpdate");
    const originalText = confirmButton.textContent;
    confirmButton.textContent = "Updating...";
    confirmButton.disabled = true;

    const result = await updateUserStatus(userEmail, newStatus);

    if (result.status === "success") {
      alert("User status updated successfully");
      closeStatusModal();
      displayUsers(); // Refresh the user list
    }
  } catch (error) {
    handleError(error);
  } finally {
    // Reset button state
    const confirmButton = document.getElementById("confirmStatusUpdate");
    confirmButton.textContent = "Update Status";
    confirmButton.disabled = false;
  }
}

/**
 * Close status modal
 */
function closeStatusModal() {
  document.getElementById("statusModal").style.display = "none";
}

/**
 * Batch update multiple users
 */
async function batchUpdateStatus(userEmails, newStatus) {
  const results = [];

  for (const email of userEmails) {
    try {
      const result = await updateUserStatus(email, newStatus);
      results.push({ email, success: true, result });
    } catch (error) {
      results.push({ email, success: false, error: error.message });
    }
  }

  return results;
}
```

## Error Handling

**Global Error Handler:**

```javascript
/**
 * Handle common errors across all endpoints
 * @param {Error} error - Error object
 */
function handleError(error) {
  console.error("Error occurred:", error);

  if (error.message.includes("Not authenticated")) {
    alert("Your session has expired. Please log in again.");
    window.location.href = "/login";
  } else if (error.message.includes("Insufficient permissions")) {
    alert("You do not have permission to perform this action.");
  } else if (error.message.includes("User not found")) {
    alert("The specified user was not found.");
  } else if (error.message.includes("Invalid status")) {
    alert("Invalid status selected. Please choose a valid status.");
  } else if (error.message.includes("Invalid email format")) {
    alert("Please enter a valid email address.");
  } else {
    alert("An error occurred: " + error.message);
  }
}
```

## Complete HTML Example

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>User Management - IslandHop</title>
    <style>
      body {
        font-family: Arial, sans-serif;
        margin: 20px;
      }
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
      }
      .filters {
        margin-bottom: 20px;
      }
      .filters select {
        margin-right: 10px;
        padding: 5px;
      }
      table {
        width: 100%;
        border-collapse: collapse;
        margin-bottom: 20px;
      }
      th,
      td {
        border: 1px solid #ddd;
        padding: 8px;
        text-align: left;
      }
      th {
        background-color: #f2f2f2;
        font-weight: bold;
      }
      .profile-pic {
        border-radius: 50%;
        object-fit: cover;
      }
      .account-type {
        padding: 3px 8px;
        border-radius: 12px;
        font-size: 11px;
        font-weight: bold;
        text-transform: uppercase;
      }
      .account-type-tourist {
        background-color: #e3f2fd;
        color: #1976d2;
      }
      .account-type-driver {
        background-color: #f3e5f5;
        color: #7b1fa2;
      }
      .account-type-guide {
        background-color: #e8f5e8;
        color: #388e3c;
      }
      .account-type-support {
        background-color: #fff3e0;
        color: #f57c00;
      }
      .account-type-admin {
        background-color: #ffebee;
        color: #d32f2f;
      }
      .status-badge {
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: bold;
        text-transform: uppercase;
      }
      .status-active {
        background-color: #d4edda;
        color: #155724;
      }
      .status-deactivated {
        background-color: #f8d7da;
        color: #721c24;
      }
      .status-suspended {
        background-color: #fff3cd;
        color: #856404;
      }
      .status-pending {
        background-color: #cce5ff;
        color: #004085;
      }
      .btn {
        padding: 6px 12px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 12px;
      }
      .btn-primary {
        background-color: #007bff;
        color: white;
      }
      .btn-primary:hover {
        background-color: #0056b3;
      }
      .btn-sm {
        padding: 4px 8px;
        font-size: 11px;
      }
      .modal {
        display: none;
        position: fixed;
        z-index: 1000;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
      }
      .modal-content {
        background-color: #fefefe;
        margin: 15% auto;
        padding: 20px;
        border: 1px solid #888;
        border-radius: 8px;
        width: 400px;
        max-width: 80%;
      }
      .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
      }
      .modal-body {
        margin-bottom: 15px;
      }
      .modal-footer {
        text-align: right;
      }
      .modal-footer button {
        margin-left: 10px;
      }
      .form-group {
        margin-bottom: 15px;
      }
      .form-group label {
        display: block;
        margin-bottom: 5px;
        font-weight: bold;
      }
      .form-group select,
      .form-group input {
        width: 100%;
        padding: 8px;
        border: 1px solid #ddd;
        border-radius: 4px;
      }
      .loading {
        text-align: center;
        padding: 20px;
        color: #666;
      }
      .error {
        color: #dc3545;
        background-color: #f8d7da;
        border: 1px solid #f5c6cb;
        padding: 10px;
        border-radius: 4px;
        margin-bottom: 15px;
      }
    </style>
  </head>
  <body>
    <div class="header">
      <h1>User Management</h1>
      <div id="userInfo">
        <span id="userEmail"></span> | <span id="userRole"></span>
      </div>
    </div>

    <div class="filters">
      <label>Filter by Account Type:</label>
      <select id="typeFilter" onchange="filterUsersByType(this.value)">
        <option value="ALL">All Types</option>
        <option value="TOURIST">Tourist</option>
        <option value="DRIVER">Driver</option>
        <option value="GUIDE">Guide</option>
        <option value="SUPPORT">Support</option>
        <option value="ADMIN">Admin</option>
      </select>

      <label>Filter by Status:</label>
      <select id="statusFilter" onchange="filterUsersByStatus(this.value)">
        <option value="ALL">All Statuses</option>
        <option value="ACTIVE">Active</option>
        <option value="DEACTIVATED">Deactivated</option>
        <option value="SUSPENDED">Suspended</option>
        <option value="PENDING">Pending</option>
      </select>

      <button onclick="displayUsers()" class="btn btn-primary">Refresh</button>
    </div>

    <table id="userTable">
      <thead>
        <tr>
          <th>Profile</th>
          <th>Name</th>
          <th>Email</th>
          <th>Account Type</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody id="userTableBody">
        <tr>
          <td colspan="6" class="loading">Loading users...</td>
        </tr>
      </tbody>
    </table>

    <!-- Status Update Modal -->
    <div id="statusModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Update User Status</h3>
          <span
            onclick="closeStatusModal()"
            style="cursor: pointer; font-size: 20px;"
            >&times;</span
          >
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Email:</label>
            <span id="modalUserEmail" style="font-weight: normal;"></span>
          </div>
          <div class="form-group">
            <label>Current Status:</label>
            <span id="currentStatus" style="font-weight: normal;"></span>
          </div>
          <div class="form-group">
            <label for="statusSelect">New Status:</label>
            <select id="statusSelect">
              <option value="ACTIVE">Active</option>
              <option value="DEACTIVATED">Deactivated</option>
              <option value="SUSPENDED">Suspended</option>
              <option value="PENDING">Pending</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button onclick="closeStatusModal()" class="btn">Cancel</button>
          <button id="confirmStatusUpdate" class="btn btn-primary">
            Update Status
          </button>
        </div>
      </div>
    </div>

    <script>
      // Initialize page
      document.addEventListener("DOMContentLoaded", async function () {
        try {
          const userInfo = await getUserRole();
          if (
            userInfo &&
            (userInfo.role === "SUPPORT" || userInfo.role === "ADMIN")
          ) {
            document.getElementById("userEmail").textContent = userInfo.email;
            document.getElementById("userRole").textContent = userInfo.role;
            await displayUsers();
          } else {
            alert("Access denied. Insufficient permissions.");
            window.location.href = "/";
          }
        } catch (error) {
          console.error("Error initializing page:", error);
          window.location.href = "/login";
        }
      });

      // Close modal when clicking outside
      window.onclick = function (event) {
        const modal = document.getElementById("statusModal");
        if (event.target === modal) {
          closeStatusModal();
        }
      };

      // Include all JavaScript functions from above
      // ... (copy all the JavaScript functions from the previous sections)
    </script>
  </body>
</html>
```

## Testing with Postman

1. **Import the Collection:**

   - Save the `postman_collection.json` file
   - Open Postman → Import → Select the file

2. **Set Environment Variables:**

   - Create a new environment in Postman
   - Add variable `baseUrl` with value `http://localhost:8083/api/v1`

3. **Test Authentication:**

   - First test `/role` endpoint to check if session is valid
   - If not authenticated, you'll need to login through your login endpoint first

4. **Test User Management:**

   - Test `/users` endpoint to get all users
   - Test `/users/status` endpoint with different status values

5. **Test Error Scenarios:**
   - Invalid email formats
   - Invalid status values
   - Non-existent users

## Status Codes Summary

- **200 OK:** Request successful
- **400 Bad Request:** Invalid input data or validation error
- **401 Unauthorized:** Not authenticated (session expired or invalid)
- **403 Forbidden:** Insufficient permissions (not SUPPORT or ADMIN)
- **500 Internal Server Error:** Server-side error occurred

## Session Management

- **Session Timeout:** Sessions typically expire after 30 minutes of inactivity
- **Session Refresh:** Call `/role` endpoint periodically to check session validity
- **Logout:** Clear session by calling logout endpoint (if available)
- **Multiple Tabs:** Sessions are shared across browser tabs

## Best Practices

1. **Error Handling:** Always handle network errors and HTTP error codes
2. **Loading States:** Show loading indicators during API calls
3. **Validation:** Validate input on client-side before sending requests
4. **Caching:** Cache user list and refresh periodically
5. **Security:** Never store sensitive data in localStorage/sessionStorage
6. **Accessibility:** Ensure proper ARIA labels and keyboard navigation
7. **Responsive Design:** Make interface mobile-friendly
8. **Logging:** Log important actions for debugging
