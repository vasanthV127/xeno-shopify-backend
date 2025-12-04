# Xeno Shopify Insights - Backend

Multi-tenant Shopify Data Ingestion & Insights Service - Spring Boot Backend

## 🚀 Tech Stack

- **Spring Boot 3.2.0**
- **Spring Security** with JWT Authentication
- **PostgreSQL** with JPA/Hibernate
- **Spring WebFlux** (WebClient for Shopify API)
- **Spring Quartz** (Scheduled data sync)
- **Java 17**

## 📋 Features

✅ Multi-tenant architecture with UUID-based isolation  
✅ JWT authentication and authorization  
✅ Shopify Admin API integration  
✅ Scheduled data synchronization (every 6 hours)  
✅ REST API endpoints for dashboard analytics  
✅ Webhook support for real-time updates  
✅ Optimized database queries with custom JPA methods  

## 🗂️ Project Structure

```
backend/
├── src/main/java/com/xeno/
│   ├── config/
│   │   └── SecurityConfig.java              # Spring Security + JWT config
│   ├── controller/
│   │   ├── AuthController.java              # Signup, Login endpoints
│   │   ├── DashboardController.java         # Analytics endpoints
│   │   └── ShopifyController.java           # Sync & Webhook endpoints
│   ├── dto/
│   │   ├── AuthResponse.java                # JWT response
│   │   ├── DashboardStatsDTO.java           # Dashboard metrics
│   │   └── ...                              # Other DTOs
│   ├── model/
│   │   ├── Tenant.java                      # Shopify store entity
│   │   ├── Customer.java                    # Customer entity
│   │   ├── Product.java                     # Product entity
│   │   ├── Order.java                       # Order entity
│   │   └── OrderItem.java                   # Order line items
│   ├── repository/
│   │   ├── TenantRepository.java            # Tenant data access
│   │   ├── CustomerRepository.java          # Customer queries
│   │   └── ...                              # Other repositories
│   ├── security/
│   │   ├── JwtTokenProvider.java            # JWT generation/validation
│   │   ├── JwtAuthenticationFilter.java     # JWT request filter
│   │   └── JwtAuthenticationEntryPoint.java # 401 error handler
│   ├── service/
│   │   ├── AuthService.java                 # Authentication logic
│   │   ├── ShopifyService.java              # Shopify API integration
│   │   ├── DashboardService.java            # Analytics logic
│   │   └── SyncSchedulerService.java        # Scheduled sync job
│   └── ShopifyInsightsApplication.java      # Main application
├── src/main/resources/
│   ├── application.properties               # Development config
│   └── application-prod.properties          # Production config
├── Dockerfile                               # Docker build config
├── pom.xml                                  # Maven dependencies
└── README.md                                # This file
```

## 🛠️ Local Development Setup

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 15+

### Steps

1. **Clone the repository:**
```bash
git clone https://github.com/YOUR_USERNAME/xeno-shopify-backend.git
cd xeno-shopify-backend
```

2. **Create PostgreSQL database:**
```bash
psql -U postgres
CREATE DATABASE xeno_db;
\q
```

3. **Update `application.properties`:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/xeno_db
spring.datasource.username=postgres
spring.datasource.password=your_password
jwt.secret=your-secret-key-min-64-characters
```

4. **Run the application:**
```bash
mvn clean install
mvn spring-boot:run
```

5. **Verify it's running:**
```bash
curl http://localhost:8080/api/health
# Expected: {"status":"UP"}
```

## 🐳 Docker Setup

### Build and Run
```bash
docker build -t xeno-backend .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/xeno_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  xeno-backend
```

### With Docker Compose (Recommended)
See main project repository for `docker-compose.yml` with PostgreSQL included.

## 🔑 API Endpoints

### Authentication
- `POST /api/auth/signup` - Register new tenant
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/health` - Health check

