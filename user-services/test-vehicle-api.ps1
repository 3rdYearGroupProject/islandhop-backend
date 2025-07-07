# Test the driver vehicle endpoints
# First, let's test the health endpoint to make sure the server is running

Write-Host "Testing Driver Vehicle API endpoints..." -ForegroundColor Green

# Test health endpoint
Write-Host "`nTesting health endpoint..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8083/api/v1/driver/health" -Method GET
    Write-Host "Health check: $health" -ForegroundColor Green
} catch {
    Write-Host "Health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Note: The vehicle endpoints require authentication (session with email)
# We would need to first register/login to test those endpoints
Write-Host "`nNote: Vehicle endpoints require authentication." -ForegroundColor Cyan
Write-Host "To test vehicle endpoints, you need to:" -ForegroundColor Cyan
Write-Host "1. Register/login a driver to get a session" -ForegroundColor Cyan
Write-Host "2. Use that session to call GET /api/v1/driver/vehicle" -ForegroundColor Cyan
Write-Host "3. Use that session to call PUT /api/v1/driver/vehicle with form data" -ForegroundColor Cyan
