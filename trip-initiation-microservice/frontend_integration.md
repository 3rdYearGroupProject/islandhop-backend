# Trip Initiation Microservice - Frontend Integration Guide

## Overview

This microservice handles trip initiation by retrieving trip plans from MongoDB, calculating routes using Google Maps API, computing driver and guide costs, and saving initiated trips with additional cost information.

## Base URL

```
http://localhost:8095/api/trips
```

## Endpoints

### 1. Initiate Trip

**POST** `/initiate`

#### Description

Initiates a trip by:

- Retrieving trip plan from MongoDB trip_plans collection
- Calculating total route distance using Google Maps API
- Computing driver costs (distance + daily fees)
- Computing guide costs based on cities
- Saving initiated trip to MongoDB initiated_trips collection

#### Request Headers

```javascript
{
  "Content-Type": "application/json"
}
```

#### Request Body

```javascript
{
  "userId": "string",                    // Required: User ID (NotBlank)
  "tripId": "string",                    // Required: Trip ID from trip_plans collection (NotBlank)
  "setDriver": 1,                        // Required: 1 if driver needed, 0 otherwise (0-1)
  "setGuide": 1,                         // Required: 1 if guide needed, 0 otherwise (0-1)
  "preferredVehicleTypeId": "string"     // Required: Vehicle type ID from PostgreSQL (NotBlank, numeric)
}
```

#### JavaScript Integration Example

```javascript
/**
 * Trip initiation service for handling trip initiation requests
 */
class TripInitiationService {
  constructor(baseUrl = "http://localhost:8095/api/trips") {
    this.baseUrl = baseUrl;
  }

  /**
   * Initiates a trip with driver and guide preferences
   * @param {Object} tripData - Trip initiation data
   * @returns {Promise<Object>} Trip initiation response
   */
  async initiateTrip(tripData) {
    try {
      const response = await fetch(`${this.baseUrl}/initiate`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(tripData),
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || "Failed to initiate trip");
      }

      return result;
    } catch (error) {
      console.error("Error initiating trip:", error);
      throw error;
    }
  }
}

// Usage example
const tripService = new TripInitiationService();

const handleTripInitiation = async (formData) => {
  try {
    // Validate form data
    const validation = validateTripInitiationForm(formData);
    if (!validation.isValid) {
      showValidationErrors(validation.errors);
      return;
    }

    // Prepare request data
    const tripData = {
      userId: formData.userId,
      tripId: formData.tripId,
      setDriver: parseInt(formData.setDriver),
      setGuide: parseInt(formData.setGuide),
      preferredVehicleTypeId: formData.preferredVehicleTypeId,
    };

    const result = await tripService.initiateTrip(tripData);

    console.log("Trip initiated successfully:", result);
    displayTripInitiationResult(result);
  } catch (error) {
    console.error("Trip initiation failed:", error);
    showErrorMessage(error.message);
  }
};
```

#### Form Validation

```javascript
/**
 * Validates trip initiation form data
 * @param {Object} formData - Form data to validate
 * @returns {Object} Validation result with isValid flag and errors
 */
const validateTripInitiationForm = (formData) => {
  const errors = {};

  // Validate userId
  if (!formData.userId || formData.userId.trim() === "") {
    errors.userId = "User ID is required";
  }

  // Validate tripId
  if (!formData.tripId || formData.tripId.trim() === "") {
    errors.tripId = "Trip ID is required";
  }

  // Validate setDriver
  if (formData.setDriver === null || formData.setDriver === undefined) {
    errors.setDriver = "Driver selection is required";
  } else {
    const driverValue = parseInt(formData.setDriver);
    if (![0, 1].includes(driverValue)) {
      errors.setDriver = "Driver selection must be 0 or 1";
    }
  }

  // Validate setGuide
  if (formData.setGuide === null || formData.setGuide === undefined) {
    errors.setGuide = "Guide selection is required";
  } else {
    const guideValue = parseInt(formData.setGuide);
    if (![0, 1].includes(guideValue)) {
      errors.setGuide = "Guide selection must be 0 or 1";
    }
  }

  // Validate preferredVehicleTypeId
  if (
    !formData.preferredVehicleTypeId ||
    formData.preferredVehicleTypeId.trim() === ""
  ) {
    errors.preferredVehicleTypeId = "Vehicle type selection is required";
  } else if (!/^\d+$/.test(formData.preferredVehicleTypeId)) {
    errors.preferredVehicleTypeId = "Vehicle type ID must be a valid number";
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
};
```

