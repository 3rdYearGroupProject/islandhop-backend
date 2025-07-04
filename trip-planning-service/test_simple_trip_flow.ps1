# Simple Full Trip Planning Flow Test Script
# This script tests the complete trip planning workflow using mock session endpoints

$baseUrl = "http://localhost:8083/api"
$outputFile = "simple_trip_flow_results.txt"

# Clear the output file
"" | Out-File $outputFile

function Test-Endpoint {
    param(
        [string]$method,
        [string]$endpoint,
        [string]$description,
        [object]$body = $null
    )
    
    $fullUrl = "$baseUrl$endpoint"
    $result = "[$method] $endpoint - $description`n"
    
    try {
        $params = @{
            Uri = $fullUrl
            Method = $method
            ErrorAction = "Stop"
        }
        
        if ($method -eq "POST" -and $body) {
            $params.Body = ($body | ConvertTo-Json -Depth 10)
            $params.ContentType = "application/json"
        }
        
        $response = Invoke-RestMethod @params
        
        $result += "SUCCESS`n"
        $result += ($response | ConvertTo-Json -Depth 10)
        $result += "`n`n"
        $result | Add-Content $outputFile
        Write-Host "SUCCESS: $description" -ForegroundColor Green
        
        return $response
        
    } catch {
        $result += "ERROR: $($_.Exception.Message)`n`n"
        $result | Add-Content $outputFile
        Write-Host "ERROR: $description - $($_.Exception.Message)" -ForegroundColor Red
        
        return $null
    }
}

Write-Host "==============================" -ForegroundColor Cyan
Write-Host "FULL TRIP PLANNING FLOW TEST" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

# Step 1: Health Check
Write-Host "`n--- Step 1: Health Check ---" -ForegroundColor Yellow
Test-Endpoint "GET" "/trip/health" "Application Health Check"

# Step 2: Session Info
Write-Host "`n--- Step 2: Session Information ---" -ForegroundColor Yellow
Test-Endpoint "GET" "/test/session-info" "Get Mock Session Information"

# Step 3: Create Trips
Write-Host "`n--- Step 3: Trip Creation ---" -ForegroundColor Yellow

$trips = @()

# Trip 1: Cultural Sri Lanka
$trip1Request = @{
    tripName = "Cultural Sri Lanka Adventure"
    startDate = "2025-07-15"
    endDate = "2025-07-20"
    baseCity = "Kandy"
    categories = @("Culture", "Nature")
    pacing = "NORMAL"
}

$trip1 = Test-Endpoint "POST" "/test/create-trip" "Create Cultural Trip" -body $trip1Request
if ($trip1) {
    $trips += $trip1
    Write-Host "Trip 1 ID: $($trip1.tripId)" -ForegroundColor Cyan
}

# Trip 2: Beach Adventure
$trip2Request = @{
    tripName = "Beach Adventure Getaway"
    startDate = "2025-08-01"
    endDate = "2025-08-04"
    baseCity = "Colombo"
    categories = @("Leisure", "Adventure")
    pacing = "ACTIVE"
}

$trip2 = Test-Endpoint "POST" "/test/create-trip" "Create Beach Trip" -body $trip2Request
if ($trip2) {
    $trips += $trip2
    Write-Host "Trip 2 ID: $($trip2.tripId)" -ForegroundColor Cyan
}

# Step 4: Add Places to Trips
Write-Host "`n--- Step 4: Adding Places to Trips ---" -ForegroundColor Yellow

if ($trips.Count -gt 0) {
    $tripId1 = $trips[0].tripId
    
    # Add places to Trip 1
    $place1 = @{
        tripId = $tripId1
        placeName = "Temple of the Sacred Tooth Relic"
        city = "Kandy"
        dayNumber = 1
        placeType = "ATTRACTION"
        estimatedVisitDurationMinutes = 120
        description = "Famous Buddhist temple housing the tooth relic of Buddha"
        priority = 8
    }
    
    Test-Endpoint "POST" "/test/add-place" "Add Temple to Cultural Trip" -body $place1
    
    $place2 = @{
        tripId = $tripId1
        placeName = "Royal Botanical Gardens"
        city = "Peradeniya"
        dayNumber = 2
        placeType = "ATTRACTION"
        estimatedVisitDurationMinutes = 180
        description = "Beautiful botanical gardens near Kandy"
        priority = 7
    }
    
    Test-Endpoint "POST" "/test/add-place" "Add Gardens to Cultural Trip" -body $place2
}

