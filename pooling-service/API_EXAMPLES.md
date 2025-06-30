# Pooling Service API Test Examples

## 1. Find Pool Matches

```bash
curl -X POST http://localhost:8086/pooling/find-matches \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test@example.com",
    "tripId": "trip123",
    "startDate": "2025-07-15",
    "endDate": "2025-07-22",
    "baseCity": "Colombo",
    "preferredCities": ["Kandy", "Galle", "Nuwara Eliya"],
    "interests": ["Adventure", "Culture", "Nature"],
    "activityPacing": "NORMAL",
    "maxPoolSize": 4,
    "willingToShareTransport": true,
    "willingToShareAccommodation": false,
    "dateFlexibilityDays": 2,
    "maxDistanceKm": 50.0,
    "minCompatibilityScore": 0.6
  }'
```

## 2. Create Pool

```bash
curl -X POST http://localhost:8086/pooling/create-pool \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test@example.com",
    "tripId": "trip123",
    "poolName": "Sri Lanka Adventure Group",
    "description": "Looking for travel buddies for cultural exploration and hiking"
  }'
```

## 3. Join Pool

```bash
curl -X POST http://localhost:8086/pooling/join-pool/POOL_ID_HERE \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user2@example.com",
    "tripId": "trip456"
  }'
```

## 4. Get User Pools

```bash
curl -X GET http://localhost:8086/pooling/my-pools/test@example.com
```

## 5. Leave Pool

```bash
curl -X POST http://localhost:8086/pooling/leave-pool/POOL_ID_HERE \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test@example.com"
  }'
```

## 6. Health Check

```bash
curl -X GET http://localhost:8086/pooling/health
```

## 7. Service Status

```bash
curl -X GET http://localhost:8086/pooling/status
```

## Expected Response Examples

### Find Matches Response:
```json
[
  {
    "suggestedPool": {
      "poolName": "Trip with John",
      "poolType": "TIMELINE_BASED",
      "status": "FORMING",
      "startDate": "2025-07-16",
      "endDate": "2025-07-21",
      "baseCity": "Colombo"
    },
    "compatibilityScore": 0.85,
    "matchReason": "Timeline-based compatibility",
    "commonInterests": ["Excellent timeline overlap (85%)", "Similar travel interests"],
    "overlapDays": 6,
    "currentMembers": 1,
    "poolCreatedBy": "John"
  }
]
```

### Create Pool Response:
```json
{
  "poolId": "generated-pool-id",
  "poolName": "Sri Lanka Adventure Group",
  "poolType": "TIMELINE_BASED",
  "status": "FORMING",
  "members": [
    {
      "userId": "test@example.com",
      "role": "CREATOR",
      "status": "ACTIVE",
      "compatibilityScore": 1.0
    }
  ],
  "joinCode": "POOL12345"
}
```
