# Xeno Shopify Data Ingestion & Insights - System Architecture

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                     React Frontend (Vercel)                         │     │
│  │  https://xeno-shopify-frontend-five.vercel.app                     │     │
│  │                                                                      │     │
│  │  • Dashboard (Charts, Stats, Top Customers)                        │     │
│  │  • Customer Segmentation (Filters, Search, Pagination)             │     │
│  │  • Authentication (Login/Signup)                                    │     │
│  │  • Dark Theme UI with Glassmorphism                                │     │
│  │  • React Router, Axios, Chart.js, Tailwind CSS                     │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                    │                                          │
│                                    │ HTTPS/REST API                           │
│                                    ▼                                          │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           APPLICATION LAYER                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │              Spring Boot Backend (Render Singapore)                 │     │
│  │         https://xeno-shopify-backend-frzt.onrender.com             │     │
│  │                                                                      │     │
│  │  ┌──────────────────────────────────────────────────────────────┐  │     │
│  │  │                    Security Layer                             │  │     │
│  │  │  • JWT Authentication (Bearer Token)                         │  │     │
│  │  │  • BCrypt Password Hashing                                   │  │     │
│  │  │  • CORS Configuration (Vercel + Localhost)                   │  │     │
│  │  │  • Multi-tenant Isolation (UUID tenant_id)                   │  │     │
│  │  └──────────────────────────────────────────────────────────────┘  │     │
│  │                              │                                       │     │
│  │  ┌──────────────────────────────────────────────────────────────┐  │     │
│  │  │                   REST Controllers                            │  │     │
│  │  │                                                               │  │     │
│  │  │  • AuthController      → /api/auth/**                        │  │     │
│  │  │    - POST /signup      (Create tenant account)               │  │     │
│  │  │    - POST /login       (JWT token generation)                │  │     │
│  │  │                                                               │  │     │
│  │  │  • DashboardController → /api/dashboard/**                   │  │     │
│  │  │    - GET /stats        (Total customers, orders, revenue)    │  │     │
│  │  │    - GET /customers/top (Top 5 customers by spend)          │  │     │
│  │  │    - GET /orders/stats  (Order stats by date range)         │  │     │
│  │  │                                                               │  │     │
│  │  │  • CustomerController  → /api/customers/**                   │  │     │
│  │  │    - GET /             (List with filters & pagination)      │  │     │
│  │  │    - GET /segments     (High/Medium/Low value counts)        │  │     │
│  │  │    - GET /{id}         (Single customer details)             │  │     │
│  │  │                                                               │  │     │
│  │  │  • ShopifyController   → /api/shopify/**                     │  │     │
│  │  │    - POST /sync        (Manual data sync from Shopify)       │  │     │
│  │  │                                                               │  │     │
│  │  │  • WebhookController   → /api/webhooks/shopify/**            │  │     │
│  │  │    - POST /orders/create    (Real-time order updates)        │  │     │
│  │  │    - POST /customers/create (Real-time customer updates)     │  │     │
│  │  │    - POST /products/create  (Real-time product updates)      │  │     │
│  │  │    - HMAC-SHA256 signature verification                      │  │     │
│  │  └──────────────────────────────────────────────────────────────┘  │     │
│  │                              │                                       │     │
│  │  ┌──────────────────────────────────────────────────────────────┐  │     │
│  │  │                    Service Layer                              │  │     │
│  │  │                                                               │  │     │
│  │  │  • ShopifyService                                            │  │     │
│  │  │    - WebClient to Shopify Admin API                          │  │     │
│  │  │    - Fetch customers, orders, products                       │  │     │
│  │  │    - Pagination handling (250 items/page)                    │  │     │
│  │  │    - Rate limiting compliance                                │  │     │
│  │  │                                                               │  │     │
│  │  │  • DashboardService                                          │  │     │
│  │  │    - Aggregate statistics calculation                        │  │     │
│  │  │    - Top customers by spend                                  │  │     │
│  │  │    - Order stats by date range                               │  │     │
│  │  │                                                               │  │     │
│  │  │  • SchedulerService (Quartz)                                 │  │     │
│  │  │    - Automatic sync every 6 hours                            │  │     │
│  │  │    - Cron: 0 0 */6 * * ?                                     │  │     │
│  │  └──────────────────────────────────────────────────────────────┘  │     │
│  │                              │                                       │     │
│  │  ┌──────────────────────────────────────────────────────────────┐  │     │
│  │  │                 Data Access Layer (JPA)                       │  │     │
│  │  │                                                               │  │     │
│  │  │  Repositories:                                                │  │     │
│  │  │  • TenantRepository                                          │  │     │
│  │  │  • CustomerRepository (with segmentation queries)            │  │     │
│  │  │  • OrderRepository (with date range stats)                   │  │     │
│  │  │  • ProductRepository                                         │  │     │
│  │  │  • OrderItemRepository                                       │  │     │
│  │  └──────────────────────────────────────────────────────────────┘  │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                    │                                          │
│                                    │ JDBC                                     │
│                                    ▼                                          │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           DATABASE LAYER                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │              PostgreSQL Database (Render Singapore)                 │     │
│  │          dpg-d4omq1e3jp1c73dl8op0-a:5432/xeno_db_c7dz             │     │
│  │                                                                      │     │
│  │  Multi-Tenant Data Model:                                          │     │
│  │                                                                      │     │
│  │  ┌──────────┐                                                       │     │
│  │  │ tenants  │                                                       │     │
│  │  │──────────│                                                       │     │
│  │  │ id (PK)  │                                                       │     │
│  │  │ tenant_id (UUID)                                                 │     │
│  │  │ email                                                            │     │
│  │  │ password_hash (BCrypt)                                          │     │
│  │  │ store_name                                                       │     │
│  │  │ shopify_domain                                                   │     │
│  │  │ shopify_access_token                                            │     │
│  │  └──────────┘                                                       │     │
│  │       │                                                              │     │
│  │       │ One-to-Many                                                 │     │
│  │       ▼                                                              │     │
│  │  ┌──────────┐      ┌──────────┐      ┌────────────┐               │     │
│  │  │customers │      │  orders  │      │  products  │               │     │
│  │  │──────────│      │──────────│      │────────────│               │     │
│  │  │ id (PK)  │◄─────┤ id (PK)  │      │ id (PK)    │               │     │
│  │  │tenant_id │      │tenant_id │      │ tenant_id  │               │     │
│  │  │shopify_  │      │customer_id      │ shopify_   │               │     │
│  │  │customer_id      │shopify_  │      │ product_id │               │     │
│  │  │email     │      │order_id  │      │ title      │               │     │
│  │  │first_name│      │total_price      │ price      │               │     │
│  │  │last_name │      │status    │      │ vendor     │               │     │
│  │  │orders_count     │created_at│      │ status     │               │     │
│  │  │total_spent      └──────────┘      └────────────┘               │     │
│  │  └──────────┘            │                                          │     │
│  │                          │                                          │     │
│  │                          │ One-to-Many                              │     │
│  │                          ▼                                          │     │
│  │                    ┌──────────────┐                                │     │
│  │                    │ order_items  │                                │     │
│  │                    │──────────────│                                │     │
│  │                    │ id (PK)      │                                │     │
│  │                    │ order_id     │                                │     │
│  │                    │ product_id   │                                │     │
│  │                    │ quantity     │                                │     │
│  │                    │ price        │                                │     │
│  │                    └──────────────┘                                │     │
│  │                                                                      │     │
│  │  Indexes:                                                           │     │
│  │  • idx_tenant_customer (tenant_id, shopify_customer_id)            │     │
│  │  • idx_tenant_order (tenant_id, shopify_order_id)                  │     │
│  │  • idx_tenant_product (tenant_id, shopify_product_id)              │     │
│  │  • idx_email (email) for fast authentication lookup                │     │
│  │                                                                      │     │
│  └────────────────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                        EXTERNAL SERVICES LAYER                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                      Shopify Admin API                              │     │
│  │            https://{shop-domain}/admin/api/2024-10                 │     │
│  │                                                                      │     │
│  │  REST Endpoints Used:                                              │     │
│  │  • GET  /customers.json      (Fetch all customers)                 │     │
│  │  • GET  /orders.json         (Fetch all orders)                    │     │
│  │  • GET  /products.json       (Fetch all products)                  │     │
│  │                                                                      │     │
│  │  Authentication:                                                    │     │
│  │  • Header: X-Shopify-Access-Token                                  │     │
│  │                                                                      │     │
│  │  Webhooks (Push to Backend):                                       │     │
│  │  • orders/create         → POST /api/webhooks/shopify/orders/create│     │
│  │  • customers/create      → POST /api/webhooks/shopify/customers/..│     │
│  │  • products/create       → POST /api/webhooks/shopify/products/.. │     │
│  │  • Header: X-Shopify-Hmac-SHA256 (signature verification)         │     │
│  └────────────────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagrams

