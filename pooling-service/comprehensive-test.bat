@echo off
echo ================================================
echo    POOLING SERVICE COMPREHENSIVE TEST
echo ================================================
echo.

echo [INFO] This script will run comprehensive tests on the pooling service:
echo [INFO] 1. Unit tests (application context, configuration)
echo [INFO] 2. Connection tests (MongoDB, service startup)
echo [INFO] 3. Health check verification
echo.

REM Step 1: Run unit tests
echo ================================================
echo    STEP 1: RUNNING UNIT TESTS
echo ================================================
echo.
mvn test -Dtest=PoolingServiceConnectionTest

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Unit tests failed! Check output above.
    goto :end
)

echo.
echo [SUCCESS] Unit tests passed!
echo.

REM Step 2: Compile the application
echo ================================================
echo    STEP 2: COMPILING APPLICATION
echo ================================================
echo.
mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed! Check output above.
    goto :end
)

echo.
echo [SUCCESS] Compilation successful!
echo.

REM Step 3: Run connection test (will start the service briefly)
echo ================================================
echo    STEP 3: TESTING LIVE CONNECTIONS
echo ================================================
echo.
echo [INFO] Starting service with connection testing...
echo [INFO] This will test real MongoDB connection and service startup
echo [INFO] The service will automatically exit after testing
echo.

timeout /t 3 /nobreak > nul

set PROFILE=test-connections
set LOGGING_LEVEL=--logging.level.com.islandhop.pooling=INFO
set FORK=false

start /wait mvn spring-boot:run ^
    -Dspring-boot.run.profiles=%PROFILE% ^
    -Dspring-boot.run.arguments="%LOGGING_LEVEL%" ^
    -Dspring-boot.run.fork=%FORK%
echo.
echo ================================================
echo    TEST SUMMARY
echo ================================================
echo.
echo [INFO] All tests completed!
echo [INFO] Review the output above for any errors or warnings.
echo.
echo If you see "ALL STARTUP TESTS PASSED", your service is ready!
echo If you see any errors, check the MongoDB connection string and credentials.
echo.

:end
pause