#### Success Response (201 Created)

```javascript
{
  "tripId": "60f59cfe-9495-4f80-97fc-b56afec831c0",
  "userId": "p12lBeHypEcQRQ6qd3eCFtnIeh93",
  "averageTripDistance": 25.5,          // Total distance in kilometers
  "averageDriverCost": 150.0,           // Total driver cost (distance + daily fees)
  "averageGuideCost": 100.0,            // Total guide cost (0 if setGuide = 0)
  "vehicleType": "Car",                 // Vehicle type name
  "routeSummary": [
    {
      "day": 1,
      "city": "Colombo",
      "attractions": [
        {
          "name": "Sambodhi Pagoda Temple",
          "location": {
            "lat": 6.9383932,
            "lng": 79.8420285
          }
        }
      ]
    }
  ]
}
```

#### Error Responses

**400 Bad Request - Validation Error**

```javascript
{
  "error": "Validation failed",
  "details": {
    "userId": "User ID is required",
    "preferredVehicleTypeId": "Vehicle type ID must be a valid number"
  }
}
```

**404 Not Found - Trip Not Found**

```javascript
{
  "error": "Trip not found",
  "message": "Trip not found with ID: 60f59cfe-9495-4f80-97fc-b56afec831c0"
}
```

**404 Not Found - Vehicle Type Not Found**

```javascript
{
  "error": "Vehicle type not found",
  "message": "Vehicle type not found with ID: 999"
}
```

**500 Internal Server Error**

```javascript
{
  "error": "Internal server error",
  "message": "An unexpected error occurred"
}
```

#### HTML Form Example

```html
<form id="tripInitiationForm">
  <div class="form-group">
    <label for="userId">User ID:</label>
    <input type="text" id="userId" name="userId" required />
  </div>

  <div class="form-group">
    <label for="tripId">Trip ID:</label>
    <input type="text" id="tripId" name="tripId" required />
  </div>

  <div class="form-group">
    <label for="setDriver">Need Driver:</label>
    <select id="setDriver" name="setDriver" required>
      <option value="">Select...</option>
      <option value="1">Yes</option>
      <option value="0">No</option>
    </select>
  </div>

  <div class="form-group">
    <label for="setGuide">Need Guide:</label>
    <select id="setGuide" name="setGuide" required>
      <option value="">Select...</option>
      <option value="1">Yes</option>
      <option value="0">No</option>
    </select>
  </div>

  <div class="form-group">
    <label for="preferredVehicleTypeId">Vehicle Type:</label>
    <select id="preferredVehicleTypeId" name="preferredVehicleTypeId" required>
      <option value="">Select...</option>
      <option value="1">Car</option>
      <option value="2">Van</option>
      <option value="3">Bus</option>
    </select>
  </div>

  <button type="submit">Initiate Trip</button>
</form>

<div id="errorMessage" style="display: none;"></div>
<div id="successMessage" style="display: none;"></div>

<div id="tripResult" style="display: none;">
  <h3>Trip Initiated Successfully</h3>
  <p><strong>Trip ID:</strong> <span id="tripId"></span></p>
  <p><strong>Total Distance:</strong> <span id="distance"></span></p>
  <p><strong>Driver Cost:</strong> <span id="driverCost"></span></p>
  <p><strong>Guide Cost:</strong> <span id="guideCost"></span></p>
  <p><strong>Total Cost:</strong> <span id="totalCost"></span></p>
  <p><strong>Vehicle Type:</strong> <span id="vehicleType"></span></p>

  <div id="routeSummary"></div>
</div>
```

#### Complete Integration Script

