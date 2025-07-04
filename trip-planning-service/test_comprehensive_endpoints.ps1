# Enhanced Trip Planning Service Endpoint Testing Script
# Tests with mock session and explores all endpoints

$baseUrl = "http://localhost:8083/api"
$outputFile = "comprehensive_endpoint_test_results.txt"
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
        [hashtable]$headers = @{},
        [switch]$expectError
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
        }
        
        $response = Invoke-RestMethod @params
        
        $result += "STATUS: SUCCESS`n"
        $result += "RESPONSE:`n"
        if ($response -is [string]) {
            $result += $response
        } else {
            $result += ($response | ConvertTo-Json -Depth 10)
        }
    }
    catch {
        $statusText = if ($expectError) { "EXPECTED ERROR" } else { "ERROR" }
        $result += "STATUS: $statusText`n"
        $result += "ERROR: $($_.Exception.Message)`n"
        if ($_.Exception.Response) {
            $result += "HTTP STATUS: $($_.Exception.Response.StatusCode)`n"
            try {
                $errorStream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($errorStream)
                $errorBody = $reader.ReadToEnd()
                if ($errorBody) {
                    $result += "ERROR BODY: $errorBody`n"
                }
            } catch {
                # Ignore error reading error body
            }
        }
    }
    
    $result += "`n`n"
    Write-Host "Testing: $method $endpoint"
    Add-Content -Path $outputFile -Value $result
}

# Start testing
"COMPREHENSIVE TRIP PLANNING SERVICE ENDPOINT TEST RESULTS" | Out-File $outputFile
"Generated: $timestamp" | Add-Content $outputFile
"Base URL: $baseUrl" | Add-Content $outputFile
"" | Add-Content $outputFile

Write-Host "Starting comprehensive endpoint testing..."

# 1. Health Checks (Should work)
Test-Endpoint "GET" "/actuator/health" "Spring Boot Actuator Health Check"
Test-Endpoint "GET" "/actuator/health/tripPlanning" "Custom Health Indicator"
Test-Endpoint "GET" "/trip/health" "Custom Trip Planning Service Health Check"

# 2. Test endpoints that might be public or have different behavior
Test-Endpoint "GET" "/trip/search-locations?query=test" "Basic search test" -expectError
Test-Endpoint "GET" "/trip/place-categories" "Place categories test" -expectError

# 3. Test with invalid data to see validation responses
$invalidTripBody = @{
    invalidField = "test"
}
Test-Endpoint "POST" "/trip/initiate" "Trip creation with invalid data" -body $invalidTripBody -expectError

# 4. Test place validation
$validPlaceBody = @{
    placeName = "Kandy Temple"
    city = "Kandy"
}
Test-Endpoint "POST" "/trip/validate-place" "Place validation test" -body $validPlaceBody -expectError

# 5. Test various search queries
$searchQueries = @(
    "Kandy",
    "temple",
    "beach",
    "hotel",
    "restaurant"
)

foreach ($query in $searchQueries) {
    Test-Endpoint "GET" "/trip/search-locations?query=$query&maxResults=3" "Search for: $query" -expectError
}

# 6. Test trip management endpoints (will fail but show structure)
Test-Endpoint "GET" "/trip/my-trips" "Get user trips" -expectError
Test-Endpoint "GET" "/trip/123/map-data" "Get trip map data (fake ID)" -expectError
Test-Endpoint "GET" "/trip/123/suggestions" "Get trip suggestions (fake ID)" -expectError
Test-Endpoint "GET" "/trip/123/day/1" "Get day plan (fake ID)" -expectError

# 7. Test travel info endpoints
Test-Endpoint "GET" "/trip/123/travel-info?fromPlaceId=place1&toPlaceId=place2" "Get travel info (fake IDs)" -expectError

# 8. Test contextual search
Test-Endpoint "GET" "/trip/123/contextual-search?query=restaurant" "Contextual search (fake ID)" -expectError

# 9. Test place details
Test-Endpoint "GET" "/trip/place-details/ChIJkxHhCHm2-lsR" "Get place details (fake ID)" -expectError

# 10. Test different HTTP methods on some endpoints
Test-Endpoint "OPTIONS" "/trip/health" "OPTIONS request on health endpoint"
Test-Endpoint "HEAD" "/trip/health" "HEAD request on health endpoint"

Write-Host "Comprehensive testing completed! Results saved to: $outputFile"
Write-Host ""
Write-Host "Summary of findings will be displayed..."

# Display a summary
$content = Get-Content $outputFile -Raw
$successCount = ($content -split "STATUS: SUCCESS").Count - 1
$errorCount = ($content -split "STATUS: ERROR").Count - 1
$expectedErrorCount = ($content -split "STATUS: EXPECTED ERROR").Count - 1

Write-Host "=== TEST SUMMARY ===" -ForegroundColor Green
Write-Host "Successful endpoints: $successCount" -ForegroundColor Green
Write-Host "Error endpoints: $errorCount" -ForegroundColor Red
Write-Host "Expected error endpoints: $expectedErrorCount" -ForegroundColor Yellow
Write-Host "Total endpoints tested: $($successCount + $errorCount + $expectedErrorCount)" -ForegroundColor Cyan
