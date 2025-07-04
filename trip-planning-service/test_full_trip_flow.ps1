# Full Trip Planning Flow Test Script
# This script tests the complete trip planning workflow using mock session endpoints

$baseUrl = "http://localhost:8083/api"
$outputFile = "full_trip_flow_results.txt"
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

# Clear the output file
"" | Out-File $outputFile

function Test-Endpoint {
    param(
        [string]$method,
        [string]$endpoint,
        [string]$description,
        [object]$body = $null,
        [hashtable]$headers = @{}
    )
    
    $fullUrl = "$baseUrl$endpoint"
    $result = @"
================================================================================
ENDPOINT: $method $endpoint
DESCRIPTION: $description
TIMESTAMP: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
FULL URL: $fullUrl
================================================================================

"@
    
    try {
        $params = @{
            Uri = $fullUrl
            Method = $method
            Headers = $headers
            ErrorAction = "Stop"
        }
        
        if ($method -eq "POST" -and $body) {
            $params.Body = ($body | ConvertTo-Json -Depth 10)
            $params.ContentType = "application/json"
            $result += "REQUEST BODY:`n"
            $result += ($body | ConvertTo-Json -Depth 10)
            $result += "`n`n"
        }
        
        $response = Invoke-RestMethod @params
        
        $result += "STATUS: SUCCESS`n"
        $result += "RESPONSE:`n"
        if ($response -is [string]) {
            $result += $response
        } else {
            $result += ($response | ConvertTo-Json -Depth 10)
        }
        
        $result += "`n`n"
        $result | Add-Content $outputFile
        Write-Host "✓ $description" -ForegroundColor Green
        
        return $response
        
    } catch {
        $result += "STATUS: ERROR`n"
        $result += "ERROR MESSAGE: $($_.Exception.Message)`n"
        if ($_.Exception.Response) {
            $result += "HTTP STATUS: $($_.Exception.Response.StatusCode)`n"
            $result += "HTTP REASON: $($_.Exception.Response.ReasonPhrase)`n"
        }
        $result += "`n`n"
        $result | Add-Content $outputFile
        Write-Host "✗ $description - $($_.Exception.Message)" -ForegroundColor Red
        
        return $null
    }
}

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "FULL TRIP PLANNING FLOW TEST" -ForegroundColor Cyan
Write-Host "Testing complete workflow from creation to summary" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

# Step 1: Test application health
Write-Host "`n--- Step 1: Health Check ---" -ForegroundColor Yellow
Test-Endpoint "GET" "/trip/health" "Application Health Check"

# Step 2: Test session info
Write-Host "`n--- Step 2: Session Information ---" -ForegroundColor Yellow
Test-Endpoint "GET" "/test/session-info" "Get Mock Session Information"

# Step 3: Test available categories
Write-Host "`n--- Step 3: Available Categories ---" -ForegroundColor Yellow
$categories = Test-Endpoint "GET" "/test/place-categories" "Get Available Place Categories"

# Step 4: Test location search functionality
Write-Host "`n--- Step 4: Location Search Tests ---" -ForegroundColor Yellow

$searchTests = @(
    @{ query = "Kandy"; description = "Search for Kandy (city)" },
    @{ query = "temple"; description = "Search for temples" },
    @{ query = "Sigiriya"; description = "Search for Sigiriya" },
    @{ query = "beach"; city = "Bentota"; description = "Search for beaches in Bentota" }
)

foreach ($search in $searchTests) {
    $endpoint = "/test/search-locations?query=$($search.query)`&maxResults=3"
    if ($search.city) { $endpoint += "`&city=$($search.city)" }
    
    Test-Endpoint "GET" $endpoint $search.description
}

# Step 5: Create trips with correct structure
Write-Host "`n--- Step 5: Trip Creation ---" -ForegroundColor Yellow

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

$trip1 = Test-Endpoint "POST" "/test/create-trip" "Create Cultural Trip 5 days" -body $trip1Request
if ($trip1) {
    $trips += $trip1
    Write-Host "  -> Trip 1 ID: $($trip1.tripId)" -ForegroundColor Cyan
}

# Trip 2: Beach & Adventure
$trip2Request = @{
    tripName = "Beach and Adventure Getaway"
    startDate = "2025-08-01"
    endDate = "2025-08-04"
    baseCity = "Colombo"
    categories = @("Leisure", "Adventure")
    pacing = "ACTIVE"
}

$trip2 = Test-Endpoint "POST" "/test/create-trip" "Create Beach and Adventure Trip 4 days" -body $trip2Request
if ($trip2) {
    $trips += $trip2
    Write-Host "  -> Trip 2 ID: $($trip2.tripId)" -ForegroundColor Cyan
}

# Step 6: Add places to trips
Write-Host "`n--- Step 6: Adding Places to Trips ---" -ForegroundColor Yellow

if ($trips.Count -gt 0) {
    $tripId1 = $trips[0].tripId
    
    # Places to add to Trip 1 (Cultural)
    $placesToAdd1 = @(
        @{
            placeName = "Temple of the Sacred Tooth Relic"
            city = "Kandy"
            category = "Culture"
            timeSpent = 2.0
            description = "Famous Buddhist temple housing the tooth relic of Buddha"
        },
        @{
            placeName = "Royal Botanical Gardens"
            city = "Peradeniya"
            category = "Nature"
            timeSpent = 3.0
            description = "Beautiful botanical gardens near Kandy"
        },
        @{
            placeName = "Sigiriya Rock Fortress"
            city = "Dambulla"
            category = "Culture"
            timeSpent = 4.0
            description = "Ancient rock fortress and UNESCO World Heritage site"
        }
    )
    
    foreach ($place in $placesToAdd1) {
        $addPlaceRequest = @{
            tripId = $tripId1
            placeName = $place.placeName
            city = $place.city
            dayNumber = 1  # Add to first day by default
            placeType = "ATTRACTION"
            estimatedVisitDurationMinutes = [int]($place.timeSpent * 60)
            description = $place.description
            priority = 7
        }
        
        Test-Endpoint "POST" "/test/add-place" "Add '$($place.placeName)' to Cultural Trip" -body $addPlaceRequest
    }
}

if ($trips.Count -gt 1) {
    $tripId2 = $trips[1].tripId
    
    # Places to add to Trip 2 (Beach & Adventure)
    $placesToAdd2 = @(
        @{
            placeName = "Bentota Beach"
            city = "Bentota"
            category = "Leisure"
            timeSpent = 4.0
            description = "Beautiful beach destination for relaxation"
        },
        @{
            placeName = "Madu River Safari"
            city = "Balapitiya"
            category = "Adventure"
            timeSpent = 3.0
            description = "Exciting river safari through mangroves"
        }
    )
    
    foreach ($place in $placesToAdd2) {
        $addPlaceRequest = @{
            tripId = $tripId2
            placeName = $place.placeName
            city = $place.city
            dayNumber = 1  # Add to first day by default
            placeType = "ATTRACTION"
            estimatedVisitDurationMinutes = [int]($place.timeSpent * 60)
            description = $place.description
            priority = 7
        }
        
        Test-Endpoint "POST" "/test/add-place" "Add '$($place.placeName)' to Beach Trip" -body $addPlaceRequest
    }
}

# Step 7: Get day plans for trips
Write-Host "`n--- Step 7: Retrieving Day Plans ---" -ForegroundColor Yellow

foreach ($trip in $trips) {
    if ($trip -and $trip.tripId) {
        Test-Endpoint "GET" "/test/day-plans/$($trip.tripId)" "Get Day Plans for Trip: $($trip.tripName)"
    }
}

# Step 8: Optimize trip order
Write-Host "`n--- Step 8: Optimizing Trip Order ---" -ForegroundColor Yellow

foreach ($trip in $trips) {
    if ($trip -and $trip.tripId) {
        Test-Endpoint "POST" "/test/optimize-order/$($trip.tripId)" "Optimize Order for Trip: $($trip.tripName)"
    }
}

# Step 9: Get complete trip summaries
Write-Host "`n--- Step 9: Trip Summaries ---" -ForegroundColor Yellow

foreach ($trip in $trips) {
    if ($trip -and $trip.tripId) {
        Test-Endpoint "GET" "/test/trip-summary/$($trip.tripId)" "Get Complete Summary for Trip: $($trip.tripName)"
    }
}

# Step 10: Test place validation
Write-Host "`n--- Step 10: Place Validation Tests ---" -ForegroundColor Yellow

$placesToValidate = @(
    @{
        placeName = "Galle Fort"
        city = "Galle"
        description = "Historic Dutch colonial fort"
    },
    @{
        placeName = "Adams Peak"
        city = "Nallathanniya"
        description = "Sacred mountain pilgrimage site"
    },
    @{
        placeName = "Non-existent Place"
        city = "Unknown City"
        description = "This should fail validation"
    }
)

foreach ($place in $placesToValidate) {
    Test-Endpoint "POST" "/test/validate-place" "Validate Place: $($place.placeName)" -body $place
}

Write-Host "`n===============================================" -ForegroundColor Green
Write-Host "FULL TRIP FLOW TESTING COMPLETED!" -ForegroundColor Green
Write-Host "Results saved to: $outputFile" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

# Display summary
$content = Get-Content $outputFile -Raw
$successCount = ($content -split "STATUS: SUCCESS").Count - 1
$errorCount = ($content -split "STATUS: ERROR").Count - 1

Write-Host "`n=== FINAL TEST SUMMARY ===" -ForegroundColor Magenta
Write-Host "Successful requests: $successCount" -ForegroundColor Green
Write-Host "Failed requests: $errorCount" -ForegroundColor Red
Write-Host "Total requests: $($successCount + $errorCount)" -ForegroundColor Cyan

if ($errorCount -eq 0) {
    Write-Host "`nALL TESTS PASSED! Complete trip planning flow is working!" -ForegroundColor Green
} elseif ($successCount -gt $errorCount) {
    Write-Host "`nMost tests passed! Core functionality is working." -ForegroundColor Yellow
    Write-Host "Check the results file for any specific issues." -ForegroundColor Yellow
} else {
    Write-Host "`nSeveral tests failed. Check the results file for details." -ForegroundColor Red
}

Write-Host "`nCreated trips count: $($trips.Count)" -ForegroundColor Blue
if ($trips.Count -gt 0) {
    Write-Host "Trip IDs created:" -ForegroundColor Blue
    for ($i = 0; $i -lt $trips.Count; $i++) {
        if ($trips[$i] -and $trips[$i].tripId) {
            Write-Host "   - Trip $($i + 1): $($trips[$i].tripId) - $($trips[$i].tripName)" -ForegroundColor Blue
        }
    }
}
