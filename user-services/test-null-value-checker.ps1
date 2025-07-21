# Test Script for Tourist Null Value Checker Endpoint
# This script tests the new GET /tourist/check-null-values endpoint

Write-Host "=== Tourist Null Value Checker Test ===" -ForegroundColor Green
Write-Host "Testing the new null value checking endpoint..." -ForegroundColor Yellow

# Configuration
$baseUrl = "http://localhost:8083/api/v1"
$testEmail = "test@example.com"

# Create session variable
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

Write-Host "`n1. Testing without authentication (should fail)..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/tourist/check-null-values" -Method Get
    Write-Host "ERROR: Expected authentication failure but got response" -ForegroundColor Red
}
catch {
    Write-Host "✓ Correctly returned authentication error: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n2. Creating test user and logging in..." -ForegroundColor Cyan
try {
    # Register test user
    $registerBody = @{
        idToken = "test-token-123"
        role    = "tourist"
    } | ConvertTo-Json

    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/session-register" -Method Post -Body $registerBody -ContentType "application/json" -WebSession $session
    Write-Host "✓ User registration successful" -ForegroundColor Green

    # Login test user
    $loginBody = @{
        idToken = "test-token-123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/login" -Method Post -Body $loginBody -ContentType "application/json" -WebSession $session
    Write-Host "✓ User login successful" -ForegroundColor Green
}
catch {
    Write-Host "⚠ User might already exist, trying login only..." -ForegroundColor Yellow
    try {
        $loginBody = @{
            idToken = "test-token-123"
        } | ConvertTo-Json
        $loginResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/login" -Method Post -Body $loginBody -ContentType "application/json" -WebSession $session
        Write-Host "✓ User login successful" -ForegroundColor Green
    }
    catch {
        Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n3. Testing null value checker with session authentication..." -ForegroundColor Cyan
try {
    $nullCheckResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/check-null-values" -Method Get -WebSession $session
    Write-Host "✓ Null value check successful" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Yellow
    $nullCheckResponse | ConvertTo-Json -Depth 10 | Write-Host
    
    # Analyze results
    if ($nullCheckResponse.nullFields -and $nullCheckResponse.nullFields.Count -gt 0) {
        Write-Host "`n📊 Analysis: Found null values in the following tables:" -ForegroundColor Magenta
        $nullCheckResponse.nullFields.PSObject.Properties | ForEach-Object {
            Write-Host "  - $($_.Name): $($_.Value | ConvertTo-Json -Compress)" -ForegroundColor White
        }
    }
    else {
        Write-Host "`n✅ Analysis: No null values found - all data is complete!" -ForegroundColor Green
    }
}
catch {
    Write-Host "✗ Null value check failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}

Write-Host "`n4. Testing null value checker with email parameter..." -ForegroundColor Cyan
try {
    $nullCheckResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/check-null-values?email=$testEmail" -Method Get -WebSession $session
    Write-Host "✓ Null value check with email parameter successful" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Yellow
    $nullCheckResponse | ConvertTo-Json -Depth 10 | Write-Host
}
catch {
    Write-Host "✗ Null value check with email parameter failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}

Write-Host "`n5. Testing with non-existent email..." -ForegroundColor Cyan
try {
    $nullCheckResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/check-null-values?email=nonexistent@example.com" -Method Get -WebSession $session
    Write-Host "ERROR: Expected failure for non-existent email but got response" -ForegroundColor Red
}
catch {
    Write-Host "✓ Correctly returned error for non-existent email: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n6. Creating some test data to verify null detection..." -ForegroundColor Cyan
try {
    # Complete profile with some fields
    $profileBody = @{
        email       = $testEmail
        firstName   = "Test"
        lastName    = "User"
        nationality = "US"
        languages   = @("English")
    } | ConvertTo-Json

    $profileResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/complete-profile" -Method Post -Body $profileBody -ContentType "application/json" -WebSession $session
    Write-Host "✓ Profile created successfully" -ForegroundColor Green
    
    # Check null values again
    $nullCheckResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/check-null-values" -Method Get -WebSession $session
    Write-Host "✓ Null value check after profile creation:" -ForegroundColor Green
    $nullCheckResponse | ConvertTo-Json -Depth 10 | Write-Host
    
    # Analyze changes
    if ($nullCheckResponse.nullFields -and $nullCheckResponse.nullFields.Count -gt 0) {
        Write-Host "`n📊 Analysis: After profile creation, still found null values:" -ForegroundColor Magenta
        $nullCheckResponse.nullFields.PSObject.Properties | ForEach-Object {
            Write-Host "  - $($_.Name): $($_.Value | ConvertTo-Json -Compress)" -ForegroundColor White
        }
    }
    else {
        Write-Host "`n✅ Analysis: All null values resolved after profile creation!" -ForegroundColor Green
    }
}
catch {
    Write-Host "⚠ Profile creation failed (might already exist): $($_.Exception.Message)" -ForegroundColor Yellow
    
    # Still check null values
    try {
        $nullCheckResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/check-null-values" -Method Get -WebSession $session
        Write-Host "✓ Null value check after profile attempt:" -ForegroundColor Green
        $nullCheckResponse | ConvertTo-Json -Depth 10 | Write-Host
    }
    catch {
        Write-Host "✗ Null value check failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n7. Testing settings creation and null detection..." -ForegroundColor Cyan
try {
    # Create settings
    $settingsBody = @{
        currency = "USD"
        units    = "Imperial"
    } | ConvertTo-Json

    $settingsResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/settings" -Method Put -Body $settingsBody -ContentType "application/json" -WebSession $session
    Write-Host "✓ Settings created successfully" -ForegroundColor Green
    
    # Check null values again
    $nullCheckResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/check-null-values" -Method Get -WebSession $session
    Write-Host "✓ Final null value check after settings creation:" -ForegroundColor Green
    $nullCheckResponse | ConvertTo-Json -Depth 10 | Write-Host
    
    # Final analysis
    if ($nullCheckResponse.nullFields -and $nullCheckResponse.nullFields.Count -gt 0) {
        Write-Host "`n📊 Final Analysis: Remaining null values:" -ForegroundColor Magenta
        $nullCheckResponse.nullFields.PSObject.Properties | ForEach-Object {
            Write-Host "  - $($_.Name): $($_.Value | ConvertTo-Json -Compress)" -ForegroundColor White
        }
    }
    else {
        Write-Host "`n🎉 Final Analysis: All tourist data is complete - no null values!" -ForegroundColor Green
    }
}
catch {
    Write-Host "✗ Settings creation failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Summary ===" -ForegroundColor Green
Write-Host "Tourist Null Value Checker endpoint testing completed." -ForegroundColor Yellow
Write-Host "Review the results above to verify the endpoint works correctly." -ForegroundColor Yellow

# Performance test
Write-Host "`n8. Performance test - checking response time..." -ForegroundColor Cyan
try {
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $nullCheckResponse = Invoke-RestMethod -Uri "$baseUrl/tourist/check-null-values" -Method Get -WebSession $session
    $stopwatch.Stop()
    
    Write-Host "✓ Response time: $($stopwatch.ElapsedMilliseconds) ms" -ForegroundColor Green
    if ($stopwatch.ElapsedMilliseconds -lt 1000) {
        Write-Host "✅ Performance: Excellent (< 1 second)" -ForegroundColor Green
    }
    elseif ($stopwatch.ElapsedMilliseconds -lt 3000) {
        Write-Host "⚠ Performance: Good (< 3 seconds)" -ForegroundColor Yellow
    }
    else {
        Write-Host "⚠ Performance: Slow (> 3 seconds)" -ForegroundColor Red
    }
}
catch {
    Write-Host "✗ Performance test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🏁 All tests completed!" -ForegroundColor Green
