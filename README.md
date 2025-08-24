# E-Shop Microservices Architecture

A production-ready microservices-based e-commerce system built with Spring Boot, featuring service discovery, API gateway, distributed databases, and event-driven communication.

---

## Architecture Overview

This project implements a microservices architecture with the following key components:

- **Service Discovery**: Eureka Server for service registration and discovery
- **API Gateway**: Spring Cloud Gateway for routing and security
- **Business Services**: Product, Order, Inventory, User, and Notification services
- **Database**: MySQL for order and inventory, MongoDB for product, others as needed
- **Communication**: REST APIs and event-driven patterns (Kafka)
- **Security**: JWT-based authentication (configured but not fully implemented)
- **Containerization**: Docker Compose for local orchestration

---

## Project Structure (Updated)

```
microservices/
├── api-gateway/               # Spring Cloud Gateway (port: 8080)
│   ├── docker-compose.yaml
│   ├── src/main/resources/application.properties
│   └── volume-data/mysql_keycloak_data/   # Docker volume for Keycloak/MySQL
├── product-service/           # Product catalog (MongoDB, port: 27017)
│   ├── docker-compose.yml
│   ├── data/                  # MongoDB data volume (ignored in git)
│   └── src/main/resources/application.properties
├── inventory-service/         # Stock management (MySQL, port: 3316)
│   ├── docker-compose.yaml
│   ├── docker/mysql/data/     # MySQL data volume (ignored in git)
│   ├── mysql/init.sql
│   └── src/main/resources/application.properties
├── order-service/             # Order processing (MySQL, port: 3306)
│   ├── docker-compose.yaml
│   ├── docker/mysql/init.sql
│   ├── mysql/                 # MySQL data volume (ignored in git)
│   └── src/main/resources/application.properties
├── users-service/             # User management (port: see application.properties)
│   └── src/main/resources/application.properties
├── notification-service/      # Email/SMS notifications (Kafka, ports: 9092, 29092)
│   └── src/main/resources/application.properties
├── pom.xml                    # Parent POM
└── README.md                  # This file
```

---

## Recent Changes & Details

### API Gateway (`api-gateway`)
- **Port:** 8080 (fixed)
- **Docker:** Contains `docker-compose.yaml` and persistent volume for Keycloak/MySQL.
- **Security:** JWT-based authentication configured.
- **Routing:** Uses Spring Cloud Gateway MVC. Swagger routes must proxy to actual service `/v3/api-docs` endpoints.
- **Application Properties:** Eureka client enabled, resilience4j retry configured.

### Product Service (`product-service`)
- **Database:** Uses MongoDB (not PostgreSQL).
- **Docker:** Has its own `docker-compose.yml` for MongoDB.
- **Data Folder:** `data/` contains MongoDB data files, ignored in git.
- **Application Properties:** Configured for MongoDB.

### Inventory Service (`inventory-service`)
- **Database:** Uses MySQL.
- **Docker:** Has its own `docker-compose.yaml` for MySQL.
- **Data Folder:** `docker/mysql/data/` contains MySQL data files, ignored in git.
- **Flyway:** Database migrations managed via Flyway.
- **Application Properties:** Configured for MySQL.

### Order Service (`order-service`)
- **Port:** 8082 (set in `application.properties`)
- **Database:** Uses MySQL.
- **Docker:** Has its own `docker-compose.yaml` for MySQL, Kafka, Zookeeper, Schema Registry, and Kafka UI.
- **MySQL Data:** Local `mysql/` folder is used as a Docker volume (should be ignored in `.gitignore`).
- **Flyway:** Database migrations managed via Flyway.
- **Application Properties:** Uses MySQL connection.

### Users Service (`users-service`)
- **Database:** Not specified, likely uses an in-memory or external DB.
- **Application Properties:** Port and DB config set in `application.properties`.

### Notification Service (`notification-service`)
- **Port:** 8087 (set in `application.properties`)
- **Kafka:** Consumes events from `order-placed` topic.
- **Event Class:** Contains `OrderPlacedEvent` in `com.techie.microservices.order.event` (must match producer for deserialization).
- **Email:** Configured for Mailtrap SMTP.
- **Kafka Consumer:** Uses trusted packages and ErrorHandlingDeserializer for robust message handling.

---

## Updated Service Ports

| Service                  | Port    | Database         |
| ------------------------ | ------- | --------------- |
| **API Gateway**          | 8080    | -               |
| **Product Service**      | 8081    | MongoDB           |
| **Inventory Service**    | 8083    | MySQL             |
| **Order Service**        | 8082    | MySQL           |
| **Users Service**        | 8084    | -      |
| **Notification Service** | 8087    | -               |

---

## 📝 Additional Notes

- **Kafka & Schema Registry:** Order and notification services use Kafka for event-driven communication. Schema Registry is configured in Docker Compose.
- **Keycloak:** API Gateway includes Keycloak configuration for authentication (see `volume-data/mysql_keycloak_data/`).
- **.gitignore:** Data folders for Docker volumes (e.g., `mysql/`, `data/`, `volume-data/`) are excluded from version control.
- **Swagger:** To view Swagger docs for each service via the gateway, ensure routes proxy to the correct `/v3/api-docs` endpoints.
- **Database:** Order and inventory services use MySQL, product service uses MongoDB.

---

## Quick Start

1. **Start API Gateway:**  
   ```sh
   cd api-gateway
   mvn spring-boot:run
   ```

2. **Start Product Service (with Docker):**  
   ```sh
   cd product-service
   docker-compose up -d
   mvn spring-boot:run
   ```

3. **Start Inventory Service (with Docker):**  
   ```sh
   cd inventory-service
   docker-compose up -d
   mvn spring-boot:run
   ```

4. **Start Order Service (with Docker):**  
   ```sh
   cd order-service
   docker-compose up -d
   mvn spring-boot:run
   ```

5. **Start Users Service:**  
   ```sh
   cd users-service
   mvn spring-boot:run
   ```

6. **Start Notification Service:**  
   ```sh
   cd notification-service
   mvn spring-boot:run
   ```

---

## Troubleshooting 

- **Kafka Deserialization:** Ensure event classes exist in both producer and consumer with matching package names.
- **Database Issues:** Confirm MySQL and MongoDB containers are running and data folders are not pushed to GitHub.
- **Swagger via Gateway:** Update gateway routes to proxy `/v3/api-docs` to each service’s actual endpoint.
- **Authentication:** Keycloak is present for API Gateway; configure clients as needed.
- **Docker Volumes:** Data folders (`mysql/`, `data/`, `volume-data/`) are ignored in git and used for persistence.

---

**For more details, see each service's `application.properties` and `docker-compose` files.**