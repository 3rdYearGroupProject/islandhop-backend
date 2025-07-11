# Chat and Notification Microservice - Database Instructions

## Overview

This document provides comprehensive instructions on how the database works for the Chat and Notification Microservice. The service uses a hybrid database architecture with MongoDB for chat messages and PostgreSQL for notifications.

## Database Architecture

### Hybrid Database Design

The microservice employs a **multi-database architecture** to optimize performance and data characteristics:

1. **MongoDB** - For chat messages (personal and group)

   - Optimized for high-volume, real-time messaging
   - Flexible document structure for different message types
   - Excellent read/write performance for chat operations

2. **PostgreSQL** - For notifications
   - ACID compliance for critical notification data
   - Strong consistency for notification delivery tracking
   - Advanced querying capabilities for notification management

## MongoDB Configuration

### Connection Settings

```properties
# MongoDB Configuration (application.properties)
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/chat_db?retryWrites=true&w=majority
spring.data.mongodb.database=chat_db
```

### Collections Structure

#### 1. Personal Messages Collection (`personal_messages`)

**Document Structure:**

```json
{
  "_id": "ObjectId",
  "senderId": "string",
  "receiverId": "string",
  "content": "string",
  "messageType": "TEXT|IMAGE|FILE|VOICE",
  "timestamp": "ISODate",
  "isRead": "boolean",
  "isDelivered": "boolean",
  "metadata": {
    "fileUrl": "string",
    "fileName": "string",
    "fileSize": "number"
  }
}
```

**Indexes:**

```javascript
// Compound index for efficient conversation queries
db.personal_messages.createIndex({
  senderId: 1,
  receiverId: 1,
  timestamp: -1,
});

// Index for unread message queries
db.personal_messages.createIndex({
  receiverId: 1,
  isRead: 1,
  timestamp: -1,
});

// Text index for message search
db.personal_messages.createIndex({
  content: "text",
});
```

#### 2. Groups Collection (`groups`)

**Document Structure:**

```json
{
  "_id": "ObjectId",
  "groupName": "string",
  "description": "string",
  "groupType": "PUBLIC|PRIVATE",
  "createdBy": "string",
  "members": ["string"],
  "createdAt": "ISODate",
  "updatedAt": "ISODate",
  "isActive": "boolean",
  "settings": {
    "allowMemberInvite": "boolean",
    "muteNotifications": "boolean"
  }
}
```

**Indexes:**

```javascript
// Index for member queries
db.groups.createIndex({
  members: 1,
  isActive: 1,
});

// Index for group search
db.groups.createIndex({
  groupName: "text",
  description: "text",
});
```

#### 3. Group Messages Collection (`group_messages`)

**Document Structure:**

```json
{
  "_id": "ObjectId",
  "groupId": "string",
  "senderId": "string",
  "content": "string",
  "messageType": "TEXT|IMAGE|FILE|VOICE",
  "timestamp": "ISODate",
  "readBy": ["string"],
  "metadata": {
    "fileUrl": "string",
    "fileName": "string",
    "fileSize": "number"
  }
}
```

**Indexes:**

```javascript
// Index for group message retrieval
db.group_messages.createIndex({
  groupId: 1,
  timestamp: -1,
});

// Text index for group message search
db.group_messages.createIndex({
  content: "text",
});
```

### MongoDB Query Examples

#### Common Chat Operations

```javascript
// Get conversation between two users (latest 20 messages)
db.personal_messages
  .find({
    $or: [
      { senderId: "user123", receiverId: "user456" },
      { senderId: "user456", receiverId: "user123" },
    ],
  })
  .sort({ timestamp: -1 })
  .limit(20);

// Get unread messages for a user
db.personal_messages
  .find({
    receiverId: "user123",
    isRead: false,
  })
  .sort({ timestamp: -1 });

// Search messages by content
db.personal_messages.find({
  $or: [
    { senderId: "user123", receiverId: "user456" },
    { senderId: "user456", receiverId: "user123" },
  ],
  $text: { $search: "hello world" },
});

// Get groups for a user
db.groups.find({
  members: "user123",
  isActive: true,
});

// Get latest group messages
db.group_messages
  .find({
    groupId: "group123",
  })
  .sort({ timestamp: -1 })
  .limit(50);
```

## PostgreSQL Configuration

### Connection Settings

