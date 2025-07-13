@echo off
echo ================================================
echo    POOLING SERVICE UNIT TESTS
echo ================================================
echo.

echo [INFO] Running unit tests to verify service configuration...
echo [INFO] This will test application context and basic connectivity
echo.

REM Run Maven tests
mvn test -Dtest=PoolingServiceConnectionTest

echo.
echo [INFO] Unit tests completed.
echo [INFO] Check the logs above for test results.
echo.
pause
