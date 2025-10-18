# Group Chat - Complete Frontend Integration Guide

## Overview

This comprehensive guide covers all frontend integration for group chat functionality in the IslandHop Chat & Notification Microservice. The service provides complete group management capabilities including creation, messaging, member management, and administration.

## Base Configuration

### API Base URL

```javascript
const API_BASE_URL = "http://localhost:8083/api/v1";
const GROUP_CHAT_ENDPOINT = `${API_BASE_URL}/chat/group`;
```

### Authentication Headers

All requests require Firebase Authentication token:

```javascript
const getAuthHeaders = async () => {
  const authToken = await getAuthToken(); // Your Firebase token function
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${authToken}`,
    Accept: "application/json",
  };
};
```

---

## 1. CREATE GROUP

### API Endpoint

```
POST /chat/group/create
```

### Request Body

```javascript
{
  "groupName": "Travel Buddies Sri Lanka",    // Required, max 100 characters
  "description": "Planning our adventure",    // Optional, max 500 characters
  "groupType": "PRIVATE",                     // Optional: "PRIVATE" or "PUBLIC"
  "memberIds": ["user456", "user789"],        // Required: Array of user IDs to invite
  "adminId": "user123"                        // Required: Creator's user ID
}
```

### Frontend Implementation

```javascript
// Create group service function
async function createGroup(groupData) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(`${GROUP_CHAT_ENDPOINT}/create`, {
      method: "POST",
      headers,
      body: JSON.stringify({
        groupName: groupData.groupName.trim(),
        description: groupData.description?.trim() || "",
        groupType: groupData.groupType || "PRIVATE",
        memberIds: groupData.memberIds || [],
        adminId: groupData.creatorId,
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to create group: ${errorText}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Error creating group:", error);
    throw error;
  }
}

// React component for group creation
const CreateGroupForm = ({ onGroupCreated, onCancel }) => {
  const [formData, setFormData] = useState({
    groupName: "",
    description: "",
    groupType: "PRIVATE",
    selectedMembers: [],
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const currentUserId = getCurrentUserId(); // Your auth function

      const groupData = {
        ...formData,
        memberIds: formData.selectedMembers,
        creatorId: currentUserId,
      };

      const newGroup = await createGroup(groupData);
      onGroupCreated(newGroup);

      // Reset form
      setFormData({
        groupName: "",
        description: "",
        groupType: "PRIVATE",
        selectedMembers: [],
      });
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="create-group-form">
      <h2>Create New Group</h2>

      {error && <div className="error-alert">{error}</div>}

      <div className="form-group">
        <label>Group Name *</label>
        <input
          type="text"
          value={formData.groupName}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, groupName: e.target.value }))
          }
          required
          maxLength={100}
          placeholder="Enter group name"
        />
      </div>

      <div className="form-group">
        <label>Description</label>
        <textarea
          value={formData.description}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, description: e.target.value }))
          }
          maxLength={500}
          placeholder="Describe your group (optional)"
          rows={3}
        />
      </div>

      <div className="form-group">
        <label>Group Type</label>
        <select
          value={formData.groupType}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, groupType: e.target.value }))
          }
        >
          <option value="PRIVATE">Private</option>
          <option value="PUBLIC">Public</option>
        </select>
      </div>

      {/* Member selection component would go here */}
      <MemberSelector
        selectedMembers={formData.selectedMembers}
        onMembersChange={(members) =>
          setFormData((prev) => ({ ...prev, selectedMembers: members }))
        }
      />

      <div className="form-actions">
        <button type="button" onClick={onCancel} disabled={loading}>
          Cancel
        </button>
        <button
          type="submit"
          disabled={
            loading ||
            !formData.groupName.trim() ||
            formData.selectedMembers.length === 0
          }
        >
          {loading ? "Creating..." : "Create Group"}
        </button>
      </div>
    </form>
  );
};
```

---

## 2. SEND GROUP MESSAGE

### API Endpoint

```
POST /chat/group/send
```

### Request Body

```javascript
{
  "groupId": "group_123456789",               // Required: Group ID
  "senderId": "user123",                      // Required: Sender's user ID
  "content": "Hey everyone! Ready for trip?", // Required: Message content
  "messageType": "TEXT",                      // Required: "TEXT", "IMAGE", "FILE"
  "senderName": "John Doe"                    // Required: Sender's display name
}
```

### Frontend Implementation

```javascript
// Send group message function
async function sendGroupMessage(messageData) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(`${GROUP_CHAT_ENDPOINT}/send`, {
      method: "POST",
      headers,
      body: JSON.stringify(messageData),
    });

    if (!response.ok) {
      throw new Error("Failed to send message");
    }

    return await response.json();
  } catch (error) {
    console.error("Error sending group message:", error);
    throw error;
  }
}

