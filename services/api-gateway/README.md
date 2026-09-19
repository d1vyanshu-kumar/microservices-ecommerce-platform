# API Gateway

The single entry point for all client traffic in the microservices e-commerce platform. Every request from the Angular frontend passes through this gateway before reaching any downstream service.

Built with **Spring Cloud Gateway MVC** on **Spring Boot 3.3**, it handles route matching, JWT token validation via Keycloak, and circuit breaker fallbacks so that individual service failures don't cascade into full outages.

## What it does

- Routes `/api/product`, `/api/order`, and `/api/inventory` to the correct backend service
- Validates OAuth2 JWT tokens issued by Keycloak before forwarding authenticated requests
- Wraps every route in a Resilience4j circuit breaker with configurable thresholds
- Aggregates Swagger/OpenAPI docs from all services into a single UI at `/swagger-ui.html`
- Exposes Actuator + Prometheus metrics for monitoring

## Request flow

<img width="1912" height="758" alt="mermaid-diagram-2026-09-20-000617" src="https://github.com/user-attachments/assets/663db12f-6ce8-4efe-8e9a-9bff3e3b2436" />


## Security

The gateway acts as an **OAuth2 Resource Server**. `GET /api/product` is public so anyone can browse the catalog without logging in. All other endpoints (placing orders, adding products) require a valid JWT.

Tokens are issued by Keycloak and validated against the realm's JWK endpoint. There's a custom `JwtDecoder` that handles the issuer mismatch between the browser-facing URL (`localhost:8181`) and the Docker-internal hostname (`keycloak:8080`), so the same token works regardless of where the gateway is running.

**Public endpoints** (no token needed):
- `GET /api/product`
- `/swagger-ui/**`, `/actuator/health`, `/actuator/prometheus`

**Protected endpoints** (Bearer token required):
- `POST /api/product`
- `POST /api/order`
- `GET /api/inventory`

## Circuit breaker config

Every downstream route is wrapped with Resilience4j:

| Setting | Value |
|---|---|
| Sliding window | 10 calls (count-based) |
| Failure rate threshold | 50% |
| Wait in open state | 5 seconds |
| Half-open calls | 3 |
| Timeout per call | 3 seconds |
| Retry attempts | 3 |

When a circuit opens, the gateway returns `503 Service Unavailable` with a friendly message instead of hanging or throwing a raw error.

## Configuration

| Property | Default | Docker override |
|---|---|---|
| `server.port` | `9000` | `9000` |
| `product.service.url` | `http://localhost:8080` | `http://product-service:8080` |
| `order.service.url` | `http://localhost:8081` | `http://order-service:8081` |
| `inventory.service.url` | `http://localhost:8082` | `http://inventory-service:8082` |
| `jwt.jwk-set-uri` | `http://localhost:8181/realms/...` | `http://keycloak:8080/realms/...` |

Activate the Docker profile by setting `SPRING_PROFILES_ACTIVE=docker`.

## Running locally

Prerequisites: Java 21, Maven, a running Keycloak instance on port 8181.

```bash
mvn clean package -DskipTests
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

The gateway starts on `http://localhost:9000`. Open `http://localhost:9000/swagger-ui.html` to see the aggregated API docs.

## Project structure

```
src/main/java/.../gateway/
├── routes/
│   └── Routes.java              # Route definitions + circuit breaker config
└── security/
    └── SecurityConfig.java      # JWT validation, CORS, public endpoint rules
```

## Tech stack

- Spring Boot 3.3.5
- Spring Cloud Gateway MVC
- Spring Security OAuth2 Resource Server
- Resilience4j (circuit breaker, retry, time limiter)
- SpringDoc OpenAPI 2.6 (Swagger aggregation)
- Micrometer + Prometheus
