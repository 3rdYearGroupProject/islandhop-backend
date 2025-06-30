# Trip Pooling Service

A sophisticated trip pooling service for the IslandHop tourism platform that matches tourists with compatible travel plans using advanced timeline-based algorithms.

## 🎯 Features

### Timeline-Based Pooling Algorithm
- **Smart Date Matching**: Finds trips with overlapping timelines (±1-2 days flexibility)
- **Route Compatibility**: Analyzes geographic proximity and shared destinations
- **Compatibility Scoring**: Multi-factor scoring based on:
  - Timeline overlap (25% weight)
  - Interest compatibility (30% weight) 
  - Travel pacing (20% weight)
  - Demographics (15% weight)
  - Base city compatibility (10% weight)

### Core Functionality
- **Pool Creation**: Users can create pools for their trips
- **Smart Matching**: Find compatible travel partners automatically
- **Join Existing Pools**: Browse and join compatible existing pools
- **Pool Management**: Leave pools, transfer ownership
- **Real-time Updates**: Track pool status and member changes

### Integration
- **Trip Planning Service**: Fetches trip data, routes, and preferences
- **User Service**: Retrieves tourist profiles and demographics
- **MongoDB**: Stores pool data and member information

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB 4.4+
- Trip Planning Service running on port 8082
- User Services running on port 8081

### Installation

1. **Navigate to pooling service**:
   ```bash
   cd pooling-service
   ```

2. **Configure application.properties**:
   ```properties
   # MongoDB
   spring.data.mongodb.uri=mongodb://localhost:27017/islandhop-pooling
   
   # External Services
   services.trip-planning.url=http://localhost:8082
   services.user-services.url=http://localhost:8081
   
   # Algorithm Configuration
   pooling.timeline.flexibility-days=2
   pooling.compatibility.min-score=0.6
   ```

3. **Build and run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Service runs on**: `http://localhost:8086`

### Using Docker

```bash
docker build -t pooling-service .
docker run -p 8086:8086 pooling-service
```

## 📡 API Endpoints

### Core Pooling Operations

#### Find Pool Matches
```http
POST /pooling/find-matches
Content-Type: application/json

{
  "userId": "user@example.com",
  "tripId": "trip123",
  "startDate": "2025-07-15",
  "endDate": "2025-07-22",
  "baseCity": "Colombo",
  "interests": ["Adventure", "Culture", "Nature"],
  "activityPacing": "MODERATE",
  "dateFlexibilityDays": 2,
  "maxDistanceKm": 50,
  "minCompatibilityScore": 0.6
}
```

#### Create Pool
```http
POST /pooling/create-pool
Content-Type: application/json

{
  "userId": "user@example.com",
  "tripId": "trip123",
  "poolName": "Sri Lanka Adventure Group",
  "description": "Looking for travel buddies for cultural exploration"
}
```

#### Join Pool
```http
POST /pooling/join-pool/{poolId}
Content-Type: application/json

{
  "userId": "user@example.com",
  "tripId": "trip456"
}
```

#### Get User Pools
```http
GET /pooling/my-pools/{userId}
```

#### Leave Pool
```http
POST /pooling/leave-pool/{poolId}
Content-Type: application/json

{
  "userId": "user@example.com"
}
```

### Utility Endpoints

#### Health Check
```http
GET /pooling/health
```

#### Service Status
```http
GET /pooling/status
```

## 🧮 Algorithm Details

### Timeline-Based Matching Process

1. **Trip Data Extraction**
   - Fetch user's trip from trip-planning-service
   - Get tourist profile from user-services
   - Extract trip timeline, destinations, preferences

2. **Candidate Discovery**
   - Find trips with overlapping dates (±flexibility days)
   - Filter by base city proximity
   - Exclude user's own trips

3. **Compatibility Analysis**
   ```
   Overall Score = 
     Timeline Overlap × 0.25 +
     Interest Similarity × 0.30 +
     Pacing Compatibility × 0.20 +
     Demographics × 0.15 +
     Base City Match × 0.10
   ```

4. **Route Similarity**
   - Calculate geographic overlap of planned destinations
   - Measure proximity using Haversine formula
   - Weight by city overlap and place proximity

5. **Ranking & Filtering**
   - Sort by overall compatibility score
   - Filter by minimum threshold (default: 0.6)
   - Return top matches with detailed explanations

### Compatibility Scoring Components

#### Timeline Compatibility
- Calculates date overlap percentage
- Considers flexibility days
- Higher scores for longer overlaps

#### Interest Compatibility
- Jaccard similarity of trip categories
- Adventure, Culture, Nature, Leisure preferences
- Measures intersection/union ratio

#### Pacing Compatibility
```
RELAXED ↔ RELAXED: 1.0
RELAXED ↔ NORMAL:  0.7
RELAXED ↔ ACTIVE:  0.3
NORMAL  ↔ NORMAL:  1.0
NORMAL  ↔ ACTIVE:  0.7
ACTIVE  ↔ ACTIVE:  1.0
```

#### Demographic Compatibility
- Language overlap (high weight)
- Nationality similarity (moderate weight)
- Cultural considerations

#### Geographic Compatibility
- Same base city: 1.0
- Different base city: 0.3
- Route overlap analysis

## 🏗️ Architecture

### Service Integration
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Trip Planning  │    │  Pooling Service │    │  User Services  │
│    Service      │◄───┤                  ├───►│                 │
│   (Port 8082)   │    │   (Port 8086)    │    │   (Port 8081)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌──────────────┐
                       │   MongoDB    │
                       │  (Pools DB)  │
                       └──────────────┘
```

### Data Models

#### TripPool
- Pool metadata and configuration
- Member list and roles
- Timeline and location data
- Compatibility statistics

#### PoolMember
- User profile integration
- Trip association
- Compatibility scores
- Participation status

#### Compatibility Scores
- Multi-factor scoring breakdown
- Detailed reasoning
- Warning identification

## 🔧 Configuration

### Algorithm Tuning
```properties
# Timeline flexibility (days)
pooling.timeline.flexibility-days=2

# Geographic constraints
pooling.geographic.max-distance-km=50

# Compatibility thresholds
pooling.compatibility.min-score=0.6

# Pool size limits
pooling.pool.max-size=6
pooling.pool.min-size=2
```

### Service URLs
```properties
services.trip-planning.url=http://localhost:8082
services.user-services.url=http://localhost:8081
```

## 📊 Example Usage Flow

1. **User has planned trip** in trip-planning-service
2. **Calls find-matches** with trip preferences
3. **Algorithm analyzes** all compatible trips
4. **Returns ranked suggestions** with explanations
5. **User creates pool** or joins existing one
6. **Pool management** handles member lifecycle

## 🔄 Future Enhancements (Real-time Pooling)

The service is designed for future real-time activity pooling:
- Location-based matching during trips
- Same-day activity suggestions
- Dynamic pool formation
- Push notifications for opportunities

## 🧪 Testing

Run the service with test data:
```bash
mvn test
```

Health check:
```bash
curl http://localhost:8086/pooling/health
```

## 📈 Monitoring

Service provides metrics on:
- Pool creation rates
- Matching success rates
- Average compatibility scores
- User engagement statistics