A production-style, containerized microservices system built with Spring Boot.
It demonstrates DDD-style separation, JWT/OAuth2 security via Keycloak, Kafka for async events, Redis for caching/sessions, and a Gateway front door. Each service owns its own Postgres database.

## Contents
- High-level Architecture
- Services
- Tech Stack
- Local Setup (Quick Start)
- Configuration
- Databases & Schemas
- Kafka Topics & Events
- Redis Usage
- Security (Keycloak + JWT)
- API Gateway
- Typical Flows
- Testing
- Troubleshooting

## Services
- Product Service — CRUD for products & categories. Caches hot data in Redis.
- Order Service — Manages orders & line items; publishes events to Kafka; calls Payment mock.
- Inventory Service — Listens to order events; decrements/compensates stock.
- User Service — User profile data (business data; auth handled by Keycloak).
- Notification Service — Consumes events; persists and sends notifications (email/SMS stub).
- API Gateway — Entry point; validates JWT; routes to services.
- Keycloak — Authorization server (OpenID Connect); issues JWTs.
- Kafka — Asynchronous communication backbone.
- Redis — Caching and optional session store.
- Payment Mock — WireMock mapping for a fake payment provider.


## Tech Stack
- Java 21, Spring Boot 3.x
- Spring Data JPA, Spring Web, Spring Security (Resource Server), Spring Cloud Gateway
- Kafka (Confluent images), Redis
- PostgreSQL (one DB per service)
- Keycloak (OIDC)
- Testcontainers, JUnit 5
- Docker Compose for local infra

