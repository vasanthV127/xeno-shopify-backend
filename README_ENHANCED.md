# Xeno Shopify Insights - Backend API

> **Multi-tenant Shopify Data Ingestion & Insights Service**  
> Enterprise-grade Spring Boot REST API with real-time webhooks, scheduled syncing, and comprehensive analytics.

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Deployment](https://img.shields.io/badge/Deployed%20on-Render-blueviolet)](https://render.com)

**🌐 Production URL:** https://xeno-shopify-backend-frzt.onrender.com  
**📚 Interactive API Docs:** https://xeno-shopify-backend-frzt.onrender.com/swagger-ui/index.html  
**✅ Status:** Live and operational (Singapore region)

---

## 📑 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Quick Start](#-quick-start)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Authentication](#-authentication)
- [Webhooks](#-webhooks)
- [Deployment](#-deployment)
- [Configuration](#-configuration)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)

---

## 🎯 Features

### Core Capabilities
✅ **Multi-tenant Architecture** - UUID-based tenant isolation, zero data leakage  
✅ **JWT Authentication** - Secure bearer token auth with BCrypt password hashing  
✅ **Shopify Integration** - Complete Admin REST API integration (customers, orders, products)  
✅ **Real-time Webhooks** - HMAC-verified webhooks for instant Shopify updates  
✅ **Scheduled Sync** - Quartz-powered 6-hour automatic data synchronization  
✅ **Customer Segmentation** - High/Medium/Low value customer analytics  
✅ **Interactive API Docs** - Swagger/OpenAPI 3.0 with try-it-now interface  
✅ **Optimized Queries** - Custom JPA repositories with pagination and filtering  
✅ **Production Ready** - Deployed on Render with PostgreSQL, full CORS, health checks

### API Endpoints
- **Authentication:** Signup, Login with JWT tokens
- **Dashboard:** Stats, Top customers, Revenue by date
- **Customers:** List with filters, Search, Segmentation stats
- **Shopify Sync:** Manual trigger, Automatic scheduling
- **Webhooks:** Orders, Customers, Products (real-time)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Shopify Admin API                            │
│                   (2024-10 REST API)                            │
└────────────────────────────┬────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
         Scheduled Sync                Webhooks
         (Every 6hrs)                  (Real-time)
                │                         │
                ▼                         ▼
┌─────────────────────────────────────────────────────────────────┐
│               Spring Boot Backend (Port 8080)                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Controllers (REST API)                                   │  │
│  │  • AuthController    • DashboardController               │  │
│  │  • CustomerController  • WebhookController               │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Security Layer                                           │  │
│  │  • JWT Filter • BCrypt • CORS • Multi-tenant Context     │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Service Layer                                            │  │
│  │  • ShopifyService (WebClient)  • DashboardService        │  │
│  │  • AuthService (JWT)  • SchedulerService (Quartz)        │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Data Access Layer (JPA Repositories)                    │  │
│  │  • Custom queries • Pagination • Tenant filtering        │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────┘
                                │ JDBC
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│         PostgreSQL 16 (Render - Singapore Region)              │
│  Tables: tenants, customers, products, orders, order_items     │
│  Indexes: Composite (tenant_id + FK), Email, Created_at        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Tech Stack

### Backend Framework
- **Spring Boot 3.2.0** - Core framework
- **Spring Security 6.2.0** - Authentication & authorization
- **Spring Data JPA** - ORM with Hibernate
- **Spring WebFlux** - WebClient for Shopify API calls
- **Spring Quartz 2.3.2** - Scheduled job execution

### Database & Persistence
- **PostgreSQL 16** - Production RDBMS
- **HikariCP** - Connection pooling
- **Flyway/Liquibase** - Schema migrations (optional)

### Security
- **JWT (io.jsonwebtoken 0.11.5)** - Token-based authentication
- **BCrypt** - Password hashing
- **HMAC-SHA256** - Webhook signature verification

### Documentation
- **Springdoc OpenAPI 2.3.0** - Swagger UI integration
- **OpenAPI 3.0** - API specification standard

### Build & Deployment
- **Maven 3.8+** - Dependency management
- **Docker** - Containerization
- **Render** - Cloud platform (PaaS)

### Development Tools
- **Lombok** - Reduce boilerplate code
- **Spring Boot DevTools** - Hot reload
- **JUnit 5** - Unit testing
- **Testcontainers** - Integration testing (optional)

---

## 🚀 Quick Start

### Prerequisites
```bash
# Required
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+

# Optional
- Docker (for containerized development)
- Postman/Insomnia (for API testing)
```

### 1. Clone Repository
```bash
git clone https://github.com/vasanthV127/xeno-shopify-backend.git
cd xeno-shopify-backend
```

### 2. Database Setup
```bash
# Start PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE xeno_db;

# Create user (optional)
CREATE USER xeno_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE xeno_db TO xeno_user;

\q
```

### 3. Configure Application
Create `src/main/resources/application-local.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/xeno_db
spring.datasource.username=postgres
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=your-secret-key-must-be-at-least-64-characters-long-for-hs512
jwt.expiration=86400000

# Shopify
shopify.api.version=2024-10
shopify.sync.cron=0 0 */6 * * ?

# Server
server.port=8080
```

### 4. Build & Run
```bash
# Install dependencies
mvn clean install

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Alternative: Run JAR
mvn package
java -jar target/shopify-insights-1.0.0.jar
```

### 5. Verify Installation
```bash
# Health check
curl http://localhost:8080/api/health
# Expected: "API is running"

# API documentation
open http://localhost:8080/swagger-ui/index.html
```

---

## 📚 API Documentation

### Interactive Documentation (Swagger UI)
Visit **http://localhost:8080/swagger-ui/index.html** for:
- Complete API reference
- Request/response schemas
- Try-it-now functionality
- Example payloads
- Authentication testing

### Authentication Endpoints

#### Register New Tenant
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "store@example.com",
  "password": "SecurePass123",
  "storeName": "My Awesome Store",
  "shopifyDomain": "mystore.myshopify.com",
  "shopifyAccessToken": "shpat_abcd1234..."
}

Response 200:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "store@example.com",
  "storeName": "My Awesome Store"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "store@example.com",
  "password": "SecurePass123"
}

Response 200:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "store@example.com",
  "storeName": "My Awesome Store"
}
```

### Dashboard Endpoints (Requires JWT)

#### Get Dashboard Statistics
```http
GET /api/dashboard/stats
Authorization: Bearer {your_jwt_token}

Response 200:
{
  "totalCustomers": 1247,
  "totalOrders": 3542,
  "totalProducts": 89,
  "totalRevenue": 45230.50,
  "averageOrderValue": 127.64,
  "ordersToday": 12,
  "revenueToday": 1532.40
}
```

#### Get Top Customers
```http
GET /api/dashboard/customers/top?limit=5
Authorization: Bearer {your_jwt_token}

Response 200:
[
  {
    "customerId": "123456789",
    "name": "John Doe",
    "email": "john@example.com",
    "totalSpent": 5420.50,
    "ordersCount": 34
  },
  ...
]
```

### Customer Endpoints

#### List Customers with Filters
```http
GET /api/customers?segment=high&search=john&page=0&size=20
Authorization: Bearer {your_jwt_token}

Query Parameters:
- segment: high (>$5000) | medium ($1000-$5000) | low (<$1000)
- search: name or email search term
- page: page number (0-indexed)
- size: items per page
- sortBy: field to sort by (default: totalSpent)
- sortDir: asc | desc (default: desc)

Response 200:
{
  "customers": [...],
  "currentPage": 0,
  "totalItems": 87,
  "totalPages": 5
}
```

#### Get Segment Statistics
```http
GET /api/customers/segments
Authorization: Bearer {your_jwt_token}

Response 200:
{
  "highValue": 23,
  "mediumValue": 145,
  "lowValue": 532,
  "total": 700
}
```

### Shopify Sync Endpoint

#### Trigger Manual Sync
```http
POST /api/shopify/sync
Authorization: Bearer {your_jwt_token}

Response 200:
{
  "message": "Sync completed successfully",
  "customersCount": 150,
  "ordersCount": 450,
  "productsCount": 89,
  "timestamp": "2025-12-04T10:30:00Z"
}
```

### Webhook Endpoints (No Auth - HMAC Verified)

#### Order Created/Updated
```http
POST /api/webhooks/shopify/orders/create
X-Shopify-Shop-Domain: mystore.myshopify.com
X-Shopify-Hmac-SHA256: {computed_hmac}
Content-Type: application/json

{Shopify order JSON payload}

Response 200:
{
  "status": "success",
  "message": "Order processed"
}
```

---

## 🗄️ Database Schema

### Entity Relationship Diagram
```
┌──────────────┐
│   tenants    │
├──────────────┤
│ id (PK)      │
│ tenant_id    │◄────────┐
│ email (UQ)   │         │
│ password_hash│         │
│ store_name   │         │
│ shopify_domain│        │
│ shopify_access_token   │
│ created_at   │         │
│ updated_at   │         │
└──────────────┘         │
                         │
        ┌────────────────┼────────────────┐
        │                │                │
┌───────▼──────┐  ┌──────▼──────┐  ┌────▼──────┐
│  customers   │  │   orders    │  │ products  │
├──────────────┤  ├─────────────┤  ├───────────┤
│ id (PK)      │  │ id (PK)     │  │ id (PK)   │
│ tenant_id (FK)  │ tenant_id(FK)  │tenant_id │
│ shopify_customer_id│customer_id│shopify_product_id
│ email        │  │ shopify_order_id│title   │
│ first_name   │  │ total_price │  │ price     │
│ last_name    │  │ status      │  │ vendor    │
│ orders_count │  │ created_at  │  │ status    │
│ total_spent  │  └─────┬───────┘  └───────────┘
│ created_at   │        │
└──────────────┘        │
                        │
                  ┌─────▼──────┐
                  │order_items │
                  ├────────────┤
                  │ id (PK)    │
                  │ order_id(FK)
                  │ product_id(FK)
                  │ quantity   │
                  │ price      │
                  └────────────┘
```

### Key Indexes
- `idx_tenant_customer` (tenant_id, shopify_customer_id) - Fast customer lookup
- `idx_tenant_order` (tenant_id, shopify_order_id) - Fast order lookup
- `idx_tenant_product` (tenant_id, shopify_product_id) - Fast product lookup
- `idx_email` (email) - Fast authentication lookup
- `idx_created_at` (created_at) - Date range queries

### Sample Data
See [WEBHOOK_SETUP.md](./WEBHOOK_SETUP.md) for sample SQL inserts.

---

## 🔐 Authentication

### JWT Token Flow
1. **User registers** via `/api/auth/signup`
2. **Password hashed** with BCrypt (10 rounds)
3. **JWT token generated** with HS512 algorithm
4. **Token contains:** tenant_id, email, store_name
5. **Token expires** after 24 hours
6. **Client stores** token in localStorage
7. **Subsequent requests** include `Authorization: Bearer {token}`

### Security Features
- ✅ BCrypt password hashing (strength: 10)
- ✅ JWT with HS512 (512-bit secret key required)
- ✅ Multi-tenant isolation (UUID tenant_id)
- ✅ CORS configuration (Vercel + localhost)
- ✅ HMAC-SHA256 webhook verification
- ✅ SQL injection prevention (JPA parameterized queries)
- ✅ Session stateless (no server-side sessions)

### Obtaining Shopify Credentials
1. Log in to Shopify Admin
2. **Apps** → **Develop apps** → **Create an app**
3. Configure **Admin API scopes:**
   - `read_customers`
   - `read_orders`
   - `read_products`
4. **Install app** and copy **Access Token**
5. Use `{yourstore}.myshopify.com` as domain

---

## 🪝 Webhooks

### Setup Instructions
See [WEBHOOK_SETUP.md](./WEBHOOK_SETUP.md) for complete guide.

### Supported Events
- **orders/create** - New order placed
- **orders/update** - Order status changed
- **customers/create** - New customer registered
- **customers/update** - Customer info updated
- **products/create** - New product added
- **products/update** - Product details changed

### Security
All webhooks verified using **HMAC-SHA256** signature:
```
HMAC = Base64(HMAC-SHA256(payload, shopify_access_token))
```

Invalid signatures return `401 Unauthorized`.

---

## 🚢 Deployment

### Render (Current Production)

#### Prerequisites
- Render account
- GitHub repository
- Render PostgreSQL database

#### Steps
1. **Create PostgreSQL Database:**
   - Dashboard → **New** → **PostgreSQL**
   - Plan: Free (max 1GB)
   - Region: Singapore (same as web service)
   - Copy **Internal Database URL**

2. **Create Web Service:**
   - Dashboard → **New** → **Web Service**
   - Connect GitHub repository
   - Build Command: `mvn clean install`
   - Start Command: `java -jar target/*.jar`
   - Region: Singapore
   - Environment: Add variables (see Configuration)

3. **Configure Environment:**
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-xxx:5432/xeno_db
SPRING_DATASOURCE_USERNAME=xeno_user
SPRING_DATASOURCE_PASSWORD=xxx
JWT_SECRET=your-64-char-secret
JWT_EXPIRATION=86400000
```

4. **Deploy:**
   - Git push → Automatic build → Deploy
   - Health check: `/api/health`

### Docker Deployment

#### Build Image
```bash
docker build -t xeno-backend:latest .
```

#### Run Container
```bash
docker run -d \
  --name xeno-backend \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/xeno_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e JWT_SECRET=your-secret \
  xeno-backend:latest
```

#### Docker Compose
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: xeno_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/xeno_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      JWT_SECRET: xenoShopifyInsightsSecretKeyForJWTToken2025MustBeLongEnough
    depends_on:
      - postgres

volumes:
  postgres_data:
```

---

## ⚙️ Configuration

### Application Properties

#### Development (`application.properties`)
```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/xeno_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=xenoShopifyInsightsSecretKeyForJWTToken2025MustBeLongEnough
jwt.expiration=86400000

# Shopify
shopify.api.version=2024-10
shopify.sync.cron=0 0 */6 * * ?

# Logging
logging.level.com.xeno=DEBUG
logging.level.org.springframework.web=INFO
```

#### Production (`application-prod.properties`)
```properties
# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Logging
logging.level.com.xeno=INFO
logging.level.org.springframework.web=WARN
```

### Environment Variables (Production)
```bash
# Database (from Render PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-xxx-a:5432/xeno_db_xxx
SPRING_DATASOURCE_USERNAME=xeno_user
SPRING_DATASOURCE_PASSWORD=generated_password

# JWT (generate strong 64+ char secret)
JWT_SECRET=use-random-generator-for-production-secret-key
JWT_EXPIRATION=86400000

# Profile
SPRING_PROFILES_ACTIVE=prod

# Optional
JAVA_OPTS=-Xmx512m -Xms256m
```

---

## 🐛 Troubleshooting

### Database Connection Issues

**Problem:** `Connection refused` or `Timeout`
```bash
# Check PostgreSQL is running
psql -U postgres -c "SELECT version();"

# Verify connection string format
jdbc:postgresql://host:port/database

# Test connection
psql -h localhost -p 5432 -U postgres -d xeno_db
```

**Solution:**
- Ensure PostgreSQL service is running
- Check firewall rules (port 5432)
- Verify credentials in application.properties
- Use internal database URL on Render (dpg-xxx-a)

### JWT Token Errors

**Problem:** `Invalid JWT token` or `401 Unauthorized`
```bash
# Check token expiration
# Tokens expire after 24 hours by default

# Verify JWT secret length
# Must be at least 64 characters for HS512
```

**Solution:**
- Re-login to get fresh token
- Ensure JWT_SECRET environment variable is set
- Check token format: `Bearer eyJhbGciOiJIU...`
- Verify clock sync between client and server

### Shopify API Errors

**Problem:** `403 Forbidden` or `Unauthorized`
```bash
# Common causes:
# 1. Invalid access token
# 2. Missing API scopes
# 3. Incorrect shop domain format
```

**Solution:**
- Regenerate Shopify access token
- Add required scopes: `read_customers`, `read_orders`, `read_products`
- Domain format: `yourstore.myshopify.com` (no `https://`)
- Test token: `curl -H "X-Shopify-Access-Token: {token}" https://{shop}/admin/api/2024-10/customers.json`

### Build Errors

**Problem:** Maven build fails
```bash
[ERROR] Failed to execute goal...
```

**Solution:**
```bash
# Clean Maven cache
mvn clean

# Update dependencies
mvn dependency:purge-local-repository

# Skip tests during build
mvn clean install -DskipTests

# Check Java version
java -version
# Should be 17 or higher
```

### Runtime Errors

**Problem:** `OutOfMemoryError` or slow performance
```bash
# Increase heap size
export JAVA_OPTS="-Xmx1024m -Xms512m"

# For Render, set in Environment Variables
JAVA_OPTS=-Xmx1024m -Xms512m
```

**Problem:** Scheduled sync not running
```bash
# Check cron expression in application.properties
shopify.sync.cron=0 0 */6 * * ?
# Format: second minute hour day month weekday

# Verify SchedulerService is enabled
# Check logs for "Sync job started"
```

---

## 📈 Performance Optimization

### Database Optimization
- ✅ Connection pooling with HikariCP (default: 10 connections)
- ✅ Composite indexes on tenant_id + foreign keys
- ✅ Query optimization with JPA Specifications
- ✅ Lazy loading for entity relationships
- ✅ Pagination for large result sets

### API Optimization
- ✅ Stateless REST (no session storage)
- ✅ JWT in header (avoid repeated DB lookups)
- ✅ Scheduled sync instead of real-time polling
- ✅ WebClient for non-blocking Shopify API calls
- ✅ Async webhook processing (future enhancement)

### Monitoring & Logging
```properties
# Enable Spring Boot Actuator (optional)
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Structured logging
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

---

## 🧪 Testing

### Unit Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Generate coverage report
mvn test jacoco:report
```

### Integration Tests
```bash
# Use Testcontainers for PostgreSQL
mvn verify -P integration-tests
```

### Manual API Testing with cURL

#### Health Check
```bash
curl http://localhost:8080/api/health
```

#### Signup
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "storeName": "Test Store",
    "shopifyDomain": "teststore.myshopify.com",
    "shopifyAccessToken": "shpat_test123"
  }'
