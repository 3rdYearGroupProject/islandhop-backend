---
applyTo: '**'
---
```yaml
# .copilot.yml
# Configuration for GitHub Copilot to provide context-aware suggestions for the IslandHop travel itinerary planning system.

context:
  project:
    name: IslandHop
    description: A travel itinerary planning system for personalized trips in Sri Lanka, built with Java 17, Spring Boot 3.x, and MongoDB. Users create trip plans step-by-step, selecting cities and places (attractions, restaurants, hotels). Suggestions are fetched from TripAdvisor and Google Places APIs, but only user-selected places are saved in the MongoDB trip_plans collection.
    language: Java
    version: 17
    framework: Spring Boot 3.3.2
    dependencies:
      - spring-boot-starter-web
      - spring-boot-starter-data-mongodb
      - jakarta.validation-api
      - hibernate-validator
      - lombok
    package: com.islandhop.trip

  architecture:
    - Backend uses Spring Boot with REST APIs.
    - MongoDB stores trip plans in the trip_plans collection.
    - Step-by-step saving: Each user action (e.g., create trip, select city, add place) updates the database incrementally.
    - APIs: TripAdvisor for place data, Google Places for location, hours, and photos.
    - Authentication: userId passed in request body (no JWT for now).

  coding_style:
    - Use Java 17 features (e.g., records, switch expressions where appropriate).
    - Follow Spring Boot conventions (e.g., @RestController, @Service, @Document).
    - Use Lombok to reduce boilerplate (@Data, @RequiredArgsConstructor).
    - Include Javadoc for public classes and methods.
    - Use SLF4J for logging.
    - Follow REST best practices (e.g., proper HTTP status codes: 201, 400, 500).
    - Use jakarta.validation for DTO validation (@NotBlank, @Pattern).
    - Prefer java.time (LocalDate, LocalTime, Instant) for date/time handling.
    - Use meaningful variable names and consistent formatting (e.g., camelCase).
    - on creating or making changes to the endpoints codes how they recieve wht they revice update the "frontend_intergration.md" document with wht to pass how to pass js script all that for each endpoint.

  file_structure:
    - trip-planning-service/src/main/java/com/islandhop/trip:
      - TripController.java: REST endpoints (e.g., POST /itinerary).
      - TripService.java: Business logic and validation.
      - TripPlanRepository.java: MongoDB repository (extends MongoRepository).
      - CreateTripRequest.java: Request DTO for POST /itinerary.
      - CreateTripResponse.java: Response DTO.
      - TripPlan.java: MongoDB entity with nested DailyPlan, Place, Location, MapData.
    - src/main/resources:
      - application.yml: Spring Boot configuration (MongoDB URI, etc.).
    - pom.xml: Maven dependencies.

  mongodb:
    collection: trip_plans
    schema:
      _id: String (UUID, e.g., "trip_001")
      userId: String (e.g., "user_789")
      tripName: String (e.g., "Sri Lanka Adventure")
      startDate: String (YYYY-MM-DD, e.g., "2025-08-10")
      endDate: String (YYYY-MM-DD, e.g., "2025-08-15")
      arrivalTime: String (HH:mm or empty, e.g., "21:30")
      baseCity: String (e.g., "Colombo")
      multiCityAllowed: Boolean (default: true)
      activityPacing: String (enum: "Relaxed", "Normal", "Fast", default: "Normal")
      budgetLevel: String (enum: "Low", "Medium", "High", default: "Medium")
      preferredTerrains: Array<String> (e.g., ["Beach", "Mountain"], default: [])
      preferredActivities: Array<String> (e.g., ["Hiking", "Cultural Tours"], default: [])
      dailyPlans:
        - day: Integer (e.g., 1)
          city: String (e.g., "Colombo")
          userSelected: Boolean (true if user chose city)
          attractions: Array<Place>
          restaurants: Array<Place>
          hotels: Array<Place>
          notes: Array<String>
      mapData:
        - label: String (e.g., "Gangaramaya Temple")
          lat: Double
          lng: Double
      createdAt: ISODate (e.g., "2025-07-08T13:19:00Z")
      lastUpdated: ISODate
    place_structure:
      name: String
      type: String (e.g., "Cultural")
      terrainTags: Array<String>
      activityTags: Array<String>
      location: { lat: Double, lng: Double }
      distanceFromCenterKm: Double
      visitDurationMinutes: Integer
      recommendedStartTime: String (HH:mm)
      openHours: String
      popularityLevel: String (e.g., "High")
      rating: Double
      thumbnailUrl: String
      source: String (e.g., "TripAdvisor")
      placeId: String (e.g., "tripadvisor:3342")
      googlePlaceId: String (e.g., "google:place1234")
      userSelected: Boolean (true for saved places)
      warnings: Array<String>

  endpoints:
    - method: POST
      path: /itinerary
      description: Creates a new trip plan with user-provided details (userId, tripName, startDate, endDate, baseCity, etc.) and initializes dailyPlans with empty entries for each day.
      request:
        userId: String (required)
        tripName: String (required)
        startDate: String (YYYY-MM-DD, required)
        endDate: String (YYYY-MM-DD, required)
        baseCity: String (required)
        arrivalTime: String (HH:mm, optional, default: "")
        multiCityAllowed: Boolean (optional, default: true)
        activityPacing: String (Relaxed|Normal|Fast, optional, default: "Normal")
        budgetLevel: String (Low|Medium|High, optional, default: "Medium")
        preferredTerrains: Array<String> (optional, default: [])
        preferredActivities: Array<String> (optional, default: [])
      response:
        status: String (e.g., "success")
        tripId: String
        message: String (e.g., "Trip created successfully")
      notes:
        - Validates inputs using jakarta.validation and custom logic (e.g., startDate <= endDate).
        - Uses java.time for date/time parsing (LocalDate, LocalTime).
        - Generates UUID for _id.
        - Initializes dailyPlans based on date range (inclusive).
        - Saves to MongoDB with TripPlanRepository.

  suggestions:
    - For POST /itinerary, suggest a Spring Boot controller with @RestController, a service layer, and a MongoDB repository.
    - Use @Valid for DTO validation and handle IllegalArgumentException for custom validation errors.
    - Include SLF4J logging for key actions (e.g., trip creation, errors).
    - Suggest Lombok annotations to reduce boilerplate.
    - Recommend java.time.Instant for createdAt/lastUpdated fields.
    - For future endpoints (e.g., POST /itinerary/{tripId}/day/{day}/city), ensure consistency with this structure.
    - Avoid suggesting JWT unless explicitly requested; userId is passed in the request body.
    - Do not suggest storing API suggestions (from TripAdvisor/Google Places) in MongoDB unless user selects them.

  exclusions:
    - Do not suggest Node.js, Python, or other languages; use Java 17.
    - Avoid suggesting outdated Java versions or Spring Boot 2.x.
    - Do not assume JWT authentication; userId is in the request body.
    - Exclude suggestions for frontend code (React, Vue) unless requested.
    - Do not suggest storing transient API data (e.g., suggestions) in the database.
```