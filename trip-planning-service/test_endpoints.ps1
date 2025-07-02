# Trip Planning Service Endpoint Testing Script
# Generated on July 2, 2025

$baseUrl = "http://localhost:8083/api"
$outputFile = "endpoint_test_results.txt"
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

# Clear the output file
"" | Out-File $outputFile

# Function to test endpoint and log result
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
================================================================================

"@
    
    try {
        if ($method -eq "GET") {
            $response = Invoke-RestMethod -Uri $fullUrl -Method $method -Headers $headers -ErrorAction Stop
        } elseif ($method -eq "POST" -and $body) {
            $jsonBody = $body | ConvertTo-Json -Depth 10
            $response = Invoke-RestMethod -Uri $fullUrl -Method $method -Body $jsonBody -ContentType "application/json" -Headers $headers -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $fullUrl -Method $method -Headers $headers -ErrorAction Stop
        }
        
        $result += "STATUS: SUCCESS`n"
        $result += "RESPONSE:`n"
        if ($response -is [string]) {
            $result += $response
        } else {
            $result += ($response | ConvertTo-Json -Depth 10)
        }
    }
    catch {
        $result += "STATUS: ERROR`n"
        $result += "ERROR: $($_.Exception.Message)`n"
        if ($_.Exception.Response) {
            $result += "HTTP STATUS: $($_.Exception.Response.StatusCode)`n"
        }
    }
    
    $result += "`n`n"
    Write-Host "Testing: $method $endpoint"
    Add-Content -Path $outputFile -Value $result
}

# Start testing
"TRIP PLANNING SERVICE ENDPOINT TEST RESULTS" | Out-File $outputFile
"Generated: $timestamp" | Add-Content $outputFile
"Base URL: $baseUrl" | Add-Content $outputFile
"" | Add-Content $outputFile

Write-Host "Starting endpoint testing..."

# 1. Health Checks
Test-Endpoint "GET" "/actuator/health" "Spring Boot Actuator Health Check"
Test-Endpoint "GET" "/trip/health" "Custom Trip Planning Service Health Check"

# 2. Basic Trip Management (These will fail without session, but we can see the error responses)
Test-Endpoint "GET" "/trip/my-trips" "Get User's Trips (will fail without session)"

# 3. Location Search (These should work without session)
Test-Endpoint "GET" "/trip/search-locations?query=Kandy&maxResults=5" "Search for locations - Kandy"
Test-Endpoint "GET" "/trip/search-locations?query=temple&city=Colombo&maxResults=3" "Search for temples in Colombo"
Test-Endpoint "GET" "/trip/search-locations?query=beach&maxResults=5" "Search for beaches"

# 4. Place Categories
Test-Endpoint "GET" "/trip/place-categories" "Get Available Place Categories (will fail without session)"

# 5. Place Validation (will fail without session but shows endpoint structure)
$placeValidationBody = @{
    placeName = "Temple of the Sacred Tooth Relic"
    city = "Kandy"
    description = "Famous Buddhist temple in Kandy"
}
Test-Endpoint "POST" "/trip/validate-place" "Validate Place Information (will fail without session)" -body $placeValidationBody

# 6. Trip Creation (will fail without session)
$tripCreationBody = @{
    baseCity = "Colombo"
    startDate = "2025-07-15"
    endDate = "2025-07-20"
    numberOfDays = 5
    interests = @("Culture", "Nature", "Adventure")
    budgetRange = "MEDIUM"
    travelStyle = "BALANCED"
}
Test-Endpoint "POST" "/trip/initiate" "Create New Trip (will fail without session)" -body $tripCreationBody

Write-Host "Testing completed! Results saved to: $outputFile"
Write-Host "Opening results file..."