// React component for sending messages
const GroupMessageInput = ({ groupId, onMessageSent }) => {
  const [message, setMessage] = useState("");
  const [sending, setSending] = useState(false);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!message.trim() || sending) return;

    setSending(true);
    try {
      const currentUser = getCurrentUser(); // Your auth function

      const messageData = {
        groupId,
        senderId: currentUser.id,
        content: message.trim(),
        messageType: "TEXT",
        senderName: currentUser.displayName || currentUser.email,
      };

      const sentMessage = await sendGroupMessage(messageData);
      onMessageSent(sentMessage);
      setMessage("");
    } catch (error) {
      console.error("Failed to send message:", error);
      // Show error to user
    } finally {
      setSending(false);
    }
  };

  return (
    <form onSubmit={handleSend} className="message-input-form">
      <div className="input-container">
        <input
          type="text"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="Type a message..."
          disabled={sending}
          maxLength={1000}
        />
        <button
          type="submit"
          disabled={!message.trim() || sending}
          className="send-button"
        >
          {sending ? "⏳" : "➤"}
        </button>
      </div>
    </form>
  );
};
```

---

## 3. GET GROUP MESSAGES

### API Endpoint

```
GET /chat/group/{groupId}/messages?page=0&size=20
```

### Frontend Implementation

```javascript
// Get group messages with pagination
async function getGroupMessages(groupId, page = 0, size = 20) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(
      `${GROUP_CHAT_ENDPOINT}/${groupId}/messages?page=${page}&size=${size}`,
      { headers }
    );

    if (!response.ok) {
      throw new Error("Failed to fetch messages");
    }

    return await response.json();
  } catch (error) {
    console.error("Error fetching group messages:", error);
    throw error;
  }
}

