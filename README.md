# E-Shop Microservices Architecture

Microservices-based e-commerce system built with Spring Boot, featuring API gateway, distributed databases, and event-driven communication.

---

# E-Shop Microservices

Overview of the microservices in this repository and how they communicate.

## What this repo contains

Each folder is a Spring Boot microservice. Below are short descriptions of the main services:

- `api-gateway` — central gateway and edge security (JWT validation, routing, circuit breakers).
- `users-service` — user management, creates email verification tokens and publishes verification requests (moved to async Kafka events).
- `notification-service` — centralized notifications; consumes email verification events and sends emails (SMTP).
- `order-service` — order lifecycle: validates inventory, processes payment, stores order and publishes `OrderPlaced` events.
- `inventory-service` — stock management; provides availability checks used by order placement.
- `product-service` — product catalog (uses MongoDB for products).

Other modules and infrastructure (docker-compose, volumes) live at the repo root.

## Communication patterns

This system uses a mix of synchronous REST calls and asynchronous Kafka events

- Synchronous (REST clients)
  - Services that need immediate answers call other services via HTTP client wrappers (e.g. `UserClient`, `InventoryClient`, `PaymentClient`).
  - Examples: `order-service` calls `inventory-service` to verify stock and calls `payment` to process payment during order placement.

- Asynchronous (Kafka events)
  - Events decouple responsibilities and improve resilience. Key async flows:
    - Email verification: `users-service` publishes an EmailVerification event; `notification-service` consumes it and sends the email.
    - Order notifications: `order-service` publishes `OrderPlaced` events for downstream processing (analytics, notifications, shipping).
  - Consumers use JSON deserialization and are configured to trust the packages used by producers.

## Security

- The API gateway enforces JWT validation at the edge.
- Services also validate tokens (defense in depth). Controller methods use method-level annotations like `@PreAuthorize` where needed.

## Quick run notes

- Use the top-level `docker-compose.yaml` to bring up shared infra (Kafka, Zookeeper, databases, Keycloak when present).
- For local development you can run modules individually from their folders:

```powershell
cd api-gateway
mvnw spring-boot:run

cd ..\order-service
mvnw spring-boot:run

cd ..\notification-service
mvnw spring-boot:run
```

Or build modules with Maven in their folders (or use the parent `mvnw -pl <module> package`).

## Notes & recommendations

- Topic names and important settings (Kafka topics, service URLs) are defined in each service's `application.properties` — change them rather than hardcoding values.
  
---