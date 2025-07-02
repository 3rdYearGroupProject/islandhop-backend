@echo off
REM IslandHop Pooling Service - Production Deployment Script

echo ========================================
echo IslandHop Pooling Service Deployment
echo ========================================

echo.
echo Step 1: Building Docker image...
docker build -t islandhop/pooling-service:latest .

if %ERRORLEVEL% neq 0 (
    echo ERROR: Docker build failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Creating network if it doesn't exist...
docker network create islandhop-network 2>nul

echo.
echo Step 3: Starting services...
docker-compose up -d

if %ERRORLEVEL% neq 0 (
    echo ERROR: Docker compose failed!
    pause
    exit /b 1
)

echo.
echo Step 4: Waiting for services to be ready...
timeout /t 30 /nobreak >nul

echo.
echo Step 5: Checking service health...
curl -s http://localhost:8086/api/health

echo.
echo ========================================
echo Deployment Complete!
echo ========================================
echo Pooling Service: http://localhost:8086
echo Health Check: http://localhost:8086/api/health
echo MongoDB: localhost:27017
echo.
echo To view logs: docker-compose logs -f
echo To stop: docker-compose down
echo ========================================

pause
