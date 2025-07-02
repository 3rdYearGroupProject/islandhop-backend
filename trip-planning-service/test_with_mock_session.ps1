# Test Script with Mock Session Data
# This script tests endpoints using the test controller that bypasses session validation

$baseUrl = "http://localhost:8083/api"
$outputFile = "mock_session_test_results.txt"
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
        $result += "STATUS: ERROR`n"
        $result += "ERROR: $($_.Exception.Message)`n"
        if ($_.Exception.Response) {
            $result += "HTTP STATUS: $($_.Exception.Response.StatusCode)`n"
        }
    }
    
    $result += "`n`n"
    Write-Host "Testing: $method $endpoint" -ForegroundColor Cyan
    Add-Content -Path $outputFile -Value $result
}

# Start testing
"TRIP PLANNING SERVICE - MOCK SESSION TESTING RESULTS" | Out-File $outputFile
"Generated: $timestamp" | Add-Content $outputFile
"Base URL: $baseUrl" | Add-Content $outputFile
"Testing Mode: Using Test Controller (No Session Required)" | Add-Content $outputFile
"" | Add-Content $outputFile

Write-Host "===============================================" -ForegroundColor Green
Write-Host "TESTING WITH MOCK SESSION DATA" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

# 1. Health Checks (always work)
Test-Endpoint "GET" "/actuator/health" "Spring Boot Health Check"
Test-Endpoint "GET" "/trip/health" "Custom Health Check"

# 2. Test session info
Test-Endpoint "GET" "/test/session-info" "Get Mock Session Information"

# 3. Test location search (the main dynamic search functionality)
Write-Host "`n--- Testing Location Search Functionality ---" -ForegroundColor Yellow

$searchQueries = @(
    @{ query = "Kandy"; description = "Search for Kandy" },
    @{ query = "temple"; description = "Search for temples" },
    @{ query = "beach"; description = "Search for beaches" },
    @{ query = "hotel"; city = "Colombo"; description = "Search for hotels in Colombo" },
    @{ query = "restaurant"; city = "Kandy"; description = "Search for restaurants in Kandy" },
    @{ query = "museum"; biasLat = "6.9271"; biasLng = "79.8612"; description = "Search for museums near Colombo with GPS bias" }
)

foreach ($search in $searchQueries) {
    $endpoint = "/test/search-locations?query=$($search.query)&maxResults=5"
    if ($search.city) { $endpoint += "&city=$($search.city)" }
    if ($search.biasLat) { $endpoint += "&biasLat=$($search.biasLat)&biasLng=$($search.biasLng)" }
    
    Test-Endpoint "GET" $endpoint $search.description
}

# 4. Test place categories
Write-Host "`n--- Testing Place Categories ---" -ForegroundColor Yellow
Test-Endpoint "GET" "/test/place-categories" "Get Available Place Categories"

# 5. Test place validation
Write-Host "`n--- Testing Place Validation ---" -ForegroundColor Yellow

$places = @(
    @{
        placeName = "Temple of the Sacred Tooth Relic"
        city = "Kandy"
        description = "Famous Buddhist temple"
    },
    @{
        placeName = "Sigiriya Rock Fortress"
        city = "Dambulla"
        description = "Ancient rock fortress"
    },
    @{
        placeName = "Galle Fort"
        city = "Galle"
        description = "Historic Dutch fort"
    }
)

foreach ($place in $places) {
    Test-Endpoint "POST" "/test/validate-place" "Validate Place: $($place.placeName)" -body $place
}

# 6. Test trip creation
Write-Host "`n--- Testing Trip Creation ---" -ForegroundColor Yellow

$tripRequests = @(
    @{
        tripName = "Cultural Heritage Tour"
        baseCity = "Colombo"
        startDate = "2025-07-15"
        endDate = "2025-07-20"
        categories = @("Culture", "Nature")
        pacing = "NORMAL"
    },
    @{
        tripName = "Kandy Explorer"
        baseCity = "Kandy"
        startDate = "2025-08-01"
        endDate = "2025-08-05"
        categories = @("Culture", "Leisure")
        pacing = "RELAXED"
    }
)

for ($i = 0; $i -lt $tripRequests.Count; $i++) {
    $trip = $tripRequests[$i]
    Test-Endpoint "POST" "/test/create-trip" "Create Test Trip #$($i + 1)" -body $trip
}

Write-Host "`n===============================================" -ForegroundColor Green
Write-Host "TESTING COMPLETED!" -ForegroundColor Green
Write-Host "Results saved to: $outputFile" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

# Display summary
$content = Get-Content $outputFile -Raw
$successCount = ($content -split "STATUS: SUCCESS").Count - 1
$errorCount = ($content -split "STATUS: ERROR").Count - 1

Write-Host "`n=== TEST SUMMARY ===" -ForegroundColor Magenta
Write-Host "Successful requests: $successCount" -ForegroundColor Green
Write-Host "Failed requests: $errorCount" -ForegroundColor Red
Write-Host "Total requests: $($successCount + $errorCount)" -ForegroundColor Cyan

if ($errorCount -eq 0) {
    Write-Host "`n🎉 ALL TESTS PASSED! Your dynamic search endpoints are working perfectly!" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  Some tests failed. Check the results file for details." -ForegroundColor Yellow
}