### 1. User Authentication Flow

```
┌────────┐                  ┌─────────────┐                  ┌──────────┐
│ User   │                  │   Backend   │                  │ Database │
└───┬────┘                  └──────┬──────┘                  └────┬─────┘
    │                              │                              │
    │ POST /api/auth/signup        │                              │
    │ {email, password,            │                              │
    │  storeName, shopifyDomain,   │                              │
    │  shopifyAccessToken}         │                              │
    ├─────────────────────────────>│                              │
    │                              │                              │
    │                              │ Hash password (BCrypt)       │
    │                              │ Generate UUID tenant_id      │
    │                              │                              │
    │                              │ INSERT INTO tenants          │
    │                              ├─────────────────────────────>│
    │                              │                              │
    │                              │ Generate JWT Token           │
    │                              │                              │
    │ {token, tenant info}         │                              │
    │<─────────────────────────────┤                              │
    │                              │                              │
    │ Store token in localStorage  │                              │
    │                              │                              │
    │ Subsequent requests:         │                              │
    │ Authorization: Bearer {token}│                              │
    ├─────────────────────────────>│                              │
    │                              │                              │
    │                              │ Validate JWT                 │
    │                              │ Extract tenant_id            │
    │                              │                              │
    │                              │ Query with tenant isolation  │
    │                              ├─────────────────────────────>│
    │                              │                              │
```