```javascript
// Initialize service
const tripService = new TripInitiationService();

// Form submission handler
document
  .getElementById("tripInitiationForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = new FormData(e.target);
    const tripData = {
      userId: formData.get("userId"),
      tripId: formData.get("tripId"),
      setDriver: formData.get("setDriver"),
      setGuide: formData.get("setGuide"),
      preferredVehicleTypeId: formData.get("preferredVehicleTypeId"),
    };

    await handleTripInitiation(tripData);
  });

/**
 * Displays trip initiation result on the UI
 * @param {Object} result - Trip initiation response
 */
const displayTripInitiationResult = (result) => {
  // Update basic trip information
  document.getElementById("tripId").textContent = result.tripId;
  document.getElementById(
    "distance"
  ).textContent = `${result.averageTripDistance} km`;
  document.getElementById(
    "driverCost"
  ).textContent = `$${result.averageDriverCost.toFixed(2)}`;
  document.getElementById(
    "guideCost"
  ).textContent = `$${result.averageGuideCost.toFixed(2)}`;
  document.getElementById("vehicleType").textContent = result.vehicleType;

  // Calculate and display total cost
  const totalCost = result.averageDriverCost + result.averageGuideCost;
  document.getElementById("totalCost").textContent = `$${totalCost.toFixed(2)}`;

  // Display route summary
  const routeContainer = document.getElementById("routeSummary");
  routeContainer.innerHTML = "";

  result.routeSummary.forEach((day) => {
    const dayElement = document.createElement("div");
    dayElement.className = "day-summary";
    dayElement.innerHTML = `
      <h3>Day ${day.day}${day.city ? ` - ${day.city}` : ""}</h3>
      ${
        day.attractions.length > 0
          ? `
        <h4>Attractions:</h4>
        <ul>
          ${day.attractions
            .map(
              (attraction) =>
                `<li>
              <strong>${attraction.name}</strong><br>
              <small>Location: ${attraction.location.lat}, ${attraction.location.lng}</small>
            </li>`
            )
            .join("")}
        </ul>
      `
          : "<p>No attractions planned for this day</p>"
      }
    `;
    routeContainer.appendChild(dayElement);
  });

  // Show success message
  document.getElementById("tripResult").style.display = "block";
  showSuccessMessage("Trip initiated successfully!");
};

/**
 * Shows validation errors on the form
 * @param {Object} errors - Validation errors
 */
const showValidationErrors = (errors) => {
  // Clear previous errors
  document.querySelectorAll(".error-message").forEach((el) => el.remove());

  // Display new errors
  Object.keys(errors).forEach((field) => {
    const fieldElement = document.getElementById(field);
    if (fieldElement) {
      const errorElement = document.createElement("div");
      errorElement.className = "error-message";
      errorElement.textContent = errors[field];
      errorElement.style.color = "red";
      errorElement.style.fontSize = "0.9em";
      fieldElement.parentNode.appendChild(errorElement);
    }
  });
};

/**
 * Shows error message
 * @param {string} message - Error message
 */
const showErrorMessage = (message) => {
  const errorDiv = document.getElementById("errorMessage");
  errorDiv.textContent = message;
  errorDiv.style.display = "block";
  errorDiv.style.color = "red";
  errorDiv.style.padding = "10px";
  errorDiv.style.border = "1px solid red";
  errorDiv.style.borderRadius = "5px";
  errorDiv.style.marginTop = "10px";
};

/**
 * Shows success message
 * @param {string} message - Success message
 */
const showSuccessMessage = (message) => {
  const successDiv = document.getElementById("successMessage");
  successDiv.textContent = message;
  successDiv.style.display = "block";
  successDiv.style.color = "green";
  successDiv.style.padding = "10px";
  successDiv.style.border = "1px solid green";
  successDiv.style.borderRadius = "5px";
  successDiv.style.marginTop = "10px";
};
```

## Notes

- Uses Jakarta persistence annotations for Spring Boot 3.x compatibility
- Integrates with MongoDB for trip plans and initiated trips
- PostgreSQL integration for vehicle types and guide fees
- Google Maps API for route calculation and distance computation
- Proper error handling with meaningful HTTP status codes
- SLF4J logging for debugging and monitoring
- Input validation using Jakarta validation annotations
- Frontend integration with complete JavaScript examples
