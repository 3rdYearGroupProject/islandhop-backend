@echo off
setlocal EnableDelayedExpansion

:: Test batch file for IslandHop Tourist API endpoints
:: Base URL
set BASE_URL=http://localhost:8083/api/v1/tourists
set FIREBASE_UID=test-firebase-uid-123

echo 1. Register Tourist
curl -X POST "%BASE_URL%/register" ^
  -H "X-Firebase-Auth: %FIREBASE_UID%" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"John Doe\",\"email\":\"john.doe@example.com\",\"dateOfBirth\":\"1990-05-15\",\"nationality\":\"Canadian\",\"languages\":[\"English\",\"French\"]}"
echo.
echo.

echo 2. Get Current Tourist
curl -X GET "%BASE_URL%/me" ^
  -H "X-Firebase-Auth: %FIREBASE_UID%"
echo.
echo.

echo 3. Update Tourist
curl -X PATCH "%BASE_URL%/update" ^
  -H "X-Firebase-Auth: %FIREBASE_UID%" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"John Smith\",\"email\":\"john.smith@example.com\",\"dateOfBirth\":\"1990-05-15\",\"nationality\":\"British\",\"languages\":[\"English\",\"Spanish\"]}"
echo.
echo.

echo 4. Send Verification Code
:: Capture OTP response (note: Windows curl doesn't easily store output, so we display it)
curl -X POST "%BASE_URL%/verify/send" ^
  -H "X-Firebase-Auth: %FIREBASE_UID%"
echo.
set /p OTP="Enter the OTP from the above response: "
echo.

echo 5. Verify OTP
curl -X POST "%BASE_URL%/verify/check?otp=%OTP%" ^
  -H "X-Firebase-Auth: %FIREBASE_UID%"
echo.
echo.

echo 6. Deactivate Tourist
curl -X POST "%BASE_URL%/deactivate" ^
  -H "X-Firebase-Auth: %FIREBASE_UID%"
echo.
echo.

echo 7. Delete Tourist
curl -X DELETE "%BASE_URL%/delete" ^
  -H "X-Firebase-Auth: %FIREBASE_UID%"
echo.
echo.

echo 8. Verify Database State
docker exec -it islandhop-postgres psql -U postgres -d islandhop -c "SELECT * FROM tourists;"
docker exec -it islandhop-postgres psql -U postgres -d islandhop -c "SELECT * FROM tourist_languages;"
echo.

pause
endlocal