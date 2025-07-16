# Frontend Integration Guide - Vehicle Types Admin API

This guide provides JavaScript code examples for integrating with the Vehicle Types Admin API endpoints.

## Base Configuration

```javascript
const API_BASE_URL = "http://localhost:8091/api/v1";
const VEHICLE_TYPES_ENDPOINT = `${API_BASE_URL}/admin/vehicle-types`;

// Default headers for all requests
const defaultHeaders = {
  "Content-Type": "application/json",
  Accept: "application/json",
};
```

## API Integration Functions

### 1. Create Vehicle Type

**Endpoint:** `POST /admin/vehicle-types`

```javascript
async function createVehicleType(vehicleTypeData) {
  try {
    const response = await fetch(VEHICLE_TYPES_ENDPOINT, {
      method: "POST",
      headers: defaultHeaders,
      body: JSON.stringify(vehicleTypeData),
    });

    const result = await response.json();

    if (response.ok) {
      console.log("Vehicle type created successfully:", result.data);
      return { success: true, data: result.data };
    } else {
      console.error("Failed to create vehicle type:", result.message);
      return {
        success: false,
        error: result.message,
        validationErrors: result.data,
      };
    }
  } catch (error) {
    console.error("Network error:", error);
    return { success: false, error: "Network error occurred" };
  }
}

// Usage example
const newVehicleType = {
  capacity: 4,
  description: "Compact sedan for city travel",
  fuelType: "Petrol",
  isAvailable: true,
  pricePerKm: 50.0,
  typeName: "Economy Car",
};

createVehicleType(newVehicleType).then((result) => {
  if (result.success) {
    // Handle success - update UI, show success message
    console.log("Created vehicle type with ID:", result.data.id);
  } else {
    // Handle error - show error message, highlight invalid fields
    if (result.validationErrors) {
      // Handle validation errors
      Object.keys(result.validationErrors).forEach((field) => {
        console.log(
          `Validation error for ${field}: ${result.validationErrors[field]}`
        );
      });
    }
  }
});
```

### 2. Get All Vehicle Types

**Endpoint:** `GET /admin/vehicle-types`

```javascript
async function getAllVehicleTypes() {
  try {
    const response = await fetch(VEHICLE_TYPES_ENDPOINT, {
      method: "GET",
      headers: defaultHeaders,
    });

    const result = await response.json();

    if (response.ok) {
      console.log("Retrieved vehicle types:", result.data);
      return { success: true, data: result.data };
    } else {
      console.error("Failed to retrieve vehicle types:", result.message);
      return { success: false, error: result.message };
    }
  } catch (error) {
    console.error("Network error:", error);
    return { success: false, error: "Network error occurred" };
  }
}

// Usage example
getAllVehicleTypes().then((result) => {
  if (result.success) {
    // Populate table or list with vehicle types
    const vehicleTypes = result.data;
    displayVehicleTypesTable(vehicleTypes);
  } else {
    // Show error message
    showErrorMessage(result.error);
  }
});

function displayVehicleTypesTable(vehicleTypes) {
  const tableBody = document.getElementById("vehicleTypesTableBody");
  tableBody.innerHTML = "";

  vehicleTypes.forEach((vehicleType) => {
    const row = document.createElement("tr");
    row.innerHTML = `
            <td>${vehicleType.id}</td>
            <td>${vehicleType.typeName}</td>
            <td>${vehicleType.capacity || "N/A"}</td>
            <td>${vehicleType.fuelType || "N/A"}</td>
            <td>${vehicleType.pricePerKm}</td>
            <td>${vehicleType.isAvailable ? "Yes" : "No"}</td>
            <td>
                <button onclick="editVehicleType(${
                  vehicleType.id
                })">Edit</button>
                <button onclick="deleteVehicleType(${
                  vehicleType.id
                })">Delete</button>
            </td>
        `;
    tableBody.appendChild(row);
  });
}
```

### 3. Get Vehicle Type by ID

**Endpoint:** `GET /admin/vehicle-types/{id}`

```javascript
async function getVehicleTypeById(id) {
  try {
    const response = await fetch(`${VEHICLE_TYPES_ENDPOINT}/${id}`, {
      method: "GET",
      headers: defaultHeaders,
    });

    const result = await response.json();

    if (response.ok) {
      console.log("Retrieved vehicle type:", result.data);
      return { success: true, data: result.data };
    } else {
      console.error("Vehicle type not found:", result.message);
      return { success: false, error: result.message };
    }
  } catch (error) {
    console.error("Network error:", error);
    return { success: false, error: "Network error occurred" };
  }
}

// Usage example
async function editVehicleType(id) {
  const result = await getVehicleTypeById(id);
  if (result.success) {
    // Populate edit form with vehicle type data
    populateEditForm(result.data);
    showEditModal();
  } else {
    showErrorMessage(`Vehicle type not found: ${result.error}`);
  }
}

function populateEditForm(vehicleType) {
  document.getElementById("editId").value = vehicleType.id;
  document.getElementById("editTypeName").value = vehicleType.typeName;
  document.getElementById("editCapacity").value = vehicleType.capacity || "";
  document.getElementById("editDescription").value =
    vehicleType.description || "";
  document.getElementById("editFuelType").value = vehicleType.fuelType || "";
  document.getElementById("editPricePerKm").value = vehicleType.pricePerKm;
  document.getElementById("editIsAvailable").checked = vehicleType.isAvailable;
}
```

### 4. Update Vehicle Type

**Endpoint:** `PUT /admin/vehicle-types/{id}`

```javascript
async function updateVehicleType(id, vehicleTypeData) {
  try {
    const response = await fetch(`${VEHICLE_TYPES_ENDPOINT}/${id}`, {
      method: "PUT",
      headers: defaultHeaders,
      body: JSON.stringify(vehicleTypeData),
    });

    const result = await response.json();

    if (response.ok) {
      console.log("Vehicle type updated successfully:", result.data);
      return { success: true, data: result.data };
    } else {
      console.error("Failed to update vehicle type:", result.message);
      return {
        success: false,
        error: result.message,
        validationErrors: result.data,
      };
    }
  } catch (error) {
    console.error("Network error:", error);
    return { success: false, error: "Network error occurred" };
  }
}

// Usage example - form submission handler
document
  .getElementById("editVehicleTypeForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = new FormData(e.target);
    const id = formData.get("id");

    const updatedVehicleType = {
      typeName: formData.get("typeName"),
      capacity: formData.get("capacity")
        ? parseInt(formData.get("capacity"))
        : null,
      description: formData.get("description") || null,
      fuelType: formData.get("fuelType") || null,
      pricePerKm: parseFloat(formData.get("pricePerKm")),
      isAvailable: formData.get("isAvailable") === "on",
    };

    const result = await updateVehicleType(id, updatedVehicleType);

    if (result.success) {
      showSuccessMessage("Vehicle type updated successfully!");
      hideEditModal();
      getAllVehicleTypes(); // Refresh the list
    } else {
      if (result.validationErrors) {
        displayValidationErrors(result.validationErrors);
      } else {
        showErrorMessage(result.error);
      }
    }
  });
```

### 5. Delete Vehicle Type

**Endpoint:** `DELETE /admin/vehicle-types/{id}`

```javascript
async function deleteVehicleType(id) {
  // Show confirmation dialog
  if (!confirm("Are you sure you want to delete this vehicle type?")) {
    return;
  }

  try {
    const response = await fetch(`${VEHICLE_TYPES_ENDPOINT}/${id}`, {
      method: "DELETE",
      headers: defaultHeaders,
    });

    const result = await response.json();

    if (response.ok) {
      console.log("Vehicle type deleted successfully");
      return { success: true };
    } else {
      console.error("Failed to delete vehicle type:", result.message);
      return { success: false, error: result.message };
    }
  } catch (error) {
    console.error("Network error:", error);
    return { success: false, error: "Network error occurred" };
  }
}

// Usage example
async function handleDeleteVehicleType(id) {
  const result = await deleteVehicleType(id);

  if (result.success) {
    showSuccessMessage("Vehicle type deleted successfully!");
    getAllVehicleTypes(); // Refresh the list
  } else {
    showErrorMessage(`Failed to delete vehicle type: ${result.error}`);
  }
}
```

### 6. Get Available Vehicle Types

**Endpoint:** `GET /admin/vehicle-types/available`

