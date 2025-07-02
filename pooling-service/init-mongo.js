// MongoDB initialization script for pooling service
db = db.getSiblingDB('pooling');

// Create collections with proper indexes
db.createCollection('tripPools');

// Create indexes for better query performance
db.tripPools.createIndex({ "createdAt": 1 });
db.tripPools.createIndex({ "status": 1 });
db.tripPools.createIndex({ "public": 1 });
db.tripPools.createIndex({ "baseCity": 1 });
db.tripPools.createIndex({ "startDate": 1 });
db.tripPools.createIndex({ "endDate": 1 });
db.tripPools.createIndex({ "members.userId": 1 });

// Create compound indexes for pooling queries
db.tripPools.createIndex({ 
  "baseCity": 1, 
  "startDate": 1, 
  "endDate": 1,
  "status": 1,
  "public": 1
});

print('MongoDB initialized for pooling service');
