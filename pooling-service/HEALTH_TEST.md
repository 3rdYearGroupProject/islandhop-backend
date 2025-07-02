# Pooling Service Health Tests

## Quick Health Check

Test the service endpoints to verify deployment readiness:

### 1. Health Endpoint
```bash
curl http://localhost:8086/api/health
```
Expected response:
```json
{
  "status": "UP",
  "service": "pooling-service", 
  "timestamp": "2025-07-02T18:14:03.290",
  "version": "1.0.0"
}
```

### 2. Readiness Endpoint
```bash
curl http://localhost:8086/api/ready
```
Expected response:
```json
{
  "status": "READY",
  "service": "pooling-service",
  "timestamp": "2025-07-02T18:14:03.290"
}
```

### 3. API Endpoints Test
```bash
# Test pooling suggestions (requires other services)
curl -X POST http://localhost:8086/api/pooling/suggestions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user-123",
    "tripId": "test-trip-456",
    "maxPoolSize": 4,
    "minCompatibilityScore": 0.7
  }'
```

## Docker Health Checks

When running via Docker Compose, the service includes built-in health checks:

```bash
# Check container health status
docker-compose ps

# View health check logs
docker-compose logs pooling-service | grep health
```

## Production Readiness Checklist

- ✅ Service compiles and builds successfully
- ✅ JAR runs standalone (tested)  
- ✅ Health endpoints respond correctly
- ✅ Docker image builds successfully
- ✅ Multi-stage Dockerfile for optimized production image
- ✅ Docker Compose configuration with health checks
- ✅ MongoDB integration configured
- ✅ External service client configuration
- ✅ Environment-specific configuration files
- ✅ Security: Non-root user in container
- ✅ Monitoring: Health check endpoints
- ✅ Documentation: API examples and deployment instructions

## Deployment Verification Steps

1. **Build Test**: `mvn clean package -DskipTests`
2. **JAR Test**: `java -jar target/pooling-service-0.0.1-SNAPSHOT.jar`
3. **Health Test**: `curl http://localhost:8086/api/health`
4. **Docker Build**: `docker build -t pooling-service .`
5. **Docker Run**: `docker-compose up -d`
6. **Integration Test**: Test API endpoints with sample data

## Notes

- MongoDB connection errors are expected until MongoDB is running
- Service will start successfully even without external dependencies
- Health endpoints work independently of database connectivity
- For full functionality, ensure trip-planning-service and user-services are available