```javascript
async function getAvailableVehicleTypes() {
  try {
    const response = await fetch(`${VEHICLE_TYPES_ENDPOINT}/available`, {
      method: "GET",
      headers: defaultHeaders,
    });

    const result = await response.json();

    if (response.ok) {
      console.log("Retrieved available vehicle types:", result.data);
      return { success: true, data: result.data };
    } else {
      console.error(
        "Failed to retrieve available vehicle types:",
        result.message
      );
      return { success: false, error: result.message };
    }
  } catch (error) {
    console.error("Network error:", error);
    return { success: false, error: "Network error occurred" };
  }
}

// Usage example - populate a dropdown for booking
getAvailableVehicleTypes().then((result) => {
  if (result.success) {
    populateVehicleTypeDropdown(result.data);
  }
});

function populateVehicleTypeDropdown(vehicleTypes) {
  const dropdown = document.getElementById("vehicleTypeSelect");
  dropdown.innerHTML = '<option value="">Select a vehicle type</option>';

  vehicleTypes.forEach((vehicleType) => {
    const option = document.createElement("option");
    option.value = vehicleType.id;
    option.textContent = `${vehicleType.typeName} - $${vehicleType.pricePerKm}/km`;
    dropdown.appendChild(option);
  });
}
```

### 7. Get Vehicle Types by Fuel Type

**Endpoint:** `GET /admin/vehicle-types/fuel-type/{fuelType}`

```javascript
async function getVehicleTypesByFuelType(fuelType) {
  try {
    const response = await fetch(
      `${VEHICLE_TYPES_ENDPOINT}/fuel-type/${encodeURIComponent(fuelType)}`,
      {
        method: "GET",
        headers: defaultHeaders,
      }
    );

    const result = await response.json();

    if (response.ok) {
      console.log(
        `Retrieved vehicle types with fuel type ${fuelType}:`,
        result.data
      );
      return { success: true, data: result.data };
    } else {
      console.error(
        "Failed to retrieve vehicle types by fuel type:",
        result.message
      );
      return { success: false, error: result.message };
    }
  } catch (error) {
    console.error("Network error:", error);
    return { success: false, error: "Network error occurred" };
  }
}

// Usage example - filter by fuel type
document
  .getElementById("fuelTypeFilter")
  .addEventListener("change", async (e) => {
    const selectedFuelType = e.target.value;

    if (selectedFuelType) {
      const result = await getVehicleTypesByFuelType(selectedFuelType);
      if (result.success) {
        displayVehicleTypesTable(result.data);
      }
    } else {
      // Show all vehicle types
      getAllVehicleTypes();
    }
  });
```

### 8. Get Vehicle Types by Price Range

**Endpoint:** `GET /admin/vehicle-types/price-range?minPrice={minPrice}&maxPrice={maxPrice}`

```javascript
async function getVehicleTypesByPriceRange(minPrice, maxPrice) {
  try {
    const queryParams = new URLSearchParams({
      minPrice: minPrice.toString(),
      maxPrice: maxPrice.toString(),
    });

    const response = await fetch(
      `${VEHICLE_TYPES_ENDPOINT}/price-range?${queryParams}`,
      {
        method: "GET",
        headers: defaultHeaders,
      }
    );

    const result = await response.json();

    if (response.ok) {
      console.log(
        `Retrieved vehicle types in price range $${minPrice}-$${maxPrice}:`,
        result.data
      );
      return { success: true, data: result.data };
    } else {
      console.error(
        "Failed to retrieve vehicle types by price range:",
        result.message
      );
      return { success: false, error: result.message };
    }
  } catch (error) {
    console.error("Network error:", error);
    return { success: false, error: "Network error occurred" };
  }
}

// Usage example - price range filter
document
  .getElementById("priceFilterForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = new FormData(e.target);
    const minPrice = parseFloat(formData.get("minPrice"));
    const maxPrice = parseFloat(formData.get("maxPrice"));

    if (minPrice > maxPrice) {
      showErrorMessage("Minimum price cannot be greater than maximum price");
      return;
    }

    const result = await getVehicleTypesByPriceRange(minPrice, maxPrice);
    if (result.success) {
      displayVehicleTypesTable(result.data);
      showSuccessMessage(
        `Found ${result.data.length} vehicle types in price range $${minPrice}-$${maxPrice}`
      );
    } else {
      showErrorMessage(result.error);
    }
  });
```

