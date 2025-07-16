@echo off
echo Running PayHere Payment Service Tests...
echo.

REM Set test environment variables
set PAYHERE_MERCHANT_ID=TEST_MERCHANT_ID
set PAYHERE_SECRET=TEST_SECRET
set PAYHERE_NOTIFY_URL=http://localhost:8088/api/v1/payments/notify
set PAYHERE_SANDBOX=true

echo Running unit tests...
mvn test

if %errorlevel% neq 0 (
    echo Tests failed!
    pause
    exit /b 1
)

echo All tests passed!
pause
