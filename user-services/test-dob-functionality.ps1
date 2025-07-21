# Test DOB functionality in Tourist Profile API
# Run this PowerShell script to test the date of birth field

$baseUrl = "http://localhost:8083/api/v1/tourist"
$testEmail = "test@example.com"

Write-Host "Testing Tourist Profile DOB Functionality" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

# Test 1: Update profile with DOB
Write-Host "`n1. Testing PUT /tourist/profile with DOB" -ForegroundColor Yellow
$profileData = @{
    email       = $testEmail
    firstName   = "John"
    lastName    = "Doe"
    dob         = "1990-05-15"
    nationality = "Sri Lanka"
    languages   = @("English", "සිංහල")
} | ConvertTo-Json

try {
    $putResponse = Invoke-RestMethod -Uri "$baseUrl/profile" -Method PUT -Body $profileData -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $putResponse | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Get profile to verify DOB
Write-Host "`n2. Testing GET /tourist/profile (verify DOB)" -ForegroundColor Yellow
try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/profile?email=$testEmail" -Method GET -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $getResponse | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Update DOB to different date
Write-Host "`n3. Testing PUT /tourist/profile (update DOB only)" -ForegroundColor Yellow
$dobUpdateData = @{
    email = $testEmail
    dob   = "1995-12-25"
} | ConvertTo-Json

try {
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/profile" -Method PUT -Body $dobUpdateData -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $updateResponse | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Set DOB to null
Write-Host "`n4. Testing PUT /tourist/profile (set DOB to null)" -ForegroundColor Yellow
$nullDobData = @{
    email = $testEmail
    dob   = $null
} | ConvertTo-Json

try {
    $nullResponse = Invoke-RestMethod -Uri "$baseUrl/profile" -Method PUT -Body $nullDobData -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $nullResponse | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Test invalid DOB format
Write-Host "`n5. Testing PUT /tourist/profile (invalid DOB format)" -ForegroundColor Yellow
$invalidDobData = @{
    email = $testEmail
    dob   = "invalid-date"
} | ConvertTo-Json

try {
    $invalidResponse = Invoke-RestMethod -Uri "$baseUrl/profile" -Method PUT -Body $invalidDobData -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Cyan
    $invalidResponse | ConvertTo-Json -Depth 3
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nDOB Testing completed!" -ForegroundColor Green

# Frontend integration examples
Write-Host "`n=== Frontend Integration Examples ===" -ForegroundColor Magenta
Write-Host @"

// Example 1: Update profile with DOB
const updateProfile = async () => {
  try {
    const response = await api.put('/tourist/profile', {
      firstName: 'John',
      lastName: 'Doe',
      dob: '1990-05-15',  // ISO date string (YYYY-MM-DD)
      nationality: 'Sri Lanka',
      languages: ['English', 'සිංහල']
    });
    console.log('Profile updated:', response.data);
  } catch (error) {
    console.error('Error:', error);
  }
};

// Example 2: Handle DOB in date picker
const handleDobChange = (e) => {
  const dobValue = e.target.value; // HTML date input gives YYYY-MM-DD format
  setForm({...form, dob: dobValue});
};

// Example 3: Display DOB from profile
const displayDob = (profile) => {
  if (profile.dob) {
    const date = new Date(profile.dob);
    return date.toLocaleDateString(); // Format for display
  }
  return 'Not provided';
};

"@ -ForegroundColor White
