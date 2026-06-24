# ShopFlow — Distributed E-commerce Backend

ShopFlow is a backend platform for e-commerce built on a microservices architecture using Spring Boot 3 and Spring Cloud. Each service owns its database, communicates asynchronously over Kafka, and is secured behind a JWT-authenticated API Gateway. The whole stack runs locally via Docker Compose in a single command.

---

## Architecture

```
Client
  │
  ▼
API Gateway (port 9191)   ◄── JWT validation, role-based routing
  │
  ├── Identity Service  (9898)  — auth, users, roles, JWT issuance
  ├── Product Service   (8084)  — catalog, variants, inventory, images
  ├── Order Service     (8080)  — order lifecycle, PayPal, state machine
  └── Payment Service   (8085)  — payment records, transaction tracking

Async backbone: Apache Kafka
  ├── order-service    ──publishes──► product-service  (stock deduction)
  ├── order-service    ──publishes──► email-service    (order confirmation)
  └── order-service    ──publishes──► payment-service  (payment tracking)

Supporting infrastructure:
  Eureka (service discovery) | Redis (caching) | Zipkin (distributed tracing)
  MySQL per service          | Cloudinary (product images)
```

---

## Services

| Service | Port | Responsibility |
|---|---|---|
| `api-gateway` | 9191 | Single entry point; JWT filter, role-based access, load balancing |
| `service-registry` | 8761 | Eureka server — service discovery and health monitoring |
| `identity-service` | 9898 | Registration, login, JWT generation, RBAC (ADMIN / EMPLOYEE / CUSTOMER) |
| `product-service` | 8084 | Product CRUD, categories, variants, attributes, Redis cache, Cloudinary images |
| `order-service` | 8080 | Order creation, state-machine workflow, PayPal capture/refund, Feign clients |
| `payment-service` | 8085 | Payment status tracking, Kafka consumer |
| `email-service` | 8086 | Kafka consumer — sends HTML order confirmation emails via SMTP |
| `common-lib` | — | Shared DTOs and events used across services |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3, Spring Cloud 2023.0 |
| API Gateway | Spring Cloud Gateway (reactive) |
| Service Discovery | Netflix Eureka |
| Messaging | Apache Kafka |
| Inter-service HTTP | OpenFeign |
| Auth | Spring Security + JJWT 0.12 |
| Persistence | Spring Data JPA + MySQL 8 |
| Caching | Redis 7 |
| Tracing | Zipkin + Micrometer |
| Image Storage | Cloudinary |
| Payments | PayPal REST SDK |
| Containerization | Docker + Docker Compose |

---

## Prerequisites

- Docker Desktop (or Docker Engine + Docker Compose v2)
- Git
- Java 17 + Maven (only if building locally without Docker)

---

## Running the Stack

```bash
git clone <your-repo-url>
cd ecommerce-microservices-suite
docker-compose up -d
```

All services, databases, Kafka, Redis, and Zipkin start together. Give it ~60 seconds for Eureka registrations to stabilise.

**Verify everything is up:**

```bash
docker-compose ps
```

**Service dashboards:**

| Dashboard | URL |
|---|---|
| Eureka | http://localhost:8761 |
| Zipkin | http://localhost:9411 |
| API Gateway | http://localhost:9191 |

---

## API Usage

### Register a user

```bash
curl -X POST http://localhost:9191/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "alice",
    "email": "alice@example.com",
    "password": "secret123",
    "roles": ["CUSTOMER"]
  }'
```

### Login and get a JWT

```bash
curl -X POST http://localhost:9191/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "secret123"}'
```

Pass the returned token as a cookie `token=<jwt>` on subsequent requests.

### Create a product (EMPLOYEE role required)

```bash
curl -X POST http://localhost:9191/api/v1/products \
  -b "token=<your_jwt>" \
  -F "name=Running Shoes" \
  -F "price=79.99" \
  -F "stockQuantity=50" \
  -F "file=@/path/to/image.jpg"
```

### Place an order (CUSTOMER role required)

```bash
curl -X POST http://localhost:9191/api/v1/order \
  -H "Content-Type: application/json" \
  -b "token=<your_jwt>" \
  -d '{
    "orderItems": [{"productId": "<product-uuid>", "quantity": 2}],
    "paymentMethod": "PAYPAL"
  }'
```

---

## Database Access

Each service has its own MySQL instance accessible from the host:

| Database | Host port |
|---|---|
| `order_db` | 3307 |
| `identity_db` | 3308 |
| `payment_db` | 3309 |
| `product_db` | 3310 |

Connect with: `mysql -h 127.0.0.1 -P <port> -u root -proot`

Seed the roles table on first run:

```sql
INSERT INTO roles (name) VALUES ('ADMIN'), ('EMPLOYEE'), ('CUSTOMER');
```

---

## Key Design Decisions

**Database-per-service** — Each microservice owns its schema completely. No cross-service joins; consistency is achieved through Kafka events.

**State machine for orders** — The order lifecycle (NEW → PROCESSING → SHIPPING → DELIVERED) is implemented using the State design pattern, keeping transition logic isolated and testable.

**Optimistic locking** — Order and product entities use `@Version` to handle concurrent updates without database-level locks.

**Cache-aside with Redis** — Product and order data is cached on read and invalidated on write. Cache misses fall through to MySQL automatically.

**Event-driven stock management** — When an order is placed, a Kafka event triggers the product-service to decrement stock, keeping inventory consistent without synchronous coupling.

---

## Stopping the Stack

```bash
docker-compose down          # stop containers, keep volumes
docker-compose down -v       # stop and delete all data volumes
```

---

## Planned Improvements

- [ ] Resilience4j circuit breakers on Feign clients
- [ ] Flyway database migrations replacing JPA auto-DDL
- [ ] Prometheus + Grafana monitoring dashboards
- [ ] Kafka Dead Letter Queue with retry and poison-pill handling
- [ ] Review & Rating microservice
- [ ] Kubernetes manifests for production deployment
- [ ] Unit and integration test coverage

---

## License

MIT