### 2. Shopify Data Sync Flow (Scheduled)

```
┌──────────────┐         ┌─────────────┐         ┌──────────┐         ┌─────────┐
│Quartz Cron   │         │   Backend   │         │ Shopify  │         │Database │
│(Every 6hrs)  │         │             │         │   API    │         │         │
└──────┬───────┘         └──────┬──────┘         └────┬─────┘         └────┬────┘
       │                        │                     │                    │
       │ Trigger at 0,6,12,18h  │                     │                    │
       ├───────────────────────>│                     │                    │
       │                        │                     │                    │
       │                        │ For each tenant:    │                    │
       │                        │                     │                    │
       │                        │ GET /customers.json │                    │
       │                        │ X-Shopify-Access-Token                   │
       │                        ├────────────────────>│                    │
       │                        │                     │                    │
       │                        │ Customers JSON (250/page)                │
       │                        │<────────────────────┤                    │
       │                        │                     │                    │
       │                        │ UPSERT customers    │                    │
       │                        │ (by shopify_customer_id)                 │
       │                        ├─────────────────────────────────────────>│
       │                        │                     │                    │
       │                        │ GET /orders.json    │                    │
       │                        ├────────────────────>│                    │
       │                        │                     │                    │
       │                        │ Orders JSON         │                    │
       │                        │<────────────────────┤                    │
       │                        │                     │                    │
       │                        │ UPSERT orders       │                    │
       │                        ├─────────────────────────────────────────>│
       │                        │                     │                    │
       │                        │ GET /products.json  │                    │
       │                        ├────────────────────>│                    │
       │                        │                     │                    │
       │                        │ Products JSON       │                    │
       │                        │<────────────────────┤                    │
       │                        │                     │                    │
       │                        │ UPSERT products     │                    │
       │                        ├─────────────────────────────────────────>│
       │                        │                     │                    │
       │                        │ Sync complete       │                    │
       │<───────────────────────┤                     │                    │
```

### 3. Real-time Webhook Flow

```
┌─────────┐         ┌──────────┐         ┌─────────────┐         ┌──────────┐
│Shopify  │         │ Backend  │         │   Verify    │         │ Database │
│         │         │ Webhook  │         │  Signature  │         │          │
└────┬────┘         └────┬─────┘         └──────┬──────┘         └────┬─────┘
     │                   │                      │                     │
     │ Order created     │                      │                     │
     │ in Shopify        │                      │                     │
     │                   │                      │                     │
     │ POST /webhooks/shopify/orders/create     │                     │
     │ X-Shopify-Shop-Domain: store.myshopify.com                     │
     │ X-Shopify-Hmac-SHA256: <signature>       │                     │
     │ {order JSON}      │                      │                     │
     ├──────────────────>│                      │                     │
     │                   │                      │                     │
     │                   │ Find tenant by       │                     │
     │                   │ shop domain          │                     │
     │                   ├──────────────────────────────────────────>│
     │                   │                      │                     │
     │                   │ Tenant found         │                     │
     │                   │<──────────────────────────────────────────┤
     │                   │                      │                     │
     │                   │ Verify HMAC-SHA256   │                     │
     │                   │ using tenant's       │                     │
     │                   │ access token         │                     │
     │                   ├─────────────────────>│                     │
     │                   │                      │                     │
     │                   │ Signature valid ✓    │                     │
     │                   │<─────────────────────┤                     │
     │                   │                      │                     │
     │                   │ Parse order JSON     │                     │
     │                   │ Save/Update order    │                     │
     │                   ├──────────────────────────────────────────>│
     │                   │                      │                     │
     │ 200 OK           │                      │                     │
     │<──────────────────┤                      │                     │
```

