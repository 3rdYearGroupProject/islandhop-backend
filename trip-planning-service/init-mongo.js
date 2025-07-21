// MongoDB initialization script
// This script runs when the MongoDB container starts for the first time

// Switch to the application database
db = db.getSiblingDB('islandhop_trips');

// Create a user for the application
db.createUser({
  user: 'tripplanner',
  pwd: 'tripplanner123',
  roles: [
    {
      role: 'readWrite',
      db: 'islandhop_trips'
    }
  ]
});

// Create collections with indexes
print('Creating trips collection...');
db.createCollection('trips');
db.trips.createIndex({ "userId": 1 });
db.trips.createIndex({ "tripId": 1, "userId": 1 }, { unique: true });
db.trips.createIndex({ "categories": 1 });
db.trips.createIndex({ "pacing": 1 });
db.trips.createIndex({ "startDate": 1 });
db.trips.createIndex({ "status": 1 });
db.trips.createIndex({ "baseCity": 1 });

print('Creating user_preferences collection...');
db.createCollection('user_preferences');
db.user_preferences.createIndex({ "userId": 1 }, { unique: true });
db.user_preferences.createIndex({ "similarUsers": 1 });

// Insert sample data
print('Inserting sample trip data...');
db.trips.insertMany([
  {
    "tripId": "sample_trip_001",
    "userId": "user@example.com",
    "tripName": "Cultural Heritage Tour of Sri Lanka",
    "startDate": new Date("2025-08-01"),
    "endDate": new Date("2025-08-05"),
    "arrivalTime": "14:30",
    "baseCity": "Colombo",
    "multiCity": true,
    "categories": ["Culture", "Nature"],
    "pacing": "NORMAL",
    "status": "PLANNING",
    "places": [
      {
        "placeId": "place_001",
        "name": "Gangaramaya Temple",
        "city": "Colombo",
        "latitude": 6.9162,
        "longitude": 79.8562,
        "description": "Famous Buddhist temple in heart of Colombo",
        "categories": ["Culture", "Religion"],
        "type": "ATTRACTION",
        "estimatedVisitDurationMinutes": 90,
        "dayNumber": 1,
        "orderInDay": 1,
        "rating": 4.3,
        "reviewCount": 1250,
        "priceLevel": "FREE",
        "userAdded": false,
        "confirmed": true
      },
      {
        "placeId": "place_002",
        "name": "National Museum of Colombo",
        "city": "Colombo",
        "latitude": 6.9094,
        "longitude": 79.8606,
        "description": "Premier museum showcasing Sri Lankan history",
        "categories": ["Culture", "History"],
        "type": "ATTRACTION",
        "estimatedVisitDurationMinutes": 120,
        "dayNumber": 1,
        "orderInDay": 2,
        "rating": 4.1,
        "reviewCount": 890,
        "priceLevel": "BUDGET",
        "userAdded": false,
        "confirmed": true
      }
    ],
    "dayPlans": [],
    "statistics": {
      "totalDays": 5,
      "totalPlaces": 2,
      "totalDistanceKm": 2.5,
      "totalTravelTimeMinutes": 15,
      "totalVisitTimeMinutes": 210,
      "predominantCategory": "Culture",
      "citiesVisited": 1
    },
    "preferences": {
      "maxDailyTravelHours": 8,
      "bufferTimeMinutes": 30,
      "maxAttractionsPerDay": 4
    },
    "excludedAttractions": [],
    "createdAt": new Date(),
    "updatedAt": new Date()
  }
]);

print('Inserting sample user preferences...');
db.user_preferences.insertMany([
  {
    "userId": "user@example.com",
    "categoryPreferences": {
      "Culture": 8,
      "Nature": 6,
      "Adventure": 3,
      "Leisure": 5
    },
    "locationPreferences": {
      "Colombo": 0.9,
      "Kandy": 0.85,
      "Galle": 0.7
    },
    "visitedAttractions": {
      "place_001": 2,
      "place_002": 1
    },
    "averageDailyBudget": 75.0,
    "averageAttractionsPerDay": 3,
    "preferredTravelModes": ["TAXI", "BUS", "WALKING"],
    "preferredPacing": "NORMAL",
    "similarUsers": [],
    "attractionRatings": {
      "place_001": 0.85,
      "place_002": 0.75
    },
    "lastUpdated": new Date()
  }
]);

print('MongoDB initialization completed successfully!');
print('Database: islandhop_trips');
print('Collections created: trips, user_preferences');
print('Sample data inserted for testing');
