# E-Shop Microservices Architecture

A production-ready microservices-based e-commerce system built with Spring Boot, featuring service discovery, API gateway, distributed databases, and event-driven communication.

## 🏗️ Architecture Overview

This project implements a microservices architecture with the following key components:

- **Service Discovery**: Eureka Server for service registration and discovery
- **API Gateway**: Spring Cloud Gateway for routing and security
- **Business Services**: Product, Order, Inventory, User, and Notification services
- **Database**: PostgreSQL with separate databases per service
- **Communication**: REST APIs and event-driven patterns
- **Security**: JWT-based authentication (configured but not fully implemented)

## 📋 Table of Contents

- [Services Overview](#services-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Service Communication](#service-communication)
- [Development Guide](#development-guide)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

## 🚀 Services Overview

### Core Business Services

| Service                  | Port    | Description                | Database            |
| ------------------------ | ------- | -------------------------- | ------------------- |
| **Discovery Server**     | 8761    | Eureka service registry    | -                   |
| **API Gateway**          | 8080    | Route requests to services | -                   |
| **Product Service**      | Dynamic | Product catalog management | `product-service`   |
| **Inventory Service**    | Dynamic | Stock management           | `inventory-service` |
| **Order Service**        | Dynamic | Order processing           | `order-service`     |
| **User Service**         | Dynamic | User management            | `user-service`      |
| **Notification Service** | Dynamic | Email/SMS notifications    | -                   |

### Service Details

#### 🔍 Discovery Server (Eureka)
- **Port**: 8761
- **Purpose**: Service registration and discovery
- **Features**: 
  - Service health monitoring
  - Load balancing support
  - Service metadata management

#### 🌐 API Gateway
- **Port**: 8080
- **Purpose**: Single entry point for all client requests
- **Routes**:
  - `/api/product/*` → Product Service
  - `/api/order/*` → Order Service
  - `/eureka/*` → Discovery Server
- **Features**: Request routing, load balancing, security

#### 📦 Product Service
- **Port**: Dynamic
- **Purpose**: Product catalog management
- **Features**:
  - CRUD operations for products
  - Category management
  - Image handling
  - Product search and filtering

#### 📊 Inventory Service
- **Port**: Dynamic
- **Purpose**: Stock management and availability
- **Features**:
  - Real-time stock checking
  - Stock updates
  - Inventory health monitoring
  - Data initialization

#### 🛒 Order Service
- **Port**: Dynamic
- **Purpose**: Order processing and management
- **Features**:
  - Order creation and management
  - Integration with inventory service
  - Order history
  - Payment integration (mock)

#### 👤 User Service
- **Port**: Dynamic
- **Purpose**: User profile management
- **Features**:
  - User registration and profiles
  - Role-based access control
  - User data management

#### 📧 Notification Service
- **Port**: Dynamic
- **Purpose**: Email and SMS notifications
- **Features**:
  - Email service integration
  - Notification persistence
  - Event-driven notifications

## 🛠️ Technology Stack

### Core Technologies
- **Java**: 21
- **Spring Boot**: 3.5.4
- **Spring Cloud**: 2025.0.0
- **Maven**: Dependency management and build tool

### Spring Framework Components
- **Spring Web**: RESTful web services
- **Spring Data JPA**: Database access
- **Spring Security**: Authentication and authorization
- **Spring Cloud Gateway**: API gateway
- **Spring Cloud Netflix Eureka**: Service discovery

### Database & Storage
- **PostgreSQL**: Primary database (separate instance per service)
- **Hibernate**: ORM framework
- **HikariCP**: Connection pooling

### Additional Libraries
- **Lombok**: Code generation and boilerplate reduction
- **ModelMapper**: Object mapping
- **Testcontainers**: Integration testing

## 📁 Project Structure

```
microservices/
├── discovery-server/          # Eureka service registry
├── api-gateway/              # Spring Cloud Gateway
├── product-service/          # Product catalog management
├── inventory-service/        # Stock management
├── order-service/           # Order processing
├── users-service/           # User management
├── notification-service/    # Email/SMS notifications
├── pom.xml                  # Parent POM
└── README.md               # This file
```

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git

### 1. Database Setup
Create the required PostgreSQL databases:

```sql
CREATE DATABASE "product-service";
CREATE DATABASE "inventory-service";
CREATE DATABASE "order-service";
CREATE DATABASE "user-service";
```

### 2. Configuration
Update database credentials in each service's `application.properties`:

```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build and Run

#### Option A: Run All Services
```bash
# Build all services
mvn clean install

# Start Discovery Server first
cd discovery-server
mvn spring-boot:run

# Start other services (in separate terminals)
cd ../api-gateway && mvn spring-boot:run
cd ../product-service && mvn spring-boot:run
cd ../inventory-service && mvn spring-boot:run
cd ../order-service && mvn spring-boot:run
cd ../users-service && mvn spring-boot:run
cd ../notification-service && mvn spring-boot:run
```

#### Option B: Run Individual Services
```bash
# Build specific service
mvn clean install -pl service-name

# Run specific service
mvn spring-boot:run -pl service-name
```

### 4. Verify Installation
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761 or http://localhost:8080/eureka/web
- **All Other Services**: Access through API Gateway using service names

### 5. Finding Service Ports
Since most services use dynamic ports, you can find their actual ports by:

1. **Using Eureka Dashboard**: 
   - Go to http://localhost:8080/eureka/web
   - View registered services and their ports

2. **Using Application Logs**:
   - Check the startup logs of each service
   - Look for: "Tomcat started on port(s): XXXX"

3. **Using API Gateway**:
   - All services are accessible through the API Gateway
   - Use the service routes defined in the gateway configuration

## ⚙️ Configuration

### Service Ports
| Service              | Default Port | Configuration      |
| -------------------- | ------------ | ------------------ |
| Discovery Server     | 8761         | `server.port=8761` |
| API Gateway          | 8080         | `server.port=8080` |
| Product Service      | Dynamic      | `server.port=0`    |
| Inventory Service    | Dynamic      | `server.port=0`    |
| Order Service        | Dynamic      | `server.port=0`    |
| User Service         | Dynamic      | `server.port=0`    |
| Notification Service | Dynamic      | `server.port=0`    |

**Note**: Dynamic ports (`server.port=0`) allow Spring Boot to automatically assign available ports, preventing port conflicts in development and enabling multiple instances in production.

### Database Configuration
Each service uses its own PostgreSQL database:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/{service-name}
spring.datasource.username=postgres
spring.datasource.password=862475139
spring.jpa.hibernate.ddl-auto=update
```

### Eureka Configuration
All services register with Eureka:

```properties
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
```

## 📚 API Documentation

### Product Service APIs

#### Get All Products
```http
GET /api/product/products/all
```

#### Get Product by ID
```http
GET /api/product/products/{id}
```

#### Add Product
```http
POST /api/product/products/add
Content-Type: application/json

{
  "name": "iPhone 15",
  "description": "Latest iPhone model",
  "price": 999.99,
  "categoryId": 1
}
```

#### Update Product
```http
PUT /api/product/products/{productId}
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "description": "Updated description",
  "price": 1099.99
}
```

#### Delete Product
```http
DELETE /api/product/products/{productId}
```

### Inventory Service APIs

#### Check Stock Availability
```http
GET /api/inventory/api/inventory?skuCode=iphone-15&skuCode=samsung-s24
```

#### Health Check
```http
GET /api/inventory/api/inventory/health
```

#### Database Health
```http
GET /api/inventory/api/inventory/db-health
```

### Order Service APIs

#### Place Order
```http
POST /api/order/api/order
Content-Type: application/json

{
  "orderItems": [
    {
      "skuCode": "iphone-15",
      "price": 999.99,
      "quantity": 1
    }
  ]
}
```

#### Get All Orders
```http
GET /api/order/api/order
```

#### Health Check
```http
GET /api/order/api/order/health
```

### User Service APIs

#### Get All Users
```http
GET /api/users/users/all
```

#### Get User by ID
```http
GET /api/users/users/{id}
```

#### Create User
```http
POST /api/users/users/add
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Update User
```http
PUT /api/users/users/{userId}
Content-Type: application/json

{
  "firstName": "John Updated",
  "lastName": "Doe Updated"
}
```

#### Delete User
```http
DELETE /api/users/users/{userId}
```


## 🔄 Service Communication

### Synchronous Communication
- **REST APIs**: Direct service-to-service communication
- **WebClient**: Used in Order Service to call Inventory Service
- **Load Balancing**: Eureka provides client-side load balancing

### Service Dependencies
```
Order Service → Inventory Service (stock checking)
Order Service → Product Service (product validation)
API Gateway → All Services (routing)
```

### Health Checks
Each service provides health check endpoints:
- `/health` - Basic service health
- `/db-health` - Database connectivity (where applicable)

## 👨‍💻 Development Guide

### Adding a New Service

1. **Create Service Structure**:
```bash
mkdir new-service
cd new-service
# Copy structure from existing service
```

2. **Update Parent POM**:
```xml
<modules>
    <module>new-service</module>
</modules>
```

3. **Configure Application Properties**:
```properties
spring.application.name=new-service
server.port=8085
spring.datasource.url=jdbc:postgresql://localhost:5432/new-service
```

4. **Register with Eureka**:
```properties
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
eureka.client.register-with-eureka=true
```

### Code Style Guidelines
- Use Lombok for boilerplate code reduction
- Implement proper exception handling
- Use DTOs for API requests/responses
- Follow RESTful API design principles
- Add comprehensive logging

### Best Practices
- **Database**: Use separate databases per service
- **Configuration**: Externalize configuration properties
- **Security**: Implement proper authentication/authorization
- **Monitoring**: Add health checks and metrics
- **Testing**: Write unit and integration tests

## 🧪 Testing

### Unit Testing
```bash
# Run tests for specific service
mvn test -pl service-name

# Run all tests
mvn test
```

### Integration Testing
- Use Testcontainers for database testing
- Mock external service dependencies
- Test service-to-service communication

### API Testing
- Use Postman or similar tools
- Test through API Gateway endpoints
- Verify service discovery and routing

## 🔧 Troubleshooting

### Common Issues

#### Service Not Starting
1. Check if required ports are available (API Gateway uses fixed port 8080)
2. Verify database connectivity
3. Check application.properties configuration
4. Ensure Eureka server is running
5. For dynamic port services, check logs for assigned port numbers

#### Service Discovery Issues
1. Verify Eureka server is accessible at http://localhost:8761 or through API Gateway at http://localhost:8080/eureka/web
2. Check service registration in Eureka dashboard
3. Verify network connectivity between services
4. Ensure services are using `server.port=0` for dynamic port assignment

#### Database Connection Issues
1. Check PostgreSQL service is running
2. Verify database credentials
3. Ensure databases exist
4. Check connection pool settings

#### API Gateway Routing Issues
1. Verify service names match Eureka registration
2. Check route configuration in application.properties
3. Ensure services are healthy in Eureka

### Logs and Debugging
- Enable debug logging in application.properties
- Check service logs for error messages
- Use Eureka dashboard for service status
- Monitor database connections

### Performance Optimization
- Configure connection pools appropriately
- Enable caching where beneficial
- Monitor service response times
- Use load balancing effectively