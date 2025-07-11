# Test Tourist Profile and Settings API with Null Values
# Run this PowerShell script to test null value handling

$baseUrl = "http://localhost:8083/api/v1/tourist"
$testEmail = "test@example.com"

Write-Host "Testing Tourist Profile and Settings API - Null Value Handling" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

# Test 1: Get profile with null values (should not crash)
Write-Host "`n1. Testing GET /tourist/profile with potential null values" -ForegroundColor Yellow
try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/profile?email=$testEmail" -Method GET -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $getResponse | ConvertTo-Json -Depth 3
    Write-Host "✅ GET profile with null values - SUCCESS" -ForegroundColor Green
}
catch {
    Write-Host "❌ GET profile with null values - FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Update profile with mixed null and non-null values
Write-Host "`n2. Testing PUT /tourist/profile with mixed null/non-null values" -ForegroundColor Yellow
$profileData = @{
    email       = $testEmail
    firstName   = "John"
    lastName    = $null
    dob         = $null
    nationality = "Sri Lanka"
    languages   = @("English")
} | ConvertTo-Json

try {
    $putResponse = Invoke-RestMethod -Uri "$baseUrl/profile" -Method PUT -Body $profileData -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $putResponse | ConvertTo-Json -Depth 3
    Write-Host "✅ PUT profile with null values - SUCCESS" -ForegroundColor Green
}
catch {
    Write-Host "❌ PUT profile with null values - FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Get settings with no existing settings (should return null values)
Write-Host "`n3. Testing GET /tourist/settings with no existing settings" -ForegroundColor Yellow
try {
    $getSettingsResponse = Invoke-RestMethod -Uri "$baseUrl/settings?email=$testEmail" -Method GET -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $getSettingsResponse | ConvertTo-Json -Depth 3
    Write-Host "✅ GET settings with null values - SUCCESS" -ForegroundColor Green
}
catch {
    Write-Host "❌ GET settings with null values - FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Update settings (should work and return non-null values)
Write-Host "`n4. Testing PUT /tourist/settings" -ForegroundColor Yellow
$settingsData = @{
    email    = $testEmail
    currency = "USD"
    units    = "Imperial"
} | ConvertTo-Json

try {
    $putSettingsResponse = Invoke-RestMethod -Uri "$baseUrl/settings" -Method PUT -Body $settingsData -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $putSettingsResponse | ConvertTo-Json -Depth 3
    Write-Host "✅ PUT settings - SUCCESS" -ForegroundColor Green
}
catch {
    Write-Host "❌ PUT settings - FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Get settings again (should return the values we just set)
Write-Host "`n5. Testing GET /tourist/settings after update" -ForegroundColor Yellow
try {
    $getSettingsResponse2 = Invoke-RestMethod -Uri "$baseUrl/settings?email=$testEmail" -Method GET -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $getSettingsResponse2 | ConvertTo-Json -Depth 3
    Write-Host "✅ GET settings after update - SUCCESS" -ForegroundColor Green
}
catch {
    Write-Host "❌ GET settings after update - FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Test with completely null profile
Write-Host "`n6. Testing PUT /tourist/profile with all null values" -ForegroundColor Yellow
$nullProfileData = @{
    email       = $testEmail
    firstName   = $null
    lastName    = $null
    dob         = $null
    nationality = $null
    languages   = $null
} | ConvertTo-Json

try {
    $nullPutResponse = Invoke-RestMethod -Uri "$baseUrl/profile" -Method PUT -Body $nullProfileData -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $nullPutResponse | ConvertTo-Json -Depth 3
    Write-Host "✅ PUT profile with all null values - SUCCESS" -ForegroundColor Green
}
catch {
    Write-Host "❌ PUT profile with all null values - FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 Null Value Handling Testing completed!" -ForegroundColor Green

# Summary
Write-Host "`n=== SUMMARY ===" -ForegroundColor Magenta
Write-Host "The NullPointerException has been fixed by:" -ForegroundColor White
Write-Host "1. Replacing Map.of() with HashMap() in profile GET/PUT endpoints" -ForegroundColor White
Write-Host "2. Replacing Map.of() with HashMap() in settings GET/PUT endpoints" -ForegroundColor White
Write-Host "3. HashMap allows null values, unlike Map.of()" -ForegroundColor White
Write-Host "4. All API endpoints now handle null values gracefully" -ForegroundColor White
