# Pooling Service API Documentation

## Endpoint: `/api/v1/groups/public/enhanced`

### Description
Retrieves a list of enhanced public groups with detailed trip and creator information. Provides comprehensive details including creator names, cities, dates, and top attractions.

### HTTP Method
`GET`

### Query Parameters
| Parameter            | Type       | Required | Description                                                                 |
|----------------------|------------|----------|-----------------------------------------------------------------------------|
| `userId`             | `String`   | Yes      | The ID of the requesting user.                                             |
| `baseCity`           | `String`   | No       | Filter by base city.                                                       |
| `startDate`          | `String`   | No       | Filter by start date (format: `YYYY-MM-DD`).                                |
| `endDate`            | `String`   | No       | Filter by end date (format: `YYYY-MM-DD`).                                  |
| `budgetLevel`        | `String`   | No       | Filter by budget level (e.g., `Low`, `Medium`, `High`).                     |
| `preferredActivities`| `List<String>` | No   | A list of preferred activities (e.g., `nightlife`, `dining`, `adventure`). |

### Example Request
```http
GET /api/v1/groups/public/enhanced?userId=wBuieMHjt1RKKgRoDgI9v6VyNHF3&budgetLevel=Medium&preferredActivities[]=nightlife&preferredActivities[]=dining&preferredActivities[]=adventure HTTP/1.1
Host: localhost:8086
```

### Example Response
```json
[
  {
    "groupId": "group_001",
    "tripName": "Adventure to Ella",
    "creatorName": "John Doe",
    "cities": ["Kandy", "Nuwara Eliya", "Ella"],
    "dateRange": "2025-08-15 to 2025-08-17",
    "memberCount": "3/5",
    "topAttractions": ["Tea Plantations", "Nine Arch Bridge"]
  },
  {
    "groupId": "group_002",
    "tripName": "Cultural Tour",
    "creatorName": "Jane Smith",
    "cities": ["Anuradhapura", "Polonnaruwa"],
    "dateRange": "2025-08-10 to 2025-08-12",
    "memberCount": "4/6",
    "topAttractions": ["Sacred City", "Ancient Ruins"]
  }
]
```

### Notes
- The `userId` parameter is mandatory.
- If no filters are provided, all public groups are returned.
- Compatibility scores are calculated if user preferences are provided.