## Utility Functions

```javascript
// Show success message
function showSuccessMessage(message) {
  const alertDiv = document.createElement("div");
  alertDiv.className = "alert alert-success";
  alertDiv.textContent = message;
  document.getElementById("alerts").appendChild(alertDiv);

  // Auto-remove after 3 seconds
  setTimeout(() => {
    alertDiv.remove();
  }, 3000);
}

// Show error message
function showErrorMessage(message) {
  const alertDiv = document.createElement("div");
  alertDiv.className = "alert alert-danger";
  alertDiv.textContent = message;
  document.getElementById("alerts").appendChild(alertDiv);

  // Auto-remove after 5 seconds
  setTimeout(() => {
    alertDiv.remove();
  }, 5000);
}

// Display validation errors
function displayValidationErrors(errors) {
  Object.keys(errors).forEach((field) => {
    const fieldElement = document.getElementById(field);
    if (fieldElement) {
      fieldElement.classList.add("is-invalid");

      // Show error message
      let errorDiv = document.getElementById(`${field}Error`);
      if (!errorDiv) {
        errorDiv = document.createElement("div");
        errorDiv.id = `${field}Error`;
        errorDiv.className = "invalid-feedback";
        fieldElement.parentNode.appendChild(errorDiv);
      }
      errorDiv.textContent = errors[field];
    }
  });
}

// Clear validation errors
function clearValidationErrors() {
  document.querySelectorAll(".is-invalid").forEach((element) => {
    element.classList.remove("is-invalid");
  });
  document.querySelectorAll(".invalid-feedback").forEach((element) => {
    element.remove();
  });
}

// Show/hide modals
function showEditModal() {
  document.getElementById("editModal").style.display = "block";
}

function hideEditModal() {
  document.getElementById("editModal").style.display = "none";
  clearValidationErrors();
}
```

## HTML Example

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Vehicle Types Admin</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
  </head>
  <body>
    <div class="container mt-4">
      <h1>Vehicle Types Management</h1>

      <!-- Alerts -->
      <div id="alerts"></div>

      <!-- Filters -->
      <div class="row mb-3">
        <div class="col-md-4">
          <select id="fuelTypeFilter" class="form-select">
            <option value="">All Fuel Types</option>
            <option value="Petrol">Petrol</option>
            <option value="Diesel">Diesel</option>
            <option value="Electric">Electric</option>
            <option value="Hybrid">Hybrid</option>
          </select>
        </div>
        <div class="col-md-8">
          <form id="priceFilterForm" class="d-flex gap-2">
            <input
              type="number"
              name="minPrice"
              placeholder="Min Price"
              class="form-control"
              step="0.01"
            />
            <input
              type="number"
              name="maxPrice"
              placeholder="Max Price"
              class="form-control"
              step="0.01"
            />
            <button type="submit" class="btn btn-primary">Filter</button>
          </form>
        </div>
      </div>

      <!-- Add New Button -->
      <button class="btn btn-success mb-3" onclick="showAddModal()">
        Add New Vehicle Type
      </button>

      <!-- Vehicle Types Table -->
      <table class="table table-striped">
        <thead>
          <tr>
            <th>ID</th>
            <th>Type Name</th>
            <th>Capacity</th>
            <th>Fuel Type</th>
            <th>Price/km</th>
            <th>Available</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody id="vehicleTypesTableBody">
          <!-- Table content will be populated by JavaScript -->
        </tbody>
      </table>
    </div>

    <script src="vehicle-types-admin.js"></script>
  </body>
</html>
```

## Error Handling Best Practices

1. **Always check the response status** before processing data
2. **Handle network errors** separately from API errors
3. **Display validation errors** clearly to users
4. **Provide meaningful error messages** to help users understand what went wrong
5. **Implement retry logic** for network failures
6. **Log errors** for debugging purposes
7. **Show loading states** during API calls
8. **Validate input** on the frontend before sending to API

## Response Status Codes

- **200 OK**: Successful GET, PUT, DELETE operations
- **201 Created**: Successful POST operation
- **400 Bad Request**: Validation errors or business logic errors
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Unexpected server error

This integration guide provides comprehensive examples for all Vehicle Types Admin API endpoints and includes proper error handling, validation, and user feedback mechanisms.
