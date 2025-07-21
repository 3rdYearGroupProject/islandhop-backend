# Tourist Settings API Test Script
# Run this PowerShell script to test the tourist settings endpoints

$baseUrl = "http://localhost:8083/api/v1/tourist"
$testEmail = "test@example.com"

Write-Host "Testing Tourist Settings API Endpoints" -ForegroundColor Green
Write-Host "=======================================" -ForegroundColor Green

# Test 1: Get settings for user (should return null values if no settings exist)
Write-Host "`n1. Testing GET /tourist/settings (first time - should return nulls)" -ForegroundColor Yellow
try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/settings?email=$testEmail" -Method GET -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $getResponse | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Create/Update settings
Write-Host "`n2. Testing PUT /tourist/settings (create new settings)" -ForegroundColor Yellow
$settingsData = @{
    email    = $testEmail
    currency = "EUR"
    units    = "Metric"
} | ConvertTo-Json

try {
    $putResponse = Invoke-RestMethod -Uri "$baseUrl/settings" -Method PUT -Body $settingsData -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $putResponse | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Get settings again (should return the values we just set)
Write-Host "`n3. Testing GET /tourist/settings (after update - should return values)" -ForegroundColor Yellow
try {
    $getResponse2 = Invoke-RestMethod -Uri "$baseUrl/settings?email=$testEmail" -Method GET -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $getResponse2 | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Update existing settings
Write-Host "`n4. Testing PUT /tourist/settings (update existing settings)" -ForegroundColor Yellow
$updateData = @{
    email    = $testEmail
    currency = "USD"
    units    = "Imperial"
} | ConvertTo-Json

try {
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/settings" -Method PUT -Body $updateData -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $updateResponse | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Partial update (only currency)
Write-Host "`n5. Testing PUT /tourist/settings (partial update - currency only)" -ForegroundColor Yellow
$partialData = @{
    email    = $testEmail
    currency = "GBP"
} | ConvertTo-Json

try {
    $partialResponse = Invoke-RestMethod -Uri "$baseUrl/settings" -Method PUT -Body $partialData -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $partialResponse | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Final check
Write-Host "`n6. Final GET /tourist/settings (verify final state)" -ForegroundColor Yellow
try {
    $finalResponse = Invoke-RestMethod -Uri "$baseUrl/settings?email=$testEmail" -Method GET -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $finalResponse | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nTesting completed!" -ForegroundColor Green

# Example JavaScript test calls for frontend integration
Write-Host "`n=== JavaScript Frontend Integration Examples ===" -ForegroundColor Magenta
Write-Host @"

// Example 1: Get settings
const getSettings = async () => {
  try {
    const response = await api.get('/tourist/settings');
    console.log('Settings:', response.data);
    // Expected: { email, currency, units } or { email, currency: null, units: null }
  } catch (error) {
    console.error('Error:', error);
  }
};

// Example 2: Update settings
const updateSettings = async () => {
  try {
    const response = await api.put('/tourist/settings', {
      currency: 'EUR',
      units: 'Metric'
    });
    console.log('Updated:', response.data);
  } catch (error) {
    console.error('Error:', error);
  }
};

"@ -ForegroundColor White
