# Trip Planning Service - Setup and Testing Guide

## Overview
This guide provides complete instructions for setting up, running, and testing the Trip Planning Service with dynamic search functionality and full trip planning workflow.

## Prerequisites
- Java 17 or later
- Maven 3.6 or later
- MongoDB (local or Atlas)
- PowerShell (for Windows testing scripts)

## Service Setup

### 1. Build the Application
```bash
mvn clean compile
```

### 2. Run Tests
```bash
mvn test
```

### 3. Package the Application
```bash
mvn package
```

## Running the Service

### Development Mode (with Test Endpoints)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Production Mode
```bash
mvn spring-boot:run
```

### Alternative: Run JAR directly
```bash
java -jar target/trip-planning-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## Service Configuration

### Key Configuration Files
- `src/main/resources/application.properties` - Main configuration
- `src/main/resources/application-dev.properties` - Development overrides

### Important Configuration Properties
```properties
# Server Configuration
server.port=8083
server.servlet.context-path=/api

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/islandhop_trip_planning

# Profile Configuration
spring.profiles.active=dev

# CORS Configuration
cors.allowed.origins=http://localhost:5173

# Health Check Configuration
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always

# Bean Override (for WebFlux compatibility)
spring.main.allow-bean-definition-overriding=true
spring.main.web-application-type=servlet
```

## Testing the Service

### Health Check
```bash
curl http://localhost:8083/api/trip/health
# Expected: "Trip Planning Service is running"
```

### Actuator Health
```bash
curl http://localhost:8083/api/actuator/health
# Expected: JSON with status "UP"
```

## PowerShell Test Scripts

### 1. Basic Mock Session Test
```powershell
.\test_with_mock_session.ps1
```
**Tests:**
- Health checks
- Session information
- Location search functionality
- Place categories
- Place validation
- Trip creation with correct structure

### 2. Comprehensive Trip Flow Test
```powershell
.\test_simple_trip_flow.ps1
```
**Tests:**
- Complete trip planning workflow
- Trip creation (2 different trips)
- Adding places to trips
- Retrieving day plans
- Optimizing trip order
- Getting trip summaries
- Location search variations
- Place validation

## API Endpoints

### Production Endpoints (Session Required)
- `GET /trip/health` - Service health check
- `POST /trip/create` - Create new trip
- `POST /trip/{tripId}/add-place` - Add place to trip
- `GET /trip/{tripId}/day-plans` - Get day plans
- `POST /trip/{tripId}/optimize-order` - Optimize visit order
- `GET /trip/{tripId}/summary` - Get trip summary
- `GET /trip/search-locations` - Search for locations

### Test Endpoints (Dev Profile Only, No Session Required)
- `GET /test/session-info` - Mock session information
- `POST /test/create-trip` - Create trip without session
- `POST /test/add-place` - Add place without session
- `GET /test/day-plans/{tripId}` - Get day plans without session
- `POST /test/optimize-order/{tripId}` - Optimize order without session
- `GET /test/trip-summary/{tripId}` - Get summary without session
- `GET /test/search-locations` - Search locations without session
- `POST /test/validate-place` - Validate place without session
- `GET /test/place-categories` - Get place categories without session

## Sample API Requests

### Create Trip Request
```json
{
  "tripName": "Cultural Sri Lanka Adventure",
  "startDate": "2025-07-15",
  "endDate": "2025-07-20",
  "baseCity": "Kandy",
  "categories": ["Culture", "Nature"],
  "pacing": "NORMAL"
}
```

### Add Place to Trip Request
```json
{
  "tripId": "your-trip-id",
  "placeName": "Temple of the Sacred Tooth Relic",
  "city": "Kandy",
  "dayNumber": 1,
  "placeType": "ATTRACTION",
  "estimatedVisitDurationMinutes": 120,
  "description": "Famous Buddhist temple",
  "priority": 8
}
```

### Location Search
```
GET /test/search-locations?query=Kandy&maxResults=5
GET /test/search-locations?query=temple&maxResults=3&city=Kandy
GET /test/search-locations?query=beach&biasLat=6.9271&biasLng=79.8612
```

## Expected Test Results

### Successful Trip Creation Output
```
Trip 1 ID: de2e438d-dca6-40ea-817d-2236af3e400a
Trip 2 ID: 0e549a83-f80c-47c0-bf47-486ff61563a5
```

### Complete Test Summary
```
=== FINAL TEST SUMMARY ===
Successful requests: 24
Failed requests: 0
Total requests: 24
Created trips count: 2
```

## Troubleshooting

### Common Issues

#### 1. Port Already in Use
```bash
# Find process using port 8083
netstat -ano | findstr :8083
# Kill the process
taskkill /PID <process-id> /F
```

#### 2. MongoDB Connection Issues
- Ensure MongoDB is running on localhost:27017
- Check connection string in application.properties
- Verify database permissions

#### 3. Bean Definition Override Warnings
This is normal and expected due to WebFlux/WebMVC compatibility setup.

#### 4. 404 Errors on Test Endpoints
- Ensure the application is running with `dev` profile
- Verify TestController is loaded (check logs for "dev" profile activation)

### Log Analysis
```bash
# Check application logs for errors
tail -f logs/application.log

# Look for profile activation
grep "dev" logs/application.log
```

## Development Features

### Dynamic Search
- Real-time location search with autocomplete support
- City-based filtering
- GPS bias for location-aware results
- Multiple data source integration (Google Places, TripAdvisor)

### Trip Planning Flow
1. **Create Trip** - With dates, base city, categories, and pacing
2. **Add Places** - With specific day assignment and timing
3. **Get Day Plans** - Organized itinerary by day
4. **Optimize Order** - Efficient visiting sequence
5. **Trip Summary** - Complete overview with statistics

### Session Management
- Production: Full session validation
- Development: Mock session for testing (user ID: "test-user-123")

## Integration Notes

### Frontend Integration
The service is configured to work with a frontend running on `http://localhost:5173` with CORS enabled.

### Database Schema
- Trip documents with embedded day plans and places
- Automatic auditing with creation/update timestamps
- MongoDB optimized queries for trip retrieval

## Performance Considerations
- Location search results are limited to prevent excessive API calls
- Caching enabled for frequently accessed data
- Optimized MongoDB queries with proper indexing

---

**Last Updated:** July 2, 2025  
**Service Version:** 0.0.1-SNAPSHOT  
**Spring Boot Version:** 3.x  
**Java Version:** 17+