```properties
# PostgreSQL Configuration (application.properties)
spring.datasource.url=jdbc:postgresql://localhost:5432/notifications_db
spring.datasource.username=username
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Table Structure

#### Notifications Table

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    related_entity_id VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);
```

**Indexes:**

```sql
-- Index for user notification queries
CREATE INDEX idx_notifications_user_id ON notifications(user_id);

-- Index for unread notifications
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read);

-- Index for notification type queries
CREATE INDEX idx_notifications_type ON notifications(user_id, type);

-- Index for cleanup operations
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
```

### PostgreSQL Query Examples

```sql
-- Get all notifications for a user
SELECT * FROM notifications
WHERE user_id = 'user123'
ORDER BY created_at DESC;

-- Get unread notifications
SELECT * FROM notifications
WHERE user_id = 'user123'
AND is_read = FALSE
ORDER BY created_at DESC;

-- Get notification count by type
SELECT type, COUNT(*) as count
FROM notifications
WHERE user_id = 'user123'
GROUP BY type;

-- Mark all notifications as read
UPDATE notifications
SET is_read = TRUE, read_at = CURRENT_TIMESTAMP
WHERE user_id = 'user123'
AND is_read = FALSE;

-- Cleanup old notifications (older than 30 days)
DELETE FROM notifications
WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '30 days';
```

## Database Operations Guide

### Setting Up Databases

#### MongoDB Setup

1. **Create MongoDB Atlas Cluster** (Recommended for production)

   ```bash
   # Or use local MongoDB
   mongod --dbpath /path/to/data/directory
   ```

2. **Create Database and Collections**

   ```javascript
   use chat_db;

   // Collections are created automatically when first document is inserted
   db.personal_messages.insertOne({});
   db.groups.insertOne({});
   db.group_messages.insertOne({});
   ```

3. **Create Indexes**
   ```javascript
   // Run the index creation commands shown above
   ```

#### PostgreSQL Setup

1. **Create Database**

   ```sql
   CREATE DATABASE notifications_db;
   ```

2. **Create User and Grant Permissions**

   ```sql
   CREATE USER chat_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE notifications_db TO chat_user;
   ```

3. **Run Application** (Tables will be created automatically via Hibernate)

### Data Migration Scripts

#### MongoDB Data Migration

```javascript
// Migrate existing chat data
db.old_messages.find().forEach(function (doc) {
  db.personal_messages.insertOne({
    senderId: doc.sender_id,
    receiverId: doc.receiver_id,
    content: doc.message_content,
    messageType: doc.message_type || "TEXT",
    timestamp: doc.created_at,
    isRead: doc.is_read || false,
    isDelivered: doc.is_delivered || true,
  });
});
```

#### PostgreSQL Data Migration

```sql
-- Migrate existing notification data
INSERT INTO notifications (user_id, title, message, type, priority, is_read, created_at)
SELECT
    user_id,
    notification_title,
    notification_message,
    notification_type,
    COALESCE(priority, 'MEDIUM'),
    COALESCE(is_read, FALSE),
    created_at
FROM old_notifications_table;
```

### Backup and Recovery

#### MongoDB Backup

```bash
# Full database backup
mongodump --uri="mongodb+srv://username:password@cluster.mongodb.net/chat_db" --out=/backup/path

# Specific collection backup
mongodump --uri="mongodb+srv://username:password@cluster.mongodb.net/chat_db" --collection=personal_messages --out=/backup/path

# Restore
mongorestore --uri="mongodb+srv://username:password@cluster.mongodb.net/chat_db" /backup/path/chat_db
```

#### PostgreSQL Backup

```bash
# Full database backup
pg_dump -h localhost -U chat_user -d notifications_db > notifications_backup.sql

# Restore
psql -h localhost -U chat_user -d notifications_db < notifications_backup.sql
```

### Performance Optimization

#### MongoDB Optimization

1. **Index Optimization**

   ```javascript
   // Analyze query patterns
   db.personal_messages
     .explain("executionStats")
     .find({
       senderId: "user123",
       receiverId: "user456",
     })
     .sort({ timestamp: -1 });

   // Monitor index usage
   db.personal_messages.aggregate([{ $indexStats: {} }]);
   ```

2. **Query Optimization**

   ```javascript
   // Use projection to limit returned fields
   db.personal_messages.find(
     { receiverId: "user123" },
     { content: 1, senderId: 1, timestamp: 1 }
   );

   // Use aggregation for complex queries
   db.personal_messages.aggregate([
     { $match: { receiverId: "user123" } },
     {
       $group: {
         _id: "$senderId",
         lastMessage: { $last: "$content" },
         count: { $sum: 1 },
       },
     },
   ]);
   ```

