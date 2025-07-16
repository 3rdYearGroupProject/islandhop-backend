@echo off
echo Starting PayHere Payment Service...
echo.

REM Set environment variables for development
set PAYHERE_MERCHANT_ID=1234567
set PAYHERE_SECRET=your-secret-key
set PAYHERE_NOTIFY_URL=http://localhost:8088/api/v1/payments/notify
set PAYHERE_SANDBOX=true

echo Environment variables set:
echo PAYHERE_MERCHANT_ID=%PAYHERE_MERCHANT_ID%
echo PAYHERE_SANDBOX=%PAYHERE_SANDBOX%
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

echo Building and starting the service...
mvn clean spring-boot:run

pause