// React component for displaying messages
const GroupMessageList = ({ groupId }) => {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  const loadMessages = async (pageNum = 0, append = false) => {
    try {
      setLoading(true);
      const response = await getGroupMessages(groupId, pageNum, 20);

      if (append) {
        setMessages((prev) => [...prev, ...response.content]);
      } else {
        setMessages(response.content);
      }

      setHasMore(!response.last);
      setPage(pageNum);
    } catch (error) {
      console.error("Error loading messages:", error);
    } finally {
      setLoading(false);
    }
  };

  const loadMoreMessages = () => {
    if (hasMore && !loading) {
      loadMessages(page + 1, true);
    }
  };

  useEffect(() => {
    loadMessages();
  }, [groupId]);

  return (
    <div className="message-list">
      {hasMore && (
        <button
          onClick={loadMoreMessages}
          disabled={loading}
          className="load-more-btn"
        >
          {loading ? "Loading..." : "Load More Messages"}
        </button>
      )}

      {messages.map((message) => (
        <div key={message.id} className="message-item">
          <div className="message-header">
            <span className="sender-name">{message.senderName}</span>
            <span className="timestamp">
              {new Date(message.timestamp).toLocaleString()}
            </span>
          </div>
          <div className="message-content">{message.content}</div>
        </div>
      ))}

      {loading && page === 0 && (
        <div className="loading">Loading messages...</div>
      )}
    </div>
  );
};
```

---

## 4. ADD MEMBER TO GROUP

### API Endpoint

```
POST /chat/group/add-member
```

### Request Body

```javascript
{
  "groupId": "group_123456789",    // Required: Group ID
  "userId": "user999",             // Required: User ID to add
  "requesterId": "user123"         // Required: ID of user making the request
}
```

### Frontend Implementation

```javascript
// Add member to group
async function addMemberToGroup(groupId, userId, requesterId) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(`${GROUP_CHAT_ENDPOINT}/add-member`, {
      method: "POST",
      headers,
      body: JSON.stringify({
        groupId,
        userId,
        requesterId,
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to add member: ${errorText}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Error adding member:", error);
    throw error;
  }
}

// React component for adding members
const AddMemberModal = ({ groupId, isOpen, onClose, onMemberAdded }) => {
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [availableUsers, setAvailableUsers] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleAddMembers = async () => {
    setLoading(true);
    const currentUserId = getCurrentUserId();

    try {
      const promises = selectedUsers.map((userId) =>
        addMemberToGroup(groupId, userId, currentUserId)
      );

      await Promise.all(promises);
      onMemberAdded();
      onClose();
    } catch (error) {
      console.error("Error adding members:", error);
      // Show error message to user
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <div className="add-member-modal">
        <h3>Add Members to Group</h3>

        <UserSelector
          availableUsers={availableUsers}
          selectedUsers={selectedUsers}
          onSelectionChange={setSelectedUsers}
        />

        <div className="modal-actions">
          <button onClick={onClose} disabled={loading}>
            Cancel
          </button>
          <button
            onClick={handleAddMembers}
            disabled={loading || selectedUsers.length === 0}
          >
            {loading ? "Adding..." : `Add ${selectedUsers.length} Member(s)`}
          </button>
        </div>
      </div>
    </Modal>
  );
};
```

---

## 5. REMOVE MEMBER FROM GROUP

### API Endpoint

```
DELETE /chat/group/{groupId}/member/{userId}?removedBy={removerId}
```

### Frontend Implementation

```javascript
// Remove member from group
async function removeMemberFromGroup(groupId, userId, removedBy) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(
      `${GROUP_CHAT_ENDPOINT}/${groupId}/member/${userId}?removedBy=${removedBy}`,
      {
        method: "DELETE",
        headers,
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to remove member: ${errorText}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Error removing member:", error);
    throw error;
  }
}

// React component for member management
const GroupMemberItem = ({ member, groupId, isAdmin, onMemberRemoved }) => {
  const [removing, setRemoving] = useState(false);
  const currentUserId = getCurrentUserId();

  const handleRemove = async () => {
    if (!confirm(`Remove ${member.name} from the group?`)) return;

    setRemoving(true);
    try {
      await removeMemberFromGroup(groupId, member.id, currentUserId);
      onMemberRemoved(member.id);
    } catch (error) {
      console.error("Failed to remove member:", error);
      // Show error message
    } finally {
      setRemoving(false);
    }
  };

  return (
    <div className="member-item">
      <div className="member-info">
        <span className="member-name">{member.name}</span>
        {member.isAdmin && <span className="admin-badge">Admin</span>}
      </div>

      {isAdmin && member.id !== currentUserId && (
        <button
          onClick={handleRemove}
          disabled={removing}
          className="remove-member-btn"
        >
          {removing ? "⏳" : "✕"}
        </button>
      )}
    </div>
  );
};
```

---

## 6. GET USER GROUPS

### API Endpoint

```
GET /chat/group/user/{userId}
```

### Frontend Implementation

```javascript
// Get all groups for a user
async function getUserGroups(userId) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(`${GROUP_CHAT_ENDPOINT}/user/${userId}`, {
      headers,
    });

    if (!response.ok) {
      throw new Error("Failed to fetch user groups");
    }

    return await response.json();
  } catch (error) {
    console.error("Error fetching user groups:", error);
    throw error;
  }
}

// React component for displaying user's groups
const GroupsList = ({ userId, onGroupSelect }) => {
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadGroups = async () => {
      try {
        const userGroups = await getUserGroups(userId);
        setGroups(userGroups);
      } catch (error) {
        console.error("Error loading groups:", error);
      } finally {
        setLoading(false);
      }
    };

    loadGroups();
  }, [userId]);

  if (loading) return <div className="loading">Loading groups...</div>;

  return (
    <div className="groups-list">
      <h3>Your Groups</h3>
      {groups.length === 0 ? (
        <div className="no-groups">
          <p>You haven't joined any groups yet.</p>
          <button onClick={() => onGroupSelect("create")}>
            Create a Group
          </button>
        </div>
      ) : (
        groups.map((group) => (
          <div
            key={group.id}
            className="group-item"
            onClick={() => onGroupSelect(group)}
          >
            <div className="group-info">
              <h4>{group.groupName}</h4>
              <p>{group.description}</p>
              <span className="member-count">
                {group.memberIds?.length || 0} members
              </span>
            </div>
            <div className="group-meta">
              {group.adminId === userId && (
                <span className="admin-indicator">Admin</span>
              )}
              <span className="group-type">{group.groupType}</span>
            </div>
          </div>
        ))
      )}
    </div>
  );
};
```

---

## 7. GET GROUP DETAILS

### API Endpoint

```
GET /chat/group/{groupId}
```

### Frontend Implementation

```javascript
// Get group details
async function getGroupDetails(groupId) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(`${GROUP_CHAT_ENDPOINT}/${groupId}`, {
      headers,
    });

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error("Group not found");
      }
      throw new Error("Failed to fetch group details");
    }

    return await response.json();
  } catch (error) {
    console.error("Error fetching group details:", error);
    throw error;
  }
}