```

#### Login and Save Token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}' \
  | jq -r '.token')

echo $TOKEN
```

#### Get Dashboard Stats
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/dashboard/stats | jq
```

---

## 📖 Related Documentation

- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - Complete system architecture diagrams and data flows
- **[WEBHOOK_SETUP.md](./WEBHOOK_SETUP.md)** - Shopify webhook configuration guide
- **[Swagger UI](https://xeno-shopify-backend-frzt.onrender.com/swagger-ui/index.html)** - Interactive API documentation

---

## 🤝 Contributing

This project was created for the **Xeno FDE Internship Assignment (December 2025)**.

### Code Style
- Follow Spring Boot best practices
- Use Lombok for boilerplate reduction
- Write meaningful commit messages
- Add Javadoc for public methods

### Pull Request Process
1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Author

**Vasanth Kumar**  
VIT-AP University  
Email: vasanthkumar.v2024@gmail.com  
GitHub: [@vasanthV127](https://github.com/vasanthV127)

---

## 🌟 Acknowledgments

- **Spring Team** - For the amazing framework
- **Shopify** - For comprehensive API documentation
- **Xeno** - For the internship opportunity
- **PostgreSQL Community** - For the reliable database
- **Render** - For free-tier hosting

---

## 🔗 Quick Links

| Resource | URL |
|----------|-----|
| **Production API** | https://xeno-shopify-backend-frzt.onrender.com |
| **API Docs** | https://xeno-shopify-backend-frzt.onrender.com/swagger-ui/index.html |
| **Frontend** | https://xeno-shopify-frontend-five.vercel.app |
| **GitHub Backend** | https://github.com/vasanthV127/xeno-shopify-backend |
| **GitHub Frontend** | https://github.com/vasanthV127/xeno-shopify-frontend |

---

**⭐ If you find this project useful, please consider giving it a star on GitHub!**

**Built with ☕ and ❤️ using Spring Boot**
