# Order Service

Handles the purchase flow. When a customer clicks "Order Now", this service validates the request, checks inventory availability, persists the order to MySQL, and publishes an event to Kafka so downstream services (like notifications) can react.

Built with **Spring Boot 3**, **Spring Data JPA** (MySQL), **Resilience4j** for fault tolerance, and **Apache Kafka** with **Avro** serialization for event-driven communication.

## What it does

- Accepts order placement requests via `POST /api/order`
- Validates that the user provided an email, first name, and last name
- Calls the Inventory Service to verify stock availability (with circuit breaker + retry)
- Persists the order in MySQL (`t_orders` table, managed by Flyway)
- Publishes an `OrderPlacedEvent` to the `order-placed` Kafka topic
- Returns a success message or a structured error response

## Order placement flow

<img width="550" alt="mermaid-diagram-2026-09-20-001335" src="https://github.com/user-attachments/assets/c91ae5db-0d32-4660-bf71-a9b6deb3e7b3" />


The inventory call is wrapped in a Resilience4j circuit breaker. If the inventory service is down, the circuit opens after 5 failures and the fallback returns `false` (order is rejected gracefully instead of hanging).

## API

### `POST /api/order`

Places a new order. Requires a valid JWT token (passed through the API Gateway).

**Request body**
```json
{
  "skuCode": "iphone_15",
  "price": 999.99,
  "quantity": 1,
  "userDetails": {
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

**Success response** `201 Created`
```
Order Placed Successfully
```

**Error response** `500 Internal Server Error` (RFC 7807 ProblemDetail)
```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Product with SKU code xyz is not in stock",
  "instance": "/api/order"
}
```

## Kafka event

When an order is placed successfully, the service publishes an `OrderPlacedEvent` to the `order-placed` topic. The event is serialized with **Apache Avro** via **Confluent Schema Registry**.

**Event schema** (Avro):

| Field | Type | Description |
|---|---|---|
| `orderNumber` | string | UUID generated for this order |
| `email` | string | Customer email |
| `firstName` | string | Customer first name |
| `lastName` | string | Customer last name |

The notification service consumes this event and sends a confirmation email.

## Resilience4j config

The inventory client call is protected with:

| Setting | Value |
|---|---|
| Sliding window | 5 calls (count-based) |
| Failure rate threshold | 50% |
| Wait in open state | 5 seconds |
| Half-open calls | 3 |
| Timeout | 3 seconds |
| Retry attempts | 3 (wait 5s between) |

The `InventoryClient` interface uses Spring's declarative HTTP client (`@GetExchange`) with `@CircuitBreaker` and `@Retry` annotations directly on the method.

## Database schema

Table: `t_orders` (managed by Flyway)

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT (PK, auto-increment) | Row identifier |
| `order_number` | VARCHAR(255) | UUID for this order |
| `sku_code` | VARCHAR(255) | Product SKU that was ordered |
| `price` | DECIMAL(19,2) | Order price |
| `quantity` | INT | Units ordered |

## Running locally

Prerequisites: Java 21, Maven, MySQL on port 3307, Kafka on port 9092, Schema Registry on port 8085.

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS order_service;"

mvn clean package -DskipTests
java -jar target/order-service-0.0.1-SNAPSHOT.jar
```

The service starts on `http://localhost:8081`.

## Configuration

| Property | Default | Docker override |
|---|---|---|
| `server.port` | `8081` | `8081` |
| `spring.datasource.url` | `jdbc:mysql://localhost:3307/order_service` | `jdbc:mysql://mysql-order:3306/order_service` |
| `spring.kafka.bootstrap-servers` | `localhost:9092` | `broker:29092` |
| `schema.registry.url` | `http://127.0.0.1:8085` | `http://schema-registry:8085` |
| `inventory.url` | `http://localhost:8082` | `http://inventory-service:8082` |

## Project structure

```
src/main/java/.../order/
├── client/
│   └── InventoryClient.java       # Declarative HTTP client + circuit breaker
├── controller/
│   └── OrderController.java       # POST /api/order endpoint
├── dto/
│   └── OrderRequest.java          # Inbound DTO with nested UserDetails
├── event/
│   └── OrderPlacedEvent.java      # Avro-generated event class
├── exception/
│   └── GlobalExceptionHandler.java # RFC 7807 error responses
├── model/
│   └── Order.java                 # JPA entity for t_orders
├── repository/
│   └── OrderRepository.java       # Spring Data JPA repository
└── service/
    └── OrderService.java          # Core business logic

src/main/resources/
├── application.properties
├── application-docker.properties
└── db/migration/
    └── V1__init.sql               # Create t_orders table
```

## Tech stack

- Spring Boot 3.3.5
- Spring Data JPA + Hibernate
- MySQL 8.3
- Flyway (schema migrations)
- Apache Kafka (producer)
- Apache Avro + Confluent Schema Registry
- Resilience4j (circuit breaker, retry, time limiter)
- Spring HTTP Interface (declarative client)
- Micrometer + Prometheus
- Lombok