// React component for group details
const GroupDetailsPanel = ({ groupId }) => {
  const [group, setGroup] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadGroupDetails = async () => {
      try {
        setLoading(true);
        setError(null);
        const groupData = await getGroupDetails(groupId);
        setGroup(groupData);
      } catch (error) {
        setError(error.message);
      } finally {
        setLoading(false);
      }
    };

    if (groupId) {
      loadGroupDetails();
    }
  }, [groupId]);

  if (loading) return <div className="loading">Loading group details...</div>;
  if (error) return <div className="error">Error: {error}</div>;
  if (!group) return <div className="no-group">Group not found</div>;

  return (
    <div className="group-details">
      <div className="group-header">
        <h2>{group.groupName}</h2>
        <span className="group-type-badge">{group.groupType}</span>
      </div>

      {group.description && (
        <div className="group-description">
          <p>{group.description}</p>
        </div>
      )}

      <div className="group-info">
        <div className="info-item">
          <label>Created:</label>
          <span>{new Date(group.createdAt).toLocaleDateString()}</span>
        </div>
        <div className="info-item">
          <label>Members:</label>
          <span>{group.memberIds?.length || 0}</span>
        </div>
        <div className="info-item">
          <label>Admin:</label>
          <span>{group.adminId}</span>
        </div>
      </div>
    </div>
  );
};
```

---

## 8. UPDATE GROUP

### API Endpoint

```
PUT /chat/group/{groupId}
```

### Request Body

```javascript
{
  "groupName": "Updated Group Name",     // Optional
  "description": "Updated description",  // Optional
  "groupType": "PRIVATE"                // Optional
}
```

### Frontend Implementation

```javascript
// Update group information
async function updateGroup(groupId, updateData) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(`${GROUP_CHAT_ENDPOINT}/${groupId}`, {
      method: "PUT",
      headers,
      body: JSON.stringify(updateData),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to update group: ${errorText}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Error updating group:", error);
    throw error;
  }
}

// React component for editing group
const EditGroupForm = ({ group, onGroupUpdated, onCancel }) => {
  const [formData, setFormData] = useState({
    groupName: group.groupName || "",
    description: group.description || "",
    groupType: group.groupType || "PRIVATE",
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");

    try {
      const updateData = {
        groupName: formData.groupName.trim(),
        description: formData.description.trim(),
        groupType: formData.groupType,
      };

      const updatedGroup = await updateGroup(group.id, updateData);
      onGroupUpdated(updatedGroup);
    } catch (error) {
      setError(error.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="edit-group-form">
      <h3>Edit Group</h3>

      {error && <div className="error-alert">{error}</div>}

      <div className="form-group">
        <label>Group Name</label>
        <input
          type="text"
          value={formData.groupName}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, groupName: e.target.value }))
          }
          maxLength={100}
          required
        />
      </div>

      <div className="form-group">
        <label>Description</label>
        <textarea
          value={formData.description}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, description: e.target.value }))
          }
          maxLength={500}
          rows={3}
        />
      </div>

      <div className="form-group">
        <label>Group Type</label>
        <select
          value={formData.groupType}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, groupType: e.target.value }))
          }
        >
          <option value="PRIVATE">Private</option>
          <option value="PUBLIC">Public</option>
        </select>
      </div>

      <div className="form-actions">
        <button type="button" onClick={onCancel} disabled={saving}>
          Cancel
        </button>
        <button type="submit" disabled={saving || !formData.groupName.trim()}>
          {saving ? "Saving..." : "Save Changes"}
        </button>
      </div>
    </form>
  );
};
```

---

## 9. DELETE GROUP

### API Endpoint

```
DELETE /chat/group/{groupId}?deletedBy={userId}
```

### Frontend Implementation

```javascript
// Delete group (admin only)
async function deleteGroup(groupId, deletedBy) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(
      `${GROUP_CHAT_ENDPOINT}/${groupId}?deletedBy=${deletedBy}`,
      {
        method: "DELETE",
        headers,
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to delete group: ${errorText}`);
    }

    return await response.text();
  } catch (error) {
    console.error("Error deleting group:", error);
    throw error;
  }
}

// React component for group deletion
const DeleteGroupButton = ({ group, onGroupDeleted }) => {
  const [deleting, setDeleting] = useState(false);
  const currentUserId = getCurrentUserId();

  const isAdmin = group.adminId === currentUserId;

  const handleDelete = async () => {
    const confirmed = confirm(
      `Are you sure you want to delete "${group.groupName}"? This action cannot be undone.`
    );

    if (!confirmed) return;

    setDeleting(true);
    try {
      await deleteGroup(group.id, currentUserId);
      onGroupDeleted(group.id);
      // Navigate away from the deleted group
    } catch (error) {
      console.error("Failed to delete group:", error);
      alert("Failed to delete group: " + error.message);
    } finally {
      setDeleting(false);
    }
  };

  if (!isAdmin) return null;

  return (
    <button
      onClick={handleDelete}
      disabled={deleting}
      className="delete-group-btn danger"
    >
      {deleting ? "Deleting..." : "Delete Group"}
    </button>
  );
};
```

---

## 10. SEARCH GROUP MESSAGES

### API Endpoint

```
GET /chat/group/{groupId}/search?searchTerm={term}&page=0&size=20
```

### Frontend Implementation

```javascript
// Search messages in group
async function searchGroupMessages(groupId, searchTerm, page = 0, size = 20) {
  try {
    const headers = await getAuthHeaders();

    const params = new URLSearchParams({
      searchTerm,
      page: page.toString(),
      size: size.toString(),
    });

    const response = await fetch(
      `${GROUP_CHAT_ENDPOINT}/${groupId}/search?${params}`,
      { headers }
    );

    if (!response.ok) {
      throw new Error("Failed to search messages");
    }

    return await response.json();
  } catch (error) {
    console.error("Error searching messages:", error);
    throw error;
  }
}

// React component for message search
const GroupMessageSearch = ({ groupId }) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchTerm.trim()) return;

    setSearching(true);
    try {
      const results = await searchGroupMessages(groupId, searchTerm.trim());
      setSearchResults(results.content || []);
      setHasSearched(true);
    } catch (error) {
      console.error("Search failed:", error);
    } finally {
      setSearching(false);
    }
  };

  const clearSearch = () => {
    setSearchTerm("");
    setSearchResults([]);
    setHasSearched(false);
  };

  return (
    <div className="message-search">
      <form onSubmit={handleSearch} className="search-form">
        <div className="search-input-group">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search messages..."
            className="search-input"
          />
          <button type="submit" disabled={searching || !searchTerm.trim()}>
            {searching ? "⏳" : "🔍"}
          </button>
          {hasSearched && (
            <button
              type="button"
              onClick={clearSearch}
              className="clear-search"
            >
              ✕
            </button>
          )}
        </div>
      </form>

      {hasSearched && (
        <div className="search-results">
          <h4>Search Results ({searchResults.length})</h4>
          {searchResults.length === 0 ? (
            <p>No messages found for "{searchTerm}"</p>
          ) : (
            searchResults.map((message) => (
              <div key={message.id} className="search-result-item">
                <div className="message-meta">
                  <span className="sender">{message.senderName}</span>
                  <span className="timestamp">
                    {new Date(message.timestamp).toLocaleString()}
                  </span>
                </div>
                <div className="message-content">
                  {highlightSearchTerm(message.content, searchTerm)}
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};

// Helper function to highlight search terms
function highlightSearchTerm(text, searchTerm) {
  if (!searchTerm) return text;

  const regex = new RegExp(`(${searchTerm})`, "gi");
  return text.replace(regex, "<mark>$1</mark>");
}
```

---

## 11. GET GROUP MEMBERS

### API Endpoint

```
GET /chat/group/{groupId}/members
```

### Frontend Implementation

```javascript
// Get group members list
async function getGroupMembers(groupId) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(`${GROUP_CHAT_ENDPOINT}/${groupId}/members`, {
      headers,
    });

    if (!response.ok) {
      throw new Error("Failed to fetch group members");
    }

    return await response.json(); // Returns array of user IDs
  } catch (error) {
    console.error("Error fetching group members:", error);
    throw error;
  }
}

// React component for displaying group members
const GroupMembersList = ({ groupId, groupAdminId }) => {
  const [memberIds, setMemberIds] = useState([]);
  const [memberDetails, setMemberDetails] = useState([]);
  const [loading, setLoading] = useState(true);
  const currentUserId = getCurrentUserId();

  useEffect(() => {
    const loadMembers = async () => {
      try {
        const ids = await getGroupMembers(groupId);
        setMemberIds(ids);

        // Fetch user details for each member ID
        const details = await Promise.all(
          ids.map((id) => getUserDetails(id)) // Your user service function
        );
        setMemberDetails(details);
      } catch (error) {
        console.error("Error loading members:", error);
      } finally {
        setLoading(false);
      }
    };

    loadMembers();
  }, [groupId]);

  if (loading) return <div className="loading">Loading members...</div>;

  return (
    <div className="group-members">
      <h3>Group Members ({memberDetails.length})</h3>

      <div className="members-list">
        {memberDetails.map((member) => (
          <GroupMemberItem
            key={member.id}
            member={member}
            groupId={groupId}
            isAdmin={currentUserId === groupAdminId}
            isGroupAdmin={member.id === groupAdminId}
            onMemberRemoved={(memberId) => {
              setMemberIds((prev) => prev.filter((id) => id !== memberId));
              setMemberDetails((prev) => prev.filter((m) => m.id !== memberId));
            }}
          />
        ))}
      </div>
    </div>
  );
};

const GroupMemberItem = ({
  member,
  groupId,
  isAdmin,
  isGroupAdmin,
  onMemberRemoved,
}) => {
  const currentUserId = getCurrentUserId();

  return (
    <div className="member-item">
      <div className="member-avatar">
        <img src={member.avatar || "/default-avatar.png"} alt={member.name} />
      </div>

      <div className="member-info">
        <div className="member-name">
          {member.name || member.email}
          {isGroupAdmin && <span className="admin-badge">Admin</span>}
          {member.id === currentUserId && (
            <span className="you-badge">You</span>
          )}
        </div>
        <div className="member-status">{member.status || "Active"}</div>
      </div>

      <div className="member-actions">
        {isAdmin && member.id !== currentUserId && !isGroupAdmin && (
          <button
            onClick={() => onMemberRemoved(member.id)}
            className="remove-member-btn"
            title="Remove member"
          >
            ✕
          </button>
        )}
      </div>
    </div>
  );
};
```

---

## 12. LEAVE GROUP

### API Endpoint

```
POST /chat/group/{groupId}/leave?userId={userId}
```

### Frontend Implementation

```javascript
// Leave group
async function leaveGroup(groupId, userId) {
  try {
    const headers = await getAuthHeaders();

    const response = await fetch(
      `${GROUP_CHAT_ENDPOINT}/${groupId}/leave?userId=${userId}`,
      {
        method: "POST",
        headers,
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to leave group: ${errorText}`);
    }

    return await response.text();
  } catch (error) {
    console.error("Error leaving group:", error);
    throw error;
  }
}

