# Product Service

Manages the product catalog for the e-commerce platform. This is the source of truth for everything a customer sees when browsing the shop: names, descriptions, prices, and SKU codes.

Built with **Spring Boot 3** and backed by **MongoDB**, it stores product documents in a flexible schema that makes it straightforward to extend with categories, images, or tags later without running schema migrations.

## What it does

- Stores and retrieves product catalog items (name, description, skuCode, price)
- Serves the public `GET /api/product` endpoint that the frontend calls to render the shop
- Accepts `POST /api/product` for authenticated users to add new items
- Exposes OpenAPI docs at `/swagger-ui.html`

## How it fits in the system

```mermaid
flowchart LR
    FE["Angular Frontend"]
    GW["API Gateway"]
    PS["Product Service"]
    DB[("MongoDB")]

    FE -- "browse catalog" --> GW
    FE -- "add product" --> GW
    GW --> PS
    PS -- "read/write" --> DB

    style FE fill:#e3f2fd,stroke:#1565c0
    style GW fill:#fff3e0,stroke:#e65100
    style PS fill:#e8f5e9,stroke:#2e7d32
    style DB fill:#fce4ec,stroke:#b71c1c
```

The Product Service doesn't know about inventory or orders. It's purely a catalog. When someone adds a product here, the inventory service will auto-provision stock the first time that SKU is ordered.

## API

### `GET /api/product`

Returns all products. No authentication required.

**Response** `200 OK`
```json
[
  {
    "id": "66a1b2c3d4e5f6a7b8c9d0e1",
    "name": "iPhone 15",
    "description": "Apple iPhone 15 128GB",
    "skuCode": "iphone_15",
    "price": 999.99
  }
]
```

### `POST /api/product`

Adds a new product to the catalog. Requires a valid JWT token (passed via the API Gateway).

**Request body**
```json
{
  "name": "OnePlus 12",
  "description": "OnePlus 12 256GB Flowy Emerald",
  "skuCode": "oneplus_12",
  "price": 799.99
}
```

**Response** `201 Created` — returns the saved product with its generated `id`.

## Data model

MongoDB collection: `product`

| Field | Type | Description |
|---|---|---|
| `id` | String | Auto-generated ObjectId |
| `name` | String | Product display name |
| `description` | String | Product description |
| `skuCode` | String | Unique identifier used across services |
| `price` | BigDecimal | Price in default currency |

The `skuCode` is what links this service to the rest of the system. When someone places an order, the order service uses this code to check inventory.

## Running locally

Prerequisites: Java 21, Maven, MongoDB on port 27017.

```bash
mvn clean package -DskipTests
java -jar target/ProductServiceApp_MS-0.0.1-SNAPSHOT.jar
```

The service starts on `http://localhost:8080`.

Default MongoDB connection: `mongodb://root:rootpassword@localhost:27017/product-service?authSource=admin`

## Configuration

| Property | Default | Docker override |
|---|---|---|
| `server.port` | `8080` | `8080` |
| `spring.data.mongodb.uri` | `mongodb://root:rootpassword@localhost:27017/...` | `mongodb://root:rootpassword@mongodb:27017/...` |

## Project structure

```
src/main/java/.../product/
├── controller/
│   └── ProductController.java      # REST endpoints (GET, POST)
├── dto/
│   ├── ProductRequest.java         # Inbound DTO (Java record)
│   └── ProductResponse.java        # Outbound DTO (Java record)
├── exception/
│   └── GlobalExceptionHandler.java # Centralized error handling (RFC 7807)
├── model/
│   └── Product.java                # MongoDB document entity
├── repository/
│   └── ProductRepository.java      # Spring Data MongoDB repository
├── service/
│   └── ProductService.java         # Business logic
└── config/
    └── OpenApiConfig.java          # Swagger/OpenAPI metadata
```

## Tech stack

- Spring Boot 3.3.5
- Spring Data MongoDB
- SpringDoc OpenAPI 2.6
- Micrometer + Prometheus
- Lombok
