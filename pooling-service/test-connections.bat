@echo off
echo ================================================
echo    POOLING SERVICE CONNECTION TEST
echo ================================================
echo.

echo [INFO] Starting connection test...
echo [INFO] This will test MongoDB connection and service startup
echo.

REM Run the service with test-connections profile
echo [INFO] Running service with connection testing enabled...
mvn spring-boot:run -Dspring-boot.run.profiles=test-connections -Dspring-boot.run.arguments="--logging.level.com.islandhop.pooling=DEBUG"

echo.
echo [INFO] Connection test completed.
echo [INFO] Check the logs above for test results.
echo.
pause