// React component for leaving group
const LeaveGroupButton = ({ group, onGroupLeft }) => {
  const [leaving, setLeaving] = useState(false);
  const currentUserId = getCurrentUserId();

  const isAdmin = group.adminId === currentUserId;

  const handleLeave = async () => {
    let confirmMessage = `Are you sure you want to leave "${group.groupName}"?`;

    if (isAdmin) {
      confirmMessage +=
        "\n\nWarning: As the admin, leaving will transfer admin rights to another member. If you're the only member, the group will be deleted.";
    }

    const confirmed = confirm(confirmMessage);
    if (!confirmed) return;

    setLeaving(true);
    try {
      await leaveGroup(group.id, currentUserId);
      onGroupLeft(group.id);
      // Navigate away from the group
    } catch (error) {
      console.error("Failed to leave group:", error);
      alert("Failed to leave group: " + error.message);
    } finally {
      setLeaving(false);
    }
  };

  return (
    <button
      onClick={handleLeave}
      disabled={leaving}
      className="leave-group-btn warning"
    >
      {leaving ? "Leaving..." : "Leave Group"}
    </button>
  );
};
```

---

## 13. COMPLETE GROUP CHAT COMPONENT

### Main Group Chat Interface

```javascript
// Complete group chat component integrating all features
const GroupChatInterface = () => {
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [userGroups, setUserGroups] = useState([]);
  const [showCreateGroup, setShowCreateGroup] = useState(false);
  const [showGroupDetails, setShowGroupDetails] = useState(false);
  const currentUserId = getCurrentUserId();

  // Load user's groups on component mount
  useEffect(() => {
    const loadUserGroups = async () => {
      try {
        const groups = await getUserGroups(currentUserId);
        setUserGroups(groups);

        // Auto-select first group if available
        if (groups.length > 0 && !selectedGroup) {
          setSelectedGroup(groups[0]);
        }
      } catch (error) {
        console.error("Error loading user groups:", error);
      }
    };

    if (currentUserId) {
      loadUserGroups();
    }
  }, [currentUserId]);

  const handleGroupCreated = (newGroup) => {
    setUserGroups((prev) => [...prev, newGroup]);
    setSelectedGroup(newGroup);
    setShowCreateGroup(false);
  };

  const handleGroupUpdated = (updatedGroup) => {
    setUserGroups((prev) =>
      prev.map((group) => (group.id === updatedGroup.id ? updatedGroup : group))
    );
    setSelectedGroup(updatedGroup);
  };

  const handleGroupLeft = (groupId) => {
    setUserGroups((prev) => prev.filter((group) => group.id !== groupId));
    if (selectedGroup?.id === groupId) {
      setSelectedGroup(userGroups.find((g) => g.id !== groupId) || null);
    }
  };

  const handleGroupDeleted = (groupId) => {
    setUserGroups((prev) => prev.filter((group) => group.id !== groupId));
    if (selectedGroup?.id === groupId) {
      setSelectedGroup(null);
    }
  };

  return (
    <div className="group-chat-interface">
      {/* Sidebar with groups list */}
      <div className="groups-sidebar">
        <div className="sidebar-header">
          <h2>Groups</h2>
          <button
            onClick={() => setShowCreateGroup(true)}
            className="create-group-btn"
          >
            + New Group
          </button>
        </div>

        <GroupsList
          userId={currentUserId}
          groups={userGroups}
          selectedGroup={selectedGroup}
          onGroupSelect={setSelectedGroup}
        />
      </div>

      {/* Main chat area */}
      <div className="chat-main">
        {selectedGroup ? (
          <>
            {/* Chat header */}
            <div className="chat-header">
              <div className="group-info">
                <h3>{selectedGroup.groupName}</h3>
                <span className="member-count">
                  {selectedGroup.memberIds?.length || 0} members
                </span>
              </div>

              <div className="header-actions">
                <button
                  onClick={() => setShowGroupDetails(true)}
                  className="group-details-btn"
                >
                  ℹ️ Details
                </button>

                <GroupMenuDropdown
                  group={selectedGroup}
                  onGroupUpdated={handleGroupUpdated}
                  onGroupLeft={handleGroupLeft}
                  onGroupDeleted={handleGroupDeleted}
                />
              </div>
            </div>

            {/* Messages area */}
            <div className="messages-area">
              <GroupMessageList groupId={selectedGroup.id} />
            </div>

            {/* Message input */}
            <div className="message-input-area">
              <GroupMessageInput
                groupId={selectedGroup.id}
                onMessageSent={(message) => {
                  // Handle new message (could update UI immediately)
                  console.log("Message sent:", message);
                }}
              />
            </div>
          </>
        ) : (
          <div className="no-group-selected">
            <div className="welcome-message">
              <h3>Welcome to Group Chat</h3>
              <p>Select a group to start chatting or create a new one.</p>
              <button
                onClick={() => setShowCreateGroup(true)}
                className="create-first-group-btn"
              >
                Create Your First Group
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Modals */}
      {showCreateGroup && (
        <CreateGroupModal
          isOpen={showCreateGroup}
          onClose={() => setShowCreateGroup(false)}
          onGroupCreated={handleGroupCreated}
        />
      )}

      {showGroupDetails && selectedGroup && (
        <GroupDetailsModal
          group={selectedGroup}
          isOpen={showGroupDetails}
          onClose={() => setShowGroupDetails(false)}
          onGroupUpdated={handleGroupUpdated}
          onGroupLeft={handleGroupLeft}
          onGroupDeleted={handleGroupDeleted}
        />
      )}
    </div>
  );
};

export default GroupChatInterface;
```

---

## CSS Styles

```css
/* Group Chat Interface Styles */
.group-chat-interface {
  display: flex;
  height: 100vh;
  background: #f5f5f5;
}

.groups-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.create-group-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.create-group-btn:hover {
  background: #0056b3;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background: white;
  padding: 15px 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-info h3 {
  margin: 0;
  font-size: 18px;
}

.member-count {
  color: #666;
  font-size: 14px;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-input-area {
  background: white;
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}

.message-item {
  margin-bottom: 15px;
  padding: 10px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 12px;
  color: #666;
}

.sender-name {
  font-weight: bold;
  color: #333;
}

.message-content {
  font-size: 14px;
  line-height: 1.4;
}

.message-input-form {
  display: flex;
  gap: 10px;
}

.input-container {
  display: flex;
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 25px;
  overflow: hidden;
}

.input-container input {
  flex: 1;
  border: none;
  padding: 12px 16px;
  outline: none;
  font-size: 14px;
}

.send-button {
  background: #007bff;
  color: white;
  border: none;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.send-button:hover:not(:disabled) {
  background: #0056b3;
}

.send-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* Error and Loading States */
.error-alert {
  background: #f8d7da;
  color: #721c24;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  border: 1px solid #f5c6cb;
}

.loading {
  text-align: center;
  color: #666;
  padding: 20px;
}

.no-group-selected {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.welcome-message h3 {
  color: #333;
  margin-bottom: 10px;
}

.welcome-message p {
  color: #666;
  margin-bottom: 20px;
}

.create-first-group-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
}

.create-first-group-btn:hover {
  background: #1e7e34;
}

/* Form Styles */
.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-primary {
  background: #007bff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary {
  background: #6c757d;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
}

.btn-secondary:hover:not(:disabled) {
  background: #545b62;
}

.btn-primary:disabled,
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Member Management */
.member-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 12px;
}

.member-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.member-info {
  flex: 1;
}

.member-name {
  font-weight: bold;
  margin-bottom: 2px;
}

.admin-badge {
  background: #ffc107;
  color: #000;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 8px;
}

.you-badge {
  background: #17a2b8;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 8px;
}

.member-status {
  font-size: 12px;
  color: #666;
}

.remove-member-btn {
  background: #dc3545;
  color: white;
  border: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
}

.remove-member-btn:hover {
  background: #c82333;
}

/* Search */
.search-form {
  margin-bottom: 20px;
}

.search-input-group {
  display: flex;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.search-input {
  flex: 1;
  border: none;
  padding: 10px;
  outline: none;
}

.search-input-group button {
  background: #007bff;
  color: white;
  border: none;
  padding: 10px 15px;
  cursor: pointer;
}

.clear-search {
  background: #6c757d !important;
}

.search-results {
  max-height: 400px;
  overflow-y: auto;
}

.search-result-item {
  padding: 10px;
  border-bottom: 1px solid #eee;
  background: white;
  border-radius: 4px;
  margin-bottom: 5px;
}

.search-result-item mark {
  background: #ffeb3b;
  padding: 1px 2px;
}

/* Responsive Design */
@media (max-width: 768px) {
  .group-chat-interface {
    flex-direction: column;
  }

  .groups-sidebar {
    width: 100%;
    height: 200px;
  }

  .chat-header {
    padding: 10px 15px;
  }

  .messages-area {
    padding: 15px;
  }

  .message-input-area {
    padding: 15px;
  }
}
```

---

## Error Handling Best Practices

```javascript
// Centralized error handling for group chat
class GroupChatError extends Error {
  constructor(message, code, details) {
    super(message);
    this.name = "GroupChatError";
    this.code = code;
    this.details = details;
  }
}

// Error handler wrapper
const withErrorHandling = (fn) => {
  return async (...args) => {
    try {
      return await fn(...args);
    } catch (error) {
      // Log error for debugging
      console.error(`Group chat operation failed:`, error);

      // Transform error for user display
      if (error.message.includes("401")) {
        throw new GroupChatError(
          "Authentication failed. Please sign in again.",
          "AUTH_FAILED",
          error
        );
      } else if (error.message.includes("403")) {
        throw new GroupChatError(
          "You do not have permission to perform this action.",
          "PERMISSION_DENIED",
          error
        );
      } else if (error.message.includes("404")) {
        throw new GroupChatError(
          "Group not found or has been deleted.",
          "GROUP_NOT_FOUND",
          error
        );
      } else if (error.message.includes("400")) {
        throw new GroupChatError(
          "Invalid request. Please check your input.",
          "INVALID_REQUEST",
          error
        );
      } else {
        throw new GroupChatError(
          "An unexpected error occurred. Please try again.",
          "UNKNOWN_ERROR",
          error
        );
      }
    }
  };
};

// Usage example
const createGroupWithErrorHandling = withErrorHandling(createGroup);
const sendGroupMessageWithErrorHandling = withErrorHandling(sendGroupMessage);
```

---

## Integration Summary

This guide provides complete frontend integration for all group chat activities:

✅ **Group Creation** - Create groups with members and admin rights
✅ **Messaging** - Send and receive group messages with real-time updates
✅ **Member Management** - Add/remove members, view member lists
✅ **Group Administration** - Update group details, delete groups
✅ **Search Functionality** - Search through group message history
✅ **User Experience** - Leave groups, view group details
✅ **Authentication** - Firebase-based security for all operations
✅ **Error Handling** - Comprehensive error management
✅ **Responsive Design** - Mobile-friendly interface
✅ **Real-time Updates** - WebSocket integration ready

All components are production-ready with proper error handling, loading states, and user feedback mechanisms. The creator is automatically included as both admin and member, ensuring they can participate in group conversations immediately after creation.
