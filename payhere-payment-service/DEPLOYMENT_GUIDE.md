# Deployment Guide - PayHere Payment Service

This guide provides step-by-step instructions for deploying the PayHere Payment Service in different environments.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PayHere merchant account
- Environment variables configured

## Environment Setup

### 1. Set Environment Variables

Create a `.env` file in the project root:

```bash
# PayHere Configuration
PAYHERE_MERCHANT_ID=your_merchant_id
PAYHERE_SECRET=your_secret_key
PAYHERE_NOTIFY_URL=http://localhost:8088/api/v1/payments/notify
PAYHERE_SANDBOX=true

# Server Configuration
SERVER_PORT=8088
```

### 2. Configure Application Properties

Update `src/main/resources/application.yml` for your environment:

```yaml
# Production Configuration
server:
  port: ${SERVER_PORT:8088}
  servlet:
    context-path: /api/v1

spring:
  profiles:
    active: prod

payhere:
  merchant-id: ${PAYHERE_MERCHANT_ID}
  secret: ${PAYHERE_SECRET}
  notify-url: ${PAYHERE_NOTIFY_URL}
  sandbox: ${PAYHERE_SANDBOX:false}
```

## Deployment Options

### Option 1: Local Development

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd payhere-payment-service
   ```

2. **Set environment variables**

   ```bash
   # Windows
   set PAYHERE_MERCHANT_ID=your_merchant_id
   set PAYHERE_SECRET=your_secret_key
   set PAYHERE_NOTIFY_URL=http://localhost:8088/api/v1/payments/notify
   set PAYHERE_SANDBOX=true

   # Linux/Mac
   export PAYHERE_MERCHANT_ID=your_merchant_id
   export PAYHERE_SECRET=your_secret_key
   export PAYHERE_NOTIFY_URL=http://localhost:8088/api/v1/payments/notify
   export PAYHERE_SANDBOX=true
   ```

3. **Build and run**

   ```bash
   ./mvnw clean compile
   ./mvnw test
   ./mvnw spring-boot:run
   ```

4. **Or use the batch file (Windows)**
   ```bash
   start-service.bat
   ```

### Option 2: Docker Deployment

1. **Build Docker image**

   ```bash
   docker build -t payhere-payment-service:latest .
   ```

2. **Run with Docker**

   ```bash
   docker run -d \
     --name payhere-payment-service \
     -p 8088:8088 \
     -e PAYHERE_MERCHANT_ID=your_merchant_id \
     -e PAYHERE_SECRET=your_secret_key \
     -e PAYHERE_NOTIFY_URL=http://localhost:8088/api/v1/payments/notify \
     -e PAYHERE_SANDBOX=true \
     payhere-payment-service:latest
   ```

3. **Or use Docker Compose**
   ```bash
   docker-compose up -d
   ```

### Option 3: JAR Deployment

1. **Build JAR file**

   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Run JAR file**
   ```bash
   java -jar target/payhere-payment-service-1.0.0.jar \
     --payhere.merchant-id=your_merchant_id \
     --payhere.secret=your_secret_key \
     --payhere.notify-url=http://localhost:8088/api/v1/payments/notify \
     --payhere.sandbox=true
   ```

### Option 4: Cloud Deployment (AWS/Azure/GCP)

#### AWS Deployment

1. **Create Dockerfile**

   ```dockerfile
   FROM openjdk:17-jdk-slim
   WORKDIR /app
   COPY target/payhere-payment-service-1.0.0.jar app.jar
   EXPOSE 8088
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. **Deploy to AWS ECS/EKS**

   ```bash
   # Build and push to ECR
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
   docker build -t payhere-payment-service .
   docker tag payhere-payment-service:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/payhere-payment-service:latest
   docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/payhere-payment-service:latest
   ```

3. **Set environment variables in AWS**
   - Use AWS Systems Manager Parameter Store
   - Or AWS Secrets Manager for sensitive data

#### Kubernetes Deployment

1. **Create deployment.yaml**

   ```yaml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: payhere-payment-service
   spec:
     replicas: 3
     selector:
       matchLabels:
         app: payhere-payment-service
     template:
       metadata:
         labels:
           app: payhere-payment-service
       spec:
         containers:
           - name: payhere-payment-service
             image: payhere-payment-service:latest
             ports:
               - containerPort: 8088
             env:
               - name: PAYHERE_MERCHANT_ID
                 valueFrom:
                   secretKeyRef:
                     name: payhere-secrets
                     key: merchant-id
               - name: PAYHERE_SECRET
                 valueFrom:
                   secretKeyRef:
                     name: payhere-secrets
                     key: secret
               - name: PAYHERE_NOTIFY_URL
                 value: "http://your-domain.com/api/v1/payments/notify"
               - name: PAYHERE_SANDBOX
                 value: "false"
   ---
   apiVersion: v1
   kind: Service
   metadata:
     name: payhere-payment-service
   spec:
     selector:
       app: payhere-payment-service
     ports:
       - protocol: TCP
         port: 8088
         targetPort: 8088
     type: LoadBalancer
   ```

2. **Deploy to Kubernetes**
   ```bash
   kubectl apply -f deployment.yaml
   ```

## Production Configuration

### 1. Security Settings

```yaml
# application-prod.yml
server:
  port: 8088
  ssl:
    enabled: true
    key-store: classpath:keystore.jks
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: JKS

payhere:
  sandbox: false

logging:
  level:
    com.islandhop.payment: INFO
    org.springframework: WARN
```

### 2. Database Configuration (Optional)

If you want to use a database instead of in-memory storage:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payhere_payments
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

### 3. Monitoring and Health Checks

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

## Post-Deployment Verification

### 1. Health Check

```bash
curl -X GET http://localhost:8088/api/v1/payments/health
```

Expected response:

```json
{
  "status": "UP",
  "service": "PayHere Payment Service",
  "timestamp": 1721041200000
}
```

### 2. Test Payment Creation

```bash
curl -X POST http://localhost:8088/api/v1/payments/create-payhere-payment \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "LKR",
    "orderId": "TEST_ORDER_123",
    "itemName": "Test Item",
    "customerDetails": {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phone": "0771234567",
      "address": "123 Test Street",
      "city": "Colombo",
      "country": "Sri Lanka"
    }
  }'
```

### 3. Load Testing

Use Apache Bench or similar tools:

```bash
ab -n 1000 -c 10 -H "Content-Type: application/json" \
  -p test-payload.json \
  http://localhost:8088/api/v1/payments/create-payhere-payment
```

## Monitoring and Logging

### 1. Log Configuration

```yaml
logging:
  level:
    com.islandhop.payment: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/payhere-payment-service.log
    max-size: 10MB
    max-history: 7
```

### 2. Monitoring Tools

- **Prometheus**: For metrics collection
- **Grafana**: For visualization
- **ELK Stack**: For log aggregation
- **New Relic/DataDog**: For APM

### 3. Alerting

Set up alerts for:

- High error rates
- Slow response times
- Payment failures
- Service downtime

## Backup and Recovery

### 1. Database Backup (if using database)

```bash
# PostgreSQL backup
pg_dump -h localhost -U username -d payhere_payments > backup.sql

# Restore
psql -h localhost -U username -d payhere_payments < backup.sql
```

### 2. Application Logs

```bash
# Archive logs
tar -czf logs-$(date +%Y%m%d).tar.gz /var/log/payhere-payment-service.log

# Rotate logs
logrotate /etc/logrotate.d/payhere-payment-service
```

## Scaling

### 1. Horizontal Scaling

- Deploy multiple instances behind a load balancer
- Use session affinity if needed
- Consider database connection pooling

### 2. Vertical Scaling

```bash
# Increase JVM heap size
java -Xmx2g -Xms1g -jar payhere-payment-service.jar
```

### 3. Auto-scaling (Kubernetes)

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: payhere-payment-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: payhere-payment-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

## Security Best Practices

1. **Use HTTPS in production**
2. **Secure environment variables**
3. **Implement rate limiting**
4. **Use Web Application Firewall (WAF)**
5. **Regular security updates**
6. **Input validation and sanitization**
7. **Secure logging (no sensitive data)**

## Troubleshooting

### Common Issues

1. **Service not starting**

   - Check Java version (require Java 17+)
   - Verify environment variables
   - Check port availability

2. **PayHere integration issues**

   - Verify merchant credentials
   - Check hash generation
   - Validate notification URLs

3. **Performance issues**
   - Monitor JVM memory usage
   - Check database connections
   - Analyze logs for bottlenecks

### Log Analysis

```bash
# Check application logs
tail -f /var/log/payhere-payment-service.log

# Filter error logs
grep "ERROR" /var/log/payhere-payment-service.log

# Check payment-specific logs
grep "Payment" /var/log/payhere-payment-service.log
```

## Rollback Strategy

1. **Keep previous version available**
2. **Database migration rollback scripts**
3. **Blue-green deployment**
4. **Canary releases**

## Support

For deployment issues:

1. Check the logs first
2. Verify configuration
3. Test with Postman collection
4. Contact development team

## Updates and Maintenance

1. **Regular security updates**
2. **Performance monitoring**
3. **Log rotation**
4. **Database maintenance**
5. **Certificate renewals**

This deployment guide covers most common scenarios. Adjust according to your specific infrastructure and requirements.