### 4. Customer Segmentation Flow

```
┌────────┐                  ┌─────────────┐                  ┌──────────┐
│ User   │                  │   Backend   │                  │ Database │
└───┬────┘                  └──────┬──────┘                  └────┬─────┘
    │                              │                              │
    │ GET /api/customers           │                              │
    │ ?segment=high                │                              │
    │ &search=john                 │                              │
    │ &page=0&size=20              │                              │
    ├─────────────────────────────>│                              │
    │                              │                              │
    │                              │ Extract tenant from JWT      │
    │                              │                              │
    │                              │ Build query:                 │
    │                              │ WHERE tenant_id = ?          │
    │                              │   AND total_spent > 5000     │
    │                              │   AND (first_name LIKE '%john%'│
    │                              │        OR email LIKE '%john%') │
    │                              │ ORDER BY total_spent DESC    │
    │                              │ LIMIT 20 OFFSET 0            │
    │                              ├─────────────────────────────>│
    │                              │                              │
    │                              │ Page<Customer> (20 results)  │
    │                              │<─────────────────────────────┤
    │                              │                              │
    │ {customers: [...],           │                              │
    │  totalPages: 5,              │                              │
    │  totalItems: 87}             │                              │
    │<─────────────────────────────┤                              │
```

## Technology Stack

### Frontend
- **Framework**: React 18.2.0
- **Build Tool**: Vite 5.0.8
- **Routing**: React Router DOM 6.20.1
- **HTTP Client**: Axios 1.6.2
- **Charts**: Chart.js 4.4.1, react-chartjs-2
- **Styling**: Tailwind CSS 3.3.6
- **Icons**: Lucide React 0.294.0
- **Date Handling**: date-fns 3.0.6
- **Deployment**: Vercel (Serverless, Global CDN)

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Security**: Spring Security 6.2.0, JWT (io.jsonwebtoken 0.12.3)
- **ORM**: Spring Data JPA, Hibernate
- **Database**: PostgreSQL 42.7.1
- **HTTP Client**: Spring WebFlux WebClient
- **Scheduler**: Quartz Scheduler 2.3.2
- **JSON**: Jackson
- **Build Tool**: Maven
- **Deployment**: Render (Docker container)

### Database
- **RDBMS**: PostgreSQL 16
- **Hosting**: Render (Managed PostgreSQL)
- **Region**: Singapore
- **Features**: Multi-tenant with UUID isolation, Indexed for performance

### External APIs
- **Shopify Admin REST API**: 2024-10 version
- **Authentication**: Custom access token per tenant
- **Webhooks**: HMAC-SHA256 signed

## Security Architecture

### Authentication & Authorization
1. **Password Security**:
   - BCrypt hashing with salt (strength: 10 rounds)
   - Passwords never stored in plain text
   - Minimum complexity enforced on client

2. **JWT Token**:
   - HS512 algorithm
   - Contains: tenant_id, email, store_name
   - Expiration: 24 hours
   - Stored in localStorage (client)
   - Sent as Bearer token in Authorization header

3. **Multi-Tenancy**:
   - UUID tenant_id for each tenant
   - All queries filtered by tenant_id
   - No cross-tenant data leakage
   - Tenant extracted from JWT claims

4. **CORS**:
   - Allowed origins: localhost (dev), Vercel domain (prod)
   - Credentials enabled for cookie support
   - Preflight caching: 1 hour

5. **Webhook Security**:
   - HMAC-SHA256 signature verification
   - Tenant-specific secret (shopify_access_token)
   - Replay attack prevention via timestamp

### API Security
- Public endpoints: `/api/auth/**`, `/api/health`, `/api/webhooks/**`
- Protected endpoints: All others require valid JWT
- Stateless session management
- SQL injection prevention via JPA parameterized queries

## Scalability Considerations

### Current Architecture
- **Horizontal Scaling**: Ready (stateless backend)
- **Database Connection Pooling**: HikariCP (default)
- **Pagination**: Server-side with configurable page size
- **Caching**: In-memory (can be upgraded to Redis)