### Dashboard (Requires JWT)
- `GET /api/dashboard/stats` - Get all metrics
- `GET /api/dashboard/top-customers?limit=5` - Get top customers
- `GET /api/dashboard/orders-by-date?startDate=...&endDate=...` - Get order stats

### Shopify Sync (Requires JWT)
- `POST /api/shopify/sync` - Trigger manual sync

### Webhooks
- `POST /api/webhooks/orders/create` - Order created
- `POST /api/webhooks/customers/create` - Customer created/updated
- `POST /api/webhooks/products/create` - Product created/updated

## 🔐 Environment Variables

**Development:**
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/xeno_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
JWT_SECRET=xenoShopifyInsightsSecretKeyForJWTToken2025MustBeLongEnough
JWT_EXPIRATION=86400000
```

**Production:**
```bash
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://your-host:5432/your-db
export DATABASE_USERNAME=your-user
export DATABASE_PASSWORD=your-password
export JWT_SECRET=generate-strong-random-secret-64-chars
```

## 📊 Database Schema

### Tables
- **tenants** - Shopify store credentials
- **customers** - Customer data from Shopify
- **products** - Product catalog from Shopify
- **orders** - Order data from Shopify
- **order_items** - Order line items

All tables include `tenant_id` for multi-tenant isolation.

## 🔄 Data Synchronization

**Automatic Sync:**
- Schedule: Every 6 hours
- Cron: `0 0 */6 * * ?`
- Configured in `application.properties`

**Manual Sync:**
- Endpoint: `POST /api/shopify/sync`
- Requires: JWT Bearer token

**What Gets Synced:**
- All customers
- All products
- Last 250 orders (configurable)

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Manual API Testing
```bash
# Health check
curl http://localhost:8080/api/health

# Signup
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "storeName": "Test Store",
    "shopifyDomain": "teststore.myshopify.com",
    "shopifyAccessToken": "shpat_abc123"
  }'

# Login and get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password123"}'

# Get dashboard stats (use token from login)
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/dashboard/stats
```

## 📦 Dependencies

Key dependencies in `pom.xml`:
- Spring Boot Starter Web
- Spring Boot Starter Security
- Spring Boot Starter Data JPA
- PostgreSQL Driver
- JWT (io.jsonwebtoken)
- Spring WebFlux (for WebClient)
- Spring Quartz (for scheduling)
- Lombok

## 🚢 Deployment

### Heroku
```bash
heroku create xeno-backend
heroku addons:create heroku-postgresql:mini
heroku config:set JWT_SECRET=your-secret
git push heroku main
```

### Render
1. Create PostgreSQL database on Render
2. Create Web Service
3. Build Command: `mvn clean install`
4. Start Command: `java -jar target/*.jar`
5. Add environment variables

## 📝 Configuration Files

**application.properties** - Development settings
```properties
server.port=8080
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
shopify.api.version=2024-01
shopify.sync.cron=0 0 */6 * * ?
```

**application-prod.properties** - Production settings
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

## 🐛 Troubleshooting

**Issue:** Database connection refused
```bash
# Check PostgreSQL is running
psql -U postgres -c "SELECT version();"

# Verify connection details in application.properties
```

**Issue:** JWT token invalid
```bash
# Ensure JWT_SECRET is at least 64 characters
# Check token expiration (default: 24 hours)
```

**Issue:** Shopify API errors
```bash
# Verify Shopify access token is valid
# Check API scopes: read_customers, read_orders, read_products
# Ensure domain format: store.myshopify.com (no https://)
```

## 📄 License

Created for Xeno FDE Internship Assignment - December 2025

## 👥 Author

Vasanth Kumar  
VIT-AP University

## 🔗 Related Repositories

- **Frontend:** [xeno-shopify-frontend](https://github.com/YOUR_USERNAME/xeno-shopify-frontend)
- **Full Documentation:** See main project README for complete architecture and setup

---

**Built with ☕ using Spring Boot**
