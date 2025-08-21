@echo off
REM Test script for the new public comprehensive trip endpoint

echo Testing the public comprehensive trip endpoint...

REM Test with sample data
set TRIP_ID=trip_001
set USER_ID=user_123
set BASE_URL=http://localhost:8080

REM Test the endpoint for anonymous users
echo Testing GET /api/v1/public-pooling/trips/%TRIP_ID%/comprehensive (anonymous)
curl -X GET "%BASE_URL%/api/v1/public-pooling/trips/%TRIP_ID%/comprehensive" ^
  -H "Content-Type: application/json" ^
  -w "\nStatus: %%{http_code}\n"

echo.
echo ----------------------------------------
echo.

REM Test the endpoint for logged-in users
echo Testing GET /api/v1/public-pooling/trips/%TRIP_ID%/comprehensive?userId=%USER_ID% (logged-in)
curl -X GET "%BASE_URL%/api/v1/public-pooling/trips/%TRIP_ID%/comprehensive?userId=%USER_ID%" ^
  -H "Content-Type: application/json" ^
  -w "\nStatus: %%{http_code}\n"

echo.
echo Test completed.
pause
