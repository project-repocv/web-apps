# Banking Platform Microservices Architecture

A comprehensive microservices-based banking platform built with Spring Boot 3.2.x, Spring Cloud, and modern cloud-native technologies.

## Architecture Overview

The system consists of 8 core microservices:

1. **Eureka Server** (Port 8761) - Service Discovery
2. **Customer Service** (Port 8081) - Customer Profile Management
3. **Account Service** (Port 8083) - Account Management
4. **Transaction Service** (Port 8084) - Financial Operations with SAGA Pattern
5. **Auth Service** (Port 8082) - Authentication & Authorization
6. **Notification Service** (Port 8085) - Alerts & Notifications via RabbitMQ
7. **API Gateway** (Port 8080) - Request Routing & Security
8. **Config Server** - Centralized Configuration

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.2.x, Spring Cloud
- **Database**: PostgreSQL (separate instances per service)
- **Message Broker**: RabbitMQ
- **Cache**: Redis
- **Service Discovery**: Eureka
- **API Gateway**: Spring Cloud Gateway
- **Monitoring**: Prometheus, Grafana
- **Containerization**: Docker, Docker Compose

## Features

### Security & Compliance
- JWT-based authentication and authorization
- Role-based access control (CUSTOMER, ADMIN, SUPPORT)
- PII encryption at rest (AES-256)
- Secure password hashing (BCrypt)
- Input validation (JSR-380)

### Financial Operations
- SAGA pattern for distributed transactions
- Idempotency keys for safe retries
- Pessimistic locking on account balances
- Audit logging for all financial operations
- Compensation logic for failed operations

### Reliability & Performance
- Circuit breakers (Resilience4j)
- Rate limiting
- Request/response logging
- Health checks and monitoring
- Retry mechanisms with exponential backoff

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Maven 3.8+

### Running the Application

1. Clone the repository
2. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

3. Start the entire stack:
   ```bash
   docker-compose up --build
   ```

### Access Points

- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Grafana**: http://localhost:3000 (admin/admin)
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Prometheus**: http://localhost:9090

### API Endpoints

#### Customer Service
- `POST /api/customers` - Create customer
- `GET /api/customers/{id}` - Get customer by ID
- `PUT /api/customers/{id}` - Update customer
- `GET /api/customers/email/{email}` - Get customer by email

#### Account Service
- `POST /api/accounts` - Create account
- `GET /api/accounts/{id}` - Get account by ID
- `PUT /api/accounts/{id}/status` - Update account status
- `GET /api/accounts/customer/{customerId}` - Get accounts by customer

#### Transaction Service
- `POST /api/transactions/deposit` - Deposit funds
- `POST /api/transactions/withdrawal` - Withdraw funds
- `POST /api/transactions/transfer` - Transfer funds
- `GET /api/transactions/account/{accountId}` - Get transactions by account

#### Auth Service
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh-token` - Refresh token
- `POST /api/auth/logout` - User logout
- `GET /api/auth/me` - Get current user

#### Notification Service
- `POST /api/notifications` - Send notification
- `POST /api/notifications/preferences` - Save preferences
- `GET /api/notifications/preferences/{customerId}` - Get preferences

## Testing Instructions

### 1. Register a User and Login

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "customerId": "TEST001",
    "email": "test@example.com"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

Save the `accessToken` from the response for subsequent requests.

### 2. Create a Customer

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com",
    "phone": "+1234567890",
    "address": "123 Main St, Anytown, USA",
    "dateOfBirth": "1990-01-01",
    "kycStatus": "VERIFIED"
  }'
```

### 3. Create an Account

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "customerId": "TEST001",
    "accountType": "SAVINGS",
    "initialBalance": 1000.00,
    "currency": "USD"
  }'
```

### 4. Make a Deposit

```bash
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "accountId": "ACCOUNT_ID_FROM_ABOVE",
    "amount": 500.00,
    "description": "Initial deposit",
    "idempotencyKey": "unique-key-123"
  }'
```

## Monitoring

The system includes comprehensive monitoring:

- **Metrics**: Collected via Micrometer and exported to Prometheus
- **Health Checks**: Available for all services at `/actuator/health`
- **Logging**: Structured JSON logging with correlation IDs

## Production Considerations

- Use strong passwords and secure configurations
- Enable SSL/TLS for all communications
- Implement proper backup strategies
- Set up alerting for critical metrics
- Regular security audits and penetration testing
- Compliance with financial regulations (PCI DSS, SOX, etc.)

## Troubleshooting

### Services not starting
Check logs: `docker-compose logs <service-name>`

### Database connection issues
Ensure PostgreSQL containers are running: `docker-compose ps`

### Message queue issues
Check RabbitMQ management UI: http://localhost:15672

## License

This project is licensed under the MIT License.
