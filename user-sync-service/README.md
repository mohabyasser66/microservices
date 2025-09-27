# User Sync Service

A Spring Boot service that provides bidirectional synchronization between your application database and Keycloak database, ensuring data consistency across both systems while maintaining optimal performance.

## Overview

This service allows Keycloak to use its own database for fast authentication while keeping your application database synchronized. It provides:

- **Bidirectional Sync**: Changes in either database are synchronized to the other
- **Scheduled Sync**: Automatic synchronization every 5 minutes
- **Manual Triggers**: REST endpoints for manual sync operations
- **Real-time Updates**: Immediate sync on user creation/updates
- **Monitoring**: Health checks and sync status endpoints

## Architecture

```
┌─────────────────┐    Sync    ┌──────────────────┐
│   Application   │ <=======>  │   Keycloak       │
│   Database      │            │   Database       │
│                 │            │                  │
│ - users         │            │ - user_entity    │
│ - roles         │            │ - credential     │
│ - user_roles    │            │ - user_role_map  │
└─────────────────┘            └──────────────────┘
         ^                              ^
         │                              │
         └──────── User Sync Service ───┘
```

## Features

### Core Synchronization
- **User Data**: Username, email, first/last name, enabled status
- **Credentials**: Password synchronization with proper hashing
- **Roles**: Role assignments and mappings
- **Timestamps**: Creation and modification tracking

### Sync Strategies
- **One-way App to Keycloak**: `syncAppToKeycloak()`
- **One-way Keycloak to App**: `syncKeycloakToApp()`
- **Bidirectional**: `syncBidirectional()` (default)

### API Endpoints

#### Sync Operations
```http
POST /api/sync/app-to-keycloak     # Sync from App DB to Keycloak
POST /api/sync/keycloak-to-app     # Sync from Keycloak to App DB
POST /api/sync/bidirectional       # Full bidirectional sync
```

#### Monitoring
```http
GET  /api/sync/status              # Get sync service status
GET  /api/sync/last-sync           # Get last sync timestamp
GET  /api/sync/stats               # Get synchronization statistics
```

## Configuration

### Database Configuration
The service connects to both databases using separate datasources:

```yaml
spring:
  datasource:
    app:
      url: jdbc:postgresql://localhost:5432/user-service
      username: postgres
      password: postgres
    keycloak:
      url: jdbc:postgresql://localhost:5432/keycloak
      username: keycloak
      password: keycloak_password

sync:
  scheduler:
    enabled: true
    fixed-rate: 300000  # 5 minutes in milliseconds
  keycloak:
    realm: microservices-realm
    client-id: admin-cli
    username: admin
    password: admin
```

### Environment Variables
For production deployment:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
APP_DB_NAME=user-service
KEYCLOAK_DB_NAME=keycloak
DB_USERNAME=postgres
DB_PASSWORD=postgres
KEYCLOAK_DB_USERNAME=keycloak
KEYCLOAK_DB_PASSWORD=keycloak_password

# Keycloak Configuration
KEYCLOAK_SERVER_URL=http://localhost:8080
KEYCLOAK_REALM=microservices-realm
KEYCLOAK_CLIENT_ID=admin-cli
KEYCLOAK_USERNAME=admin
KEYCLOAK_PASSWORD=admin

# Service Configuration
EUREKA_SERVER_URL=http://localhost:8761/eureka
```

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL 12+
- Keycloak 23.0.7+
- Maven 3.6+

### Setup

1. **Database Setup**
   ```bash
   # Run the database initialization script
   psql -h localhost -U postgres -f init-db.sql
   ```

2. **Keycloak Setup**
   - Start Keycloak server
   - Create realm: `microservices-realm`
   - Enable admin user or create service account

3. **Application Setup**
   ```bash
   # Build the application
   mvn clean install
   
   # Run the application
   mvn spring-boot:run
   ```

### Docker Deployment

Use the provided Docker Compose configuration:

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f user-sync-service

# Stop services
docker-compose down
```

## Usage Examples

### Manual Synchronization

```bash
# Sync all users from App DB to Keycloak
curl -X POST http://localhost:8083/api/sync/app-to-keycloak

# Sync all users from Keycloak to App DB  
curl -X POST http://localhost:8083/api/sync/keycloak-to-app

# Perform bidirectional sync
curl -X POST http://localhost:8083/api/sync/bidirectional
```

### Monitoring