### Performance Optimizations
1. **Database Indexes**:
   - Composite indexes on tenant_id + foreign keys
   - Email index for fast auth lookup
   - Created_at indexes for date range queries

2. **Query Optimization**:
   - Lazy loading for relationships
   - Fetch joins for N+1 prevention
   - Aggregate queries for statistics

3. **API Rate Limiting**:
   - Shopify API: 2 requests/second (respected)
   - Pagination: 250 items/page
   - Webhook retry: Automatic by Shopify

### Future Scaling Path
1. **Caching Layer**: Redis for session storage, frequent queries
2. **Message Queue**: RabbitMQ/Kafka for async webhook processing
3. **Read Replicas**: PostgreSQL read replicas for reporting
4. **CDN**: CloudFlare for frontend assets
5. **Load Balancer**: Render handles automatically
6. **Monitoring**: Sentry, New Relic, or Datadog

## Deployment Architecture

### Frontend (Vercel)
- **Build**: `npm run build` → static files
- **Deployment**: Git push → automatic deploy
- **Regions**: Global CDN, edge caching
- **Environment**: Production variables in Vercel dashboard
- **URL**: https://xeno-shopify-frontend-five.vercel.app

### Backend (Render)
- **Build**: Dockerfile → Maven package → JAR
- **Container**: eclipse-temurin:17-jre-alpine
- **Port**: 8080 (internal), 443 (external)
- **Region**: Singapore (same as database)
- **Health Check**: `/api/health` endpoint
- **Deployment**: Git push → automatic rebuild
- **URL**: https://xeno-shopify-backend-frzt.onrender.com

### Database (Render PostgreSQL)
- **Version**: PostgreSQL 16
- **Region**: Singapore
- **Backups**: Daily automatic backups
- **Connection**: SSL/TLS encrypted
- **Host**: dpg-d4omq1e3jp1c73dl8op0-a (internal)
- **Connection pooling**: Max 20 connections

## Monitoring & Logging

### Application Logs
- **Level**: INFO in production
- **Framework**: SLF4J + Logback
- **Location**: Render console logs
- **Retention**: 7 days (Render free tier)

### Key Metrics
- Response times per endpoint
- Shopify API call counts
- Database query performance
- Error rates and types
- Active user sessions

### Error Handling
- Global exception handler (@ControllerAdvice)
- Structured error responses
- HTTP status codes per error type
- User-friendly error messages

## Disaster Recovery

### Backup Strategy
- **Database**: Daily automated backups on Render
- **Code**: Git repositories (GitHub)
- **Configuration**: Environment variables in Render/Vercel

### Recovery Procedures
1. **Database Failure**: Restore from latest backup
2. **Backend Failure**: Redeploy from Git main branch
3. **Frontend Failure**: Redeploy from Git main branch
4. **Data Corruption**: Resync from Shopify via manual trigger

## Compliance & Data Privacy

### Data Handling
- **Customer PII**: Stored encrypted at rest (PostgreSQL)
- **In Transit**: HTTPS/TLS 1.3 everywhere
- **Access Control**: JWT-based, tenant-isolated
- **Data Retention**: As per Shopify merchant data

### GDPR Considerations
- User data export capability (future)
- Right to be forgotten (tenant deletion)
- Consent tracking (via Shopify)
- Data minimization principle

## Development Workflow

### Local Development
1. **Backend**: `mvn spring-boot:run`
2. **Frontend**: `npm run dev`
3. **Database**: Local PostgreSQL or Docker

### Git Workflow
- **Main branch**: Production-ready code
- **Feature branches**: For new features
- **Commit**: Conventional commits
- **Deploy**: Automatic on push to main

### Testing
- Unit tests: JUnit 5 (backend), Jest (frontend)
- Integration tests: Spring Boot Test
- E2E tests: Manual for now (can add Playwright)

## Future Enhancements

### Phase 2 (Planned)
1. **Swagger API Documentation**: OpenAPI 3.0 spec
2. **CSV Export**: Download customer/order data
3. **Product Analytics**: Revenue by product page
4. **Redis Caching**: Faster dashboard loads
5. **Enhanced README**: Complete productionization guide

### Phase 3 (Planned)
1. **Cart Abandonment Tracking**: Abandoned checkout webhooks
2. **Email Campaigns**: SendGrid integration
3. **Advanced Filters**: Multi-criteria customer search
4. **Role-Based Access**: Admin/Viewer roles
5. **Audit Logs**: Track all data changes

---

**Document Version**: 1.0  
**Last Updated**: December 4, 2025  
**Maintained By**: Xeno Development Team