#### PostgreSQL Optimization

1. **Query Optimization**

   ```sql
   -- Use EXPLAIN to analyze query performance
   EXPLAIN ANALYZE
   SELECT * FROM notifications
   WHERE user_id = 'user123'
   AND is_read = FALSE;

   -- Use appropriate indexes
   CREATE INDEX CONCURRENTLY idx_notifications_composite
   ON notifications(user_id, is_read, created_at DESC);
   ```

2. **Connection Pooling**
   ```properties
   # HikariCP configuration
   spring.datasource.hikari.maximum-pool-size=20
   spring.datasource.hikari.minimum-idle=5
   spring.datasource.hikari.connection-timeout=20000
   spring.datasource.hikari.idle-timeout=300000
   ```

### Monitoring and Maintenance

#### MongoDB Monitoring

```javascript
// Check database stats
db.stats();

// Check collection stats
db.personal_messages.stats();

// Monitor slow queries
db.setProfilingLevel(2, { slowms: 100 });
db.system.profile.find().sort({ ts: -1 }).limit(5);
```

#### PostgreSQL Monitoring

```sql
-- Check database size
SELECT pg_size_pretty(pg_database_size('notifications_db'));

-- Monitor active connections
SELECT count(*) FROM pg_stat_activity;

-- Check slow queries
SELECT query, mean_time, calls
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

### Data Cleanup Strategies

#### MongoDB Cleanup

```javascript
// Remove old messages (older than 1 year)
db.personal_messages.deleteMany({
  timestamp: {
    $lt: new Date(new Date().setFullYear(new Date().getFullYear() - 1)),
  },
});

// Archive old group messages
db.group_messages.aggregate([
  {
    $match: {
      timestamp: {
        $lt: new Date(new Date().setMonth(new Date().getMonth() - 6)),
      },
    },
  },
  { $out: "archived_group_messages" },
]);
```

#### PostgreSQL Cleanup

```sql
-- Archive old notifications
INSERT INTO archived_notifications
SELECT * FROM notifications
WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '90 days';

DELETE FROM notifications
WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '90 days';

-- Vacuum and analyze
VACUUM ANALYZE notifications;
```

### Troubleshooting

#### Common MongoDB Issues

1. **Connection Issues**

   ```bash
   # Check connection string
   mongo "mongodb+srv://cluster.mongodb.net/chat_db" --username username

   # Check network connectivity
   telnet cluster.mongodb.net 27017
   ```

2. **Performance Issues**

   ```javascript
   // Check if indexes are being used
   db.personal_messages.find({ senderId: "user123" }).explain("executionStats");

   // Monitor memory usage
   db.serverStatus().mem;
   ```

#### Common PostgreSQL Issues

1. **Connection Issues**

   ```bash
   # Check PostgreSQL status
   sudo systemctl status postgresql

   # Test connection
   psql -h localhost -U chat_user -d notifications_db -c "SELECT version();"
   ```

2. **Performance Issues**

   ```sql
   -- Check for locks
   SELECT * FROM pg_locks WHERE NOT granted;

   -- Check table bloat
   SELECT schemaname, tablename,
          pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
   FROM pg_tables
   WHERE schemaname = 'public';
   ```

### Security Considerations

#### MongoDB Security

1. **Authentication and Authorization**

   ```javascript
   // Create application user with limited permissions
   use chat_db;
   db.createUser({
     user: "chat_app",
     pwd: "secure_password",
     roles: [
       { role: "readWrite", db: "chat_db" }
     ]
   });
   ```

2. **Network Security**
   - Use MongoDB Atlas for managed security
   - Configure IP whitelisting
   - Enable SSL/TLS encryption

#### PostgreSQL Security

1. **User Management**

   ```sql
   -- Create limited privilege user
   CREATE ROLE chat_app_role LOGIN PASSWORD 'secure_password';
   GRANT SELECT, INSERT, UPDATE, DELETE ON notifications TO chat_app_role;
   ```

2. **Connection Security**
   ```properties
   # Enable SSL
   spring.datasource.url=jdbc:postgresql://localhost:5432/notifications_db?sslmode=require
   ```

This comprehensive database guide provides all the necessary information to understand, set up, maintain, and troubleshoot the database layer of the Chat and Notification Microservice.