```bash
# Check service status
curl http://localhost:8083/api/sync/status

# Get sync statistics
curl http://localhost:8083/api/sync/stats

# Response example:
{
  "status": "HEALTHY",
  "lastSyncTime": "2024-01-15T10:30:00Z",
  "totalUsers": 150,
  "syncedUsers": 150,
  "errors": 0,
  "nextScheduledSync": "2024-01-15T10:35:00Z"
}
```

## Synchronization Logic

### User Mapping
```java
App Database (users)     <->    Keycloak Database (user_entity)
├── id                   <->    ├── id
├── username             <->    ├── username  
├── email                <->    ├── email
├── first_name           <->    ├── first_name
├── last_name            <->    ├── last_name
├── enabled              <->    ├── enabled
├── email_verified       <->    ├── email_verified
└── updated_at           <->    └── created_timestamp
```

### Conflict Resolution
- **Timestamp-based**: Most recent update wins
- **Keycloak Priority**: For authentication-specific fields
- **App Priority**: For business-specific fields

### Error Handling
- Failed syncs are logged with detailed error information
- Partial failures don't rollback successful operations
- Retry mechanism for transient errors

## Configuration Options

### Scheduling
```yaml
sync:
  scheduler:
    enabled: true              # Enable/disable scheduled sync
    fixed-rate: 300000         # Sync interval (5 minutes)
    initial-delay: 60000       # Initial delay before first sync
```

### Sync Behavior
```yaml
sync:
  conflict-resolution: TIMESTAMP_BASED  # TIMESTAMP_BASED, KEYCLOAK_PRIORITY, APP_PRIORITY
  batch-size: 100                       # Number of users to process in each batch
  retry-attempts: 3                     # Number of retry attempts for failed operations
  enable-password-sync: true            # Enable password synchronization
```

## Monitoring and Logging

### Health Checks
The service provides health endpoints:
- `/actuator/health` - Overall application health
- `/actuator/health/db-app` - App database connectivity
- `/actuator/health/db-keycloak` - Keycloak database connectivity

### Logging Configuration
```yaml
logging:
  level:
    com.microservices.sync: DEBUG
    org.keycloak: INFO
    org.springframework.jdbc: DEBUG
```

### Metrics
Integration with Micrometer for monitoring:
- Sync operation duration
- Success/failure rates
- User count discrepancies
- Database connection pool metrics

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   ```
   Solution: Verify database URLs, credentials, and network connectivity
   Check: PostgreSQL is running and accessible
   ```

2. **Keycloak Authentication Failed**
   ```
   Solution: Verify Keycloak server URL, realm name, and admin credentials
   Check: Keycloak admin user has proper permissions
   ```

3. **Sync Performance Issues**
   ```
   Solution: Adjust batch-size, optimize database indexes
   Check: Database performance and connection pool settings
   ```

4. **Data Inconsistencies**
   ```
   Solution: Run manual bidirectional sync, check sync logs
   Check: No concurrent modifications during sync operations
   ```

### Debug Mode
Enable debug logging to troubleshoot sync issues:

```yaml
logging:
  level:
    com.microservices.sync.service.UserSyncService: DEBUG
```

## Development

### Project Structure
```
user-sync-service/
├── src/main/java/com/microservices/sync/
│   ├── config/          # Database and Keycloak configuration
│   ├── controller/      # REST API controllers
│   ├── entity/          # JPA entities (app and keycloak)
│   ├── repository/      # Data access layer
│   ├── service/         # Business logic and sync operations
│   └── UserSyncServiceApplication.java
├── src/main/resources/
│   ├── application.yml  # Configuration properties
│   └── logback-spring.xml
├── docker-compose.yml   # Docker deployment configuration
├── init-db.sql         # Database initialization script
└── pom.xml             # Maven dependencies
```

### Testing
```bash
# Run all tests
mvn test

# Run integration tests
mvn test -Dtest=*IntegrationTest

# Run with test profile
mvn spring-boot:run -Dspring.profiles.active=test
```

## Security Considerations

- Database credentials should be stored securely (use environment variables)
- Keycloak admin credentials should have minimal required permissions
- Enable SSL/TLS for database connections in production
- Regular security updates for dependencies
- Monitor sync operations for suspicious activities

## Performance Optimization

- **Database Indexing**: Ensure proper indexes on frequently queried columns
- **Connection Pooling**: Configure appropriate pool sizes for both databases
- **Batch Processing**: Use batch operations for large user sets
- **Caching**: Implement caching for frequently accessed data
- **Async Processing**: Consider async processing for large sync operations

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