if ($trips.Count -gt 1) {
    $tripId2 = $trips[1].tripId
    
    # Add places to Trip 2
    $place3 = @{
        tripId = $tripId2
        placeName = "Bentota Beach"
        city = "Bentota"
        dayNumber = 1
        placeType = "ATTRACTION"
        estimatedVisitDurationMinutes = 240
        description = "Beautiful beach destination for relaxation"
        priority = 8
    }
    
    Test-Endpoint "POST" "/test/add-place" "Add Beach to Beach Trip" -body $place3
}

# Step 5: Get Day Plans
Write-Host "`n--- Step 5: Retrieving Day Plans ---" -ForegroundColor Yellow

foreach ($trip in $trips) {
    if ($trip -and $trip.tripId) {
        Test-Endpoint "GET" "/test/day-plans/$($trip.tripId)" "Get Day Plans for Trip $($trip.tripId)"
    }
}

# Step 6: Optimize Trip Order
Write-Host "`n--- Step 6: Optimizing Trip Order ---" -ForegroundColor Yellow

foreach ($trip in $trips) {
    if ($trip -and $trip.tripId) {
        Test-Endpoint "POST" "/test/optimize-order/$($trip.tripId)" "Optimize Order for Trip $($trip.tripId)"
    }
}

# Step 7: Get Trip Summaries
Write-Host "`n--- Step 7: Trip Summaries ---" -ForegroundColor Yellow

foreach ($trip in $trips) {
    if ($trip -and $trip.tripId) {
        Test-Endpoint "GET" "/test/trip-summary/$($trip.tripId)" "Get Summary for Trip $($trip.tripId)"
    }
}

# Step 8: Location Search Tests
Write-Host "`n--- Step 8: Location Search Tests ---" -ForegroundColor Yellow

Test-Endpoint "GET" "/test/search-locations?query=Kandy`&maxResults=3" "Search for Kandy"
Test-Endpoint "GET" "/test/search-locations?query=temple`&maxResults=3" "Search for temples"
Test-Endpoint "GET" "/test/search-locations?query=beach`&maxResults=3" "Search for beaches"

# Step 9: Place Validation
Write-Host "`n--- Step 9: Place Validation ---" -ForegroundColor Yellow

$placeToValidate = @{
    placeName = "Galle Fort"
    city = "Galle"
    description = "Historic Dutch colonial fort"
}

Test-Endpoint "POST" "/test/validate-place" "Validate Galle Fort" -body $placeToValidate

Write-Host "`n==============================" -ForegroundColor Green
Write-Host "TESTING COMPLETED!" -ForegroundColor Green
Write-Host "Results saved to: $outputFile" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Green

# Display summary
$content = Get-Content $outputFile -Raw
$successCount = ($content -split "SUCCESS").Count - 1
$errorCount = ($content -split "ERROR").Count - 1

Write-Host "`n=== FINAL TEST SUMMARY ===" -ForegroundColor Magenta
Write-Host "Successful requests: $successCount" -ForegroundColor Green
Write-Host "Failed requests: $errorCount" -ForegroundColor Red
Write-Host "Total requests: $($successCount + $errorCount)" -ForegroundColor Cyan
Write-Host "Created trips count: $($trips.Count)" -ForegroundColor Blue

if ($trips.Count -gt 0) {
    Write-Host "Trip IDs created:" -ForegroundColor Blue
    for ($i = 0; $i -lt $trips.Count; $i++) {
        if ($trips[$i] -and $trips[$i].tripId) {
            Write-Host "  Trip $($i + 1): $($trips[$i].tripId)" -ForegroundColor Blue
        }
    }
}
