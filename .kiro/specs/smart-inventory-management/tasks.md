# Implementation Plan: Smart Inventory & Purchase Management System

## Overview

Full-stack implementation of a production-style inventory management system built with Java Spring Boot (backend), React + Vite (frontend), and MySQL. Tasks are ordered so that each step builds on the previous, ending with all components wired together. The backend is implemented first (foundation → security → domain modules → AI), followed by the frontend (scaffold → shared infrastructure → per-module pages → charts), and finally property-based and integration tests.

---

## Tasks
- [x] 1. Backend project scaffolding and shared infrastructure
  - [x] 1.1 Initialize Spring Boot project with all required dependencies
    - Create a Maven project (`com.inventory`) targeting Java 17+ with dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`, `mysql-connector-j`, `jjwt-api` / `jjwt-impl` / `jjwt-jackson`, `lombok`, `springdoc-openapi-starter-webmvc-ui`, and `spring-boot-starter-test`
    - Set up `application.properties` with datasource, JPA DDL-auto (`create-drop` for dev), JWT config keys, and CORS origin — all values referencing environment variables via `${ENV_VAR}` placeholders
    - Create the top-level package structure: `controller`, `service`, `repository`, `entity`, `dto`, `exception`, `security`, `config`, `ai`
    - _Requirements: 17.6, 17.7, 15.7_

  - [x] 1.2 Implement the API response envelope DTOs and global exception handler
    - Create `ApiResponse<T>` (success envelope: `success`, `statusCode`, `message`, `data`) and `ApiErrorResponse` (error envelope: `success`, `statusCode`, `message`, `timestamp`, optional `fieldErrors` map) in the `dto` package
    - Create all custom exception classes in the `exception` package: `ResourceNotFoundException`, `DuplicateResourceException`, `InvalidStatusTransitionException`, `InsufficientStockException`, `InvalidDateRangeException`, `AIServiceUnavailableException`
    - Implement `GlobalExceptionHandler` (`@ControllerAdvice`) mapping every exception type listed in the design to the correct HTTP status and envelope DTO; ensure stack traces are never serialized
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7_

  - [ ]* 1.3 Write property test for API response envelope structure (Property 18)
    - **Property 18: All API responses conform to the envelope structure**
    - **Validates: Requirements 12.1, 12.2, 12.3**
    - Use jqwik; call several endpoints with arbitrary valid and invalid payloads; assert `success`, `statusCode`, `message`, `data`/`timestamp` fields are always present and correctly typed

- [x] 2. Database entities and schema
  - [x] 2.1 Implement all JPA entity classes
    - Create `User`, `Category`, `Supplier`, `Product`, `PurchaseOrder`, `PurchaseOrderItem`, `StockMovement` entity classes in the `entity` package
    - Annotate with JPA (`@Entity`, `@Table`, `@Column`, `@Enumerated`, etc.); define `Role` and `OrderStatus` enums; define `MovementType` and `Direction` enums
    - Apply unique constraints: `User.username`, `User.email`, `Product.sku`, `Category.name`, `Supplier.email`, `PurchaseOrder.order_number`
    - Map all foreign key relationships (`@ManyToOne`, `@OneToMany`) with appropriate fetch types (`LAZY` for collections to avoid N+1); apply `@CreationTimestamp` / `@UpdateTimestamp` for audit fields
    - Declare all monetary columns as `DECIMAL(12,2)` and all timestamp columns as UTC `DATETIME`
    - `StockMovement` must include non-nullable columns: `stock_before INT`, `stock_after INT`, `unit_cost DECIMAL(12,2)`
    - _Requirements: 18.1, 18.2, 18.3, 18.4, 18.6_

  - [ ]* 2.2 Write property test for monetary precision round-trip (Property 20)
    - **Property 20: Monetary values survive a storage round-trip without precision loss**
    - **Validates: Requirements 18.3**
    - Use jqwik to generate random `BigDecimal` values with up to 2 decimal places and absolute value ≤ 999999999.99; persist via repository and retrieve; assert retrieved value equals stored value

- [x] 3. Security infrastructure — JWT and Spring Security
  - [x] 3.1 Implement JWT service and filter
    - Create `JwtService` in the `security` package with methods: `generateToken(UserDetails)`, `validateToken(String, UserDetails)`, `extractUsername(String)` — sign with HS256 using the secret from env var; token expiry from env var
    - Implement `JwtAuthFilter` (`OncePerRequestFilter`) that extracts the Bearer token, validates it via `JwtService`, and sets `SecurityContextHolder` authentication
    - _Requirements: 1.7, 1.8, 1.10, 15.2, 15.3_

  - [x] 3.2 Configure Spring Security filter chain
    - Create `SecurityConfig` (`@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity`)
    - Permit public endpoints (`/api/v1/auth/**`); require authentication for all others
    - Set session creation to `STATELESS` and disable CSRF for REST endpoints
    - Register `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`
    - Create `UserDetailsServiceImpl` that loads users from the `UserRepository`
    - Configure CORS from the env-variable origin in `CorsConfig`
    - _Requirements: 15.4, 15.8, 2.1_

  - [ ]* 3.3 Write unit tests for JWT service
    - Test token generation, successful validation, expiry rejection, malformed token rejection
    - Test that unauthenticated requests to protected endpoints return 401

- [x] 4. Authentication and user management
  - [x] 4.1 Implement auth entities, DTOs, repository, and service
    - Create `UserRepository` (extends `JpaRepository<User, Long>`) with finder methods `findByUsername` and `findByEmail`
    - Create request DTOs (`RegisterRequestDTO`, `LoginRequestDTO`) and response DTOs (`UserResponseDTO`, `LoginResponseDTO`)
    - Implement `AuthServiceImpl`: `register` — validate unique username/email (409 if clash), validate password complexity (400 if violated), hash with BCrypt, persist with `STAFF` role, ignore any role field in the request; `login` — authenticate credentials, return signed JWT and role
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.13, 15.1_

  - [x] 4.2 Implement AuthController and UserManagementController
    - `POST /api/v1/auth/register` — public; calls `AuthService.register`; returns 201
    - `POST /api/v1/auth/login` — public; calls `AuthService.login`; returns 200 with JWT
    - `POST /api/v1/users/` — ADMIN only; creates account with specified role; returns 201
    - `GET /api/v1/users/` — ADMIN only; paginated user list
    - _Requirements: 1.12, 2.2_

  - [ ]* 4.3 Write property tests for authentication (Properties 1, 2, 3)
    - **Property 1: Registration always assigns STAFF role** — generate payloads with any role value; assert created user always has STAFF
    - **Property 2: Invalid passwords are always rejected** — generate strings violating each complexity rule; assert HTTP 400 and no user created
    - **Property 3: API responses never expose passwords** — call any user endpoint; assert response JSON contains no `password`, `passwordHash`, or `password_hash` field
    - **Validates: Requirements 1.1, 1.3, 1.13, 15.5**

- [x] 5. Checkpoint — core infrastructure complete
  - Ensure the Spring Boot application starts cleanly, the schema is created, public auth endpoints respond, and protected endpoints return 401 without a token. Ask the user if questions arise.

- [x] 6. Category management
  - [x] 6.1 Implement Category entity, repository, DTOs, and service
    - Create `CategoryRepository` with `findByNameIgnoreCase` and `existsByNameIgnoreCase` finders
    - Create `CreateCategoryRequestDTO`, `UpdateCategoryRequestDTO`, `CategoryResponseDTO` (includes `productCount`)
    - Implement `CategoryServiceImpl`: create (409 if name duplicate, case-insensitive), update, deactivate (soft-delete), list all with product count, list products by category (paginated, 404 if category not found)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

  - [x] 6.2 Implement CategoryController
    - `POST /api/v1/categories/` — ADMIN; returns 201
    - `GET /api/v1/categories/` — ADMIN, STAFF
    - `PUT /api/v1/categories/{id}` — ADMIN
    - `PATCH /api/v1/categories/{id}/deactivate` — ADMIN
    - `GET /api/v1/categories/{id}/products` — ADMIN, STAFF; paginated
    - _Requirements: 4.1–4.7, 2.2_

- [x] 7. Supplier management
  - [x] 7.1 Implement Supplier entity, repository, DTOs, and service
    - Create `SupplierRepository` with `findByEmail` and `existsByEmail` finders; add paginated query method with name/company/status filters
    - Create `CreateSupplierRequestDTO` (with `@Email` and phone `@Pattern` validation), `UpdateSupplierRequestDTO`, `SupplierResponseDTO` (includes `totalPurchaseOrderCount`)
    - Implement `SupplierServiceImpl`: create (409 if email duplicate, 400 for invalid email/phone), update, deactivate (soft-delete), paginated list with filters, get by ID (404 if not found)
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9_

  - [x] 7.2 Implement SupplierController
    - `POST /api/v1/suppliers/` — ADMIN; returns 201
    - `GET /api/v1/suppliers/` — ADMIN, STAFF; paginated with filters
    - `GET /api/v1/suppliers/{id}` — ADMIN, STAFF
    - `PUT /api/v1/suppliers/{id}` — ADMIN
    - `PATCH /api/v1/suppliers/{id}/deactivate` — ADMIN
    - _Requirements: 5.1–5.9, 2.2_

- [x] 8. Product management
  - [x] 8.1 Implement Product entity, repository, DTOs, and service
    - Create `ProductRepository` with paginated query method supporting filtering by name (partial), SKU (partial), category ID, and active status; sortable by name, SKU, current stock, and creation date
    - Create `CreateProductRequestDTO` (with `@Min(0)` on price/stock fields and custom cross-field validator for `minStockLevel ≤ maxStockLevel`), `UpdateProductRequestDTO`, `ProductResponseDTO` (includes category name, all fields from Req 3.8)
    - Implement `ProductServiceImpl`: create (409 if SKU duplicate; initial stock = 0; 400 for negative values, min > max), update (409 if SKU belongs to different product), deactivate (soft-delete), paginated list, get by ID (404 if not found)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11_

  - [x] 8.2 Implement ProductController
    - `POST /api/v1/products/` — ADMIN, STAFF; returns 201
    - `GET /api/v1/products/` — ADMIN, STAFF; paginated with search/filter/sort
    - `GET /api/v1/products/{id}` — ADMIN, STAFF
    - `PUT /api/v1/products/{id}` — ADMIN, STAFF
    - `PATCH /api/v1/products/{id}/deactivate` — ADMIN
    - _Requirements: 3.1–3.11, 2.2, 2.3_

  - [ ]* 8.3 Write property tests for product management (Properties 4, 5)
    - **Property 4: New products always start with zero stock** — generate random valid product creation requests; assert `currentStock = 0` in every response
    - **Property 5: Invalid stock level ranges are rejected** — generate `(minStockLevel, maxStockLevel)` pairs where `min > max`; assert HTTP 400 and no product persisted
    - **Validates: Requirements 3.1, 3.10, 3.11**

- [x] 9. Pagination infrastructure
  - [x] 9.1 Implement shared pagination utilities and response wrapper
    - Create `PageResponseDTO<T>` wrapping the content list and pagination metadata: `currentPage`, `pageSize`, `totalElements`, `totalPages`, `first`, `last`
    - Create `PaginationUtils` that converts Spring `Page<T>` to `PageResponseDTO<T>` and validates the metadata invariants
    - Confirm all existing list endpoints (categories, suppliers, products) use `PageResponseDTO` inside the `ApiResponse` envelope
    - _Requirements: 13.1, 13.2, 13.5, 13.6_

  - [ ]* 9.2 Write property test for pagination metadata invariants (Property 6)
    - **Property 6: Paginated responses satisfy metadata invariants**
    - **Validates: Requirements 13.1, 13.2, 13.6**
    - Use jqwik to generate random `(pageSize, pageNumber, totalElements)` triples; verify `totalPages = ceil(T/S)`, item count ≤ S, `first`/`last` correctness, and empty data when `P ≥ totalPages`

- [x] 10. Purchase Order management
  - [x] 10.1 Implement PurchaseOrder and PurchaseOrderItem entities, repository, and DTOs
    - Create `PurchaseOrderRepository` with paginated query method supporting filtering by supplier ID, status, order date range, and order number (partial); sortable by order date and total
    - Create `CreatePurchaseOrderRequestDTO` (with `@NotEmpty` items list), `PurchaseOrderItemRequestDTO` (quantity > 0, price > 0), `UpdateStatusRequestDTO`, `PurchaseOrderResponseDTO` (includes full item list with product name, SKU, subtotals), `PurchaseOrderItemResponseDTO`
    - _Requirements: 6.1, 6.14, 6.15_

  - [x] 10.2 Implement PurchaseOrderService — creation and status transition logic
    - `createOrder`: validate supplier exists and is active (400), validate all product IDs exist and are active (400), reject duplicate product IDs in items (400), reject delivery date before order date (400); auto-generate `PO-YYYYMMDD-NNNNNN` order number; compute subtotals and total; persist with `DRAFT` status; return 201
    - `updateStatus`: enforce state machine (`DRAFT → ORDERED/CANCELLED`, `ORDERED → RECEIVED/CANCELLED`; all others → 400); `CANCELLED` transition sets status without touching stock; `RECEIVED` transition — see task 10.3
    - _Requirements: 6.1–6.7, 6.10, 6.11, 6.12, 6.13_

  - [x] 10.3 Implement the transactional RECEIVED stock-update flow
    - In `PurchaseOrderService.updateStatus` for `RECEIVED`: annotate with `@Transactional`; for each `PurchaseOrderItem` lock the product row (`SELECT FOR UPDATE` via `@Lock(PESSIMISTIC_WRITE)`); capture `stockBefore`; compute `stockAfter = stockBefore + item.quantity`; create a `StockMovement` record with `movementType = PURCHASE`, `direction = ADD`, `quantity = item.quantity`, `unitCost = item.purchasePrice`, `stockBefore`, `stockAfter`, `note = order.orderNumber`, `timestamp = UTC now`, `actingUser = authenticated user`; update `product.currentStock = stockAfter`; set `order.status = RECEIVED`; if any step throws, the transaction rolls back and returns HTTP 500
    - _Requirements: 6.8, 6.9_

  - [x] 10.4 Implement PurchaseOrderController
    - `POST /api/v1/purchase-orders/` — ADMIN, STAFF; returns 201
    - `GET /api/v1/purchase-orders/` — ADMIN, STAFF; paginated with filters/sort
    - `GET /api/v1/purchase-orders/{id}` — ADMIN, STAFF
    - `PATCH /api/v1/purchase-orders/{id}/status` — ADMIN, STAFF
    - `GET /api/v1/purchase-orders/{id}/items` — ADMIN, STAFF
    - _Requirements: 6.1–6.15, 2.2, 2.3_

  - [ ]* 10.5 Write property tests for Purchase Order (Properties 7, 8, 9)
    - **Property 7: Purchase order totals are arithmetically correct** — generate random N-item orders; assert `subtotal_i = qty_i × price_i` and `total = Σ subtotal_i` for every combination
    - **Property 8: PO status machine is correctly enforced** — cross-product all `(current_status, target_status)` pairs; assert only valid transitions return 200 and all others return 400
    - **Property 9: RECEIVED transition correctly updates stock and creates movements** — generate random N-item orders with mocked repositories; assert per-item stock increments, movement fields, and atomicity
    - **Validates: Requirements 6.1, 6.6, 6.7, 6.8, 6.9, 6.11**

- [x] 11. Stock Movement (manual)
  - [x] 11.1 Implement StockMovement repository and service
    - Create `StockMovementRepository` with paginated query method supporting filtering by product ID, movement type, date range, and acting user ID; default sort by timestamp descending
    - Create `CreateMovementRequestDTO` with Bean Validation: `@NotNull` product ID, `@NotNull` movement type, quantity `@Min(1)` `@Max(1_000_000)`, optional note `@Size(max=500)`, and direction required when `movementType = ADJUSTMENT` (validated in service)
    - Implement `StockMovementServiceImpl.recordMovement` `@Transactional`: validate product exists and is active (404/400); reject `ADJUSTMENT` without direction (400); lock product row; compute signed delta; reject if `stockAfter < 0` (400); capture `stockBefore`; compute `stockAfter`; set `direction` from movement type; set `unitCost = product.currentPurchasePrice`; create immutable `StockMovement` record; update `product.currentStock`; return 201 with `StockMovementResponseDTO`
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9_

  - [x] 11.2 Implement StockMovementController
    - `POST /api/v1/stock-movements/` — ADMIN, STAFF; returns 201
    - `GET /api/v1/stock-movements/` — ADMIN, STAFF; paginated with filters/sort
    - _Requirements: 7.1–7.9, 2.2, 2.3_

  - [ ]* 11.3 Write property tests for stock movements (Properties 10, 11, 12)
    - **Property 10: Movement delta and audit fields are consistent** — generate random `(currentStock, movementType, quantity)`; assert `stockBefore`, `stockAfter`, `direction`, and `unitCost` are computed correctly
    - **Property 11: Stock cannot be driven below zero** — generate requests where `stockAfter < 0`; assert HTTP 400 and no persistence
    - **Property 12: Movement quantity bounds are enforced** — generate `quantity ≤ 0` and `quantity > 1,000,000`; assert HTTP 400
    - **Validates: Requirements 7.1, 7.2, 7.3, 7.7, 7.8**

- [x] 12. Low Stock management
  - [x] 12.1 Implement Low Stock service and controller
    - Implement `LowStockServiceImpl`:
      - `getLowStockProducts(filter, pageable)` — returns active products with `currentStock ≤ minStockLevel`; accepts optional `stock-status` filter for `LOW_STOCK` (stock > 0 and ≤ min) or `OUT_OF_STOCK` (stock = 0)
      - `getOutOfStockProducts(pageable)` — returns active products with `currentStock = 0`
      - `getLowStockSummary()` — returns aggregate counts (LOW_STOCK, OUT_OF_STOCK) plus top-10 critical list ordered by: products with `minStockLevel > 0` sorted by `currentStock / minStockLevel` ascending (ratio-rank), then products with `minStockLevel = 0` sorted by name ascending; ties in ratio broken by name ascending — implement as a single JPQL query using a `CASE` expression
    - Create `LowStockController` with endpoints: `GET /api/v1/low-stock/`, `GET /api/v1/low-stock/out-of-stock`, `GET /api/v1/low-stock/summary`
    - Response items include: product name, SKU, category name, current stock, minimum stock level, stock status
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8_

  - [ ]* 12.2 Write property tests for low stock (Properties 13, 14)
    - **Property 13: Low-stock classification matches the definition** — generate random `(currentStock, minStockLevel)` pairs; assert `OUT_OF_STOCK` when stock = 0, `LOW_STOCK` when 0 < stock ≤ min, absent when stock > min
    - **Property 14: Low-stock summary top-10 sort is correctly ordered** — generate random sets of products with varying stock/min levels; assert the sort order matches the criticality-ratio specification
    - **Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7**

- [x] 13. Dashboard endpoints
  - [x] 13.1 Implement Dashboard service
    - Implement `DashboardServiceImpl`:
      - `getSummary()` — query DB for: total active product count, total active supplier count, total inventory value (`SUM(currentStock × purchasePrice)` for active products), total PO count, count of POs with status `ORDERED`, count of LOW_STOCK products, count of OUT_OF_STOCK products
      - `getRecentMovements()` — return 10 most recent `StockMovement` records ordered by timestamp descending
      - `getPurchaseTrend(start, end)` — aggregate PO totals grouped by day for POs with status `ORDERED` or `RECEIVED` within range; validate `start ≤ end`
      - `getInventoryTrend(start, end)` — (1) compute opening value: `SUM(signed qty × unitCost)` for all movements with `timestamp < start`; (2) for each day D in `[start, end]`, compute daily delta; (3) output cumulative `{ date, inventoryValue }` per day — use each movement's recorded `unitCost`, not the current product price
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [x] 13.2 Implement DashboardController
    - `GET /api/v1/dashboard/summary` — ADMIN, STAFF
    - `GET /api/v1/dashboard/recent-movements` — ADMIN, STAFF
    - `GET /api/v1/dashboard/purchase-trend?startDate=&endDate=` — ADMIN, STAFF
    - `GET /api/v1/dashboard/inventory-trend?startDate=&endDate=` — ADMIN, STAFF
    - _Requirements: 9.1–9.4_

  - [ ]* 13.3 Write property test for inventory trend cumulative sum (Property 15)
    - **Property 15: Inventory trend value is a correct cumulative sum**
    - **Validates: Requirements 9.4**
    - Use jqwik to generate random lists of `StockMovement` records with timestamps; assert opening value formula, per-day cumulative sum, one entry per calendar day, and correct use of `unitCost`

- [x] 14. Reports endpoints
  - [x] 14.1 Implement Reports service and controller
    - Implement `ReportServiceImpl`:
      - `getPurchaseReport(start, end)` — validate `start ≤ end` (400); return total order count, total amount, breakdown by status, and order list (order number, supplier name, date, status, total)
      - `getInventoryReport(start, end)` — active products with current stock, min/max levels, total purchase qty received in range, total sale/damaged/adjustment qty in range
      - `getSupplierPurchaseReport(start, end)` — suppliers with count and total value of POs in range, ordered by total value descending
      - `getStockMovementReport(start, end, movementType?)` — movements in range matching optional type filter, plus total quantity moved per type
      - `getLowStockReport()` — active low/out-of-stock products with `recommendedReorderQuantity = maxStockLevel - currentStock`
    - Create `ReportController` with endpoints: `GET /api/v1/reports/purchase`, `GET /api/v1/reports/inventory`, `GET /api/v1/reports/supplier-purchase`, `GET /api/v1/reports/stock-movements`, `GET /api/v1/reports/low-stock`
    - All endpoints validate `startDate ≤ endDate` (HTTP 400 if violated)
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_

  - [ ]* 14.2 Write property tests for reports (Properties 16, 17)
    - **Property 16: Low-stock report reorder quantity is correct** — generate random `(currentStock, maxStockLevel)` pairs where `currentStock ≤ maxStockLevel`; assert `recommendedReorderQuantity = maxStockLevel - currentStock`
    - **Property 17: Report endpoints reject inverted date ranges** — generate `(start, end)` pairs where `start > end` for every report endpoint; assert HTTP 400
    - **Validates: Requirements 10.5, 10.6**

- [x] 15. AI Inventory Assistant module
  - [x] 15.1 Implement isolated AI service module
    - Create `AIService` interface in `com.inventory.ai` with single method `String query(AIQueryRequestDTO req)`
    - Implement `OpenAIServiceImpl` (implements `AIService`):
      - Inject `RestTemplate` configured with 30-second read timeout (from `AIConfig`)
      - Inject OpenAI API key from env var (never hardcode)
      - On `query(req)`: (a) read relevant summary data from repositories (product counts, low-stock items, recent movements, supplier count); (b) build a context prompt from that data plus `req.question`; (c) call OpenAI Chat Completions API; (d) extract and return response text
      - Wrap all OpenAI HTTP calls in try-catch; log raw error server-side; throw `AIServiceUnavailableException` on any failure or timeout → mapped to HTTP 503
    - The AI module MUST NOT share business logic with any other service or directly mutate any entity
    - _Requirements: 11.1, 11.2, 11.3, 11.5, 11.6, 11.7, 11.8_

  - [x] 15.2 Implement AIController
    - `POST /api/v1/ai/query` — ADMIN, STAFF (JWT required; 401 if unauthenticated)
    - Pass authenticated user context; delegate to `AIService.query(req)`; return response text in standard `ApiResponse` envelope
    - _Requirements: 11.3, 11.9_

  - [ ]* 15.3 Write unit tests for AI service
    - Mock `RestTemplate`; test successful OpenAI response passthrough; test timeout → 503; test that raw OpenAI error message is NOT in response body
    - Test that `AIController` returns 401 for unauthenticated requests

- [x] 16. Checkpoint — full backend complete
  - Ensure all backend endpoints respond correctly, the Swagger UI (`/swagger-ui.html`) is accessible, all custom exception types map to the correct HTTP status codes, and the transactional PO RECEIVED flow correctly increments stock. Ask the user if questions arise.

- [x] 17. Frontend project scaffolding and shared infrastructure
  - [x] 17.1 Initialize React + Vite project with all required dependencies
    - Create a Vite React project; install: `react-router-dom`, `axios`, `recharts`, `lucide-react`, `tailwindcss` (with PostCSS config), `@tailwindcss/forms`
    - Configure Tailwind CSS; set up `vite.config.js` with API proxy to backend during dev
    - Create the directory structure: `src/api/`, `src/components/`, `src/contexts/`, `src/hooks/`, `src/layouts/`, `src/pages/`, `src/routes/`, `src/utils/`
    - _Requirements: 16.1_

  - [x] 17.2 Implement Axios instance, interceptors, and per-domain API modules
    - Create `src/api/axiosInstance.js` with `baseURL` from env var; add request interceptor to attach `Authorization: Bearer <token>` from `AuthContext`; add response interceptor to handle 401 (redirect to login) and extract `data` from the envelope
    - Create per-domain API modules: `authApi.js`, `productsApi.js`, `categoriesApi.js`, `suppliersApi.js`, `purchaseOrdersApi.js`, `stockMovementsApi.js`, `lowStockApi.js`, `dashboardApi.js`, `reportsApi.js`, `aiApi.js`
    - _Requirements: 16.13_

  - [x] 17.3 Implement AuthContext, useAuth hook, and route guards
    - Create `AuthContext` storing JWT (in memory or httpOnly cookie), user role, `login(token, role)`, and `logout()` functions; `logout` discards the JWT and redirects to login
    - Create `useAuth` hook exposing context values
    - Create `ProtectedRoute` component that redirects unauthenticated users to `/login`
    - Create `RoleRoute` component that redirects users lacking the required role to a 403 page
    - _Requirements: 1.11, 15.6, 16.2, 16.5, 16.12_

  - [x] 17.4 Implement shared UI components
    - `AppLayout` — persistent sidebar + `<Outlet>`; sidebar items filtered by role using `useAuth`
    - `AuthLayout` — centered card layout for login/register pages
    - `DataTable` — generic table component with server-side pagination controls (page, pageSize, sort column, sort direction) and column definitions
    - `Modal` — reusable confirmation dialog for destructive actions
    - `Toast` / `useToast` — success/error notification system
    - `LoadingSpinner` — displayed during in-flight API requests
    - `EmptyState` — displayed when a list returns zero results
    - `ErrorMessage` — inline API error display
    - `FormField` — wrapper for inputs with field-level error display
    - _Requirements: 16.1, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8, 16.9_

- [x] 18. Frontend auth and user management pages
  - [x] 18.1 Implement Login and Register pages
    - `src/pages/auth/LoginPage.jsx` — form with username/password; on submit call `authApi.login`; store JWT in `AuthContext`; redirect to dashboard; display API error on failure
    - `src/pages/auth/RegisterPage.jsx` — form with username, email, password; client-side password complexity validation; on submit call `authApi.register`; display success/error
    - Both pages use `AuthLayout`; unauthenticated route (no guard)
    - _Requirements: 1.5, 1.6, 16.9_

  - [x] 18.2 Set up React Router route definitions
    - Define routes in `src/routes/routes.jsx`: public routes (`/login`, `/register`), protected routes wrapped in `ProtectedRoute`, admin-only routes wrapped in `RoleRoute`
    - Implement a `NotFound` (404) page and a `Forbidden` (403) page
    - _Requirements: 16.2, 16.5, 16.12_

- [x] 19. Frontend pages — Categories, Suppliers, Products
  - [x] 19.1 Implement Categories page (ADMIN only)
    - `src/pages/categories/CategoriesPage.jsx` — table listing all categories (name, description, active status, product count); "Create Category" button opens a modal form; "Edit" opens update form; "Deactivate" shows confirmation dialog then calls deactivate API; display loading, empty, and error states; ADMIN-only route
    - _Requirements: 4.1–4.7, 16.3, 16.4, 16.5, 16.6, 16.7, 16.9_

  - [x] 19.2 Implement Suppliers page (ADMIN only)
    - `src/pages/suppliers/SuppliersPage.jsx` — paginated table with search (name, company) and active status filter; "Create Supplier" modal form with email/phone validation; "Edit", "Deactivate" with confirmation; `SupplierDetailPage.jsx` showing supplier info and PO count; ADMIN-only route
    - _Requirements: 5.1–5.9, 16.3–16.9_

  - [x] 19.3 Implement Products pages
    - `src/pages/products/ProductsPage.jsx` — paginated table with search (name, SKU), category filter, active status filter, and sort controls; "Create Product" modal; "Edit Product" modal; "Deactivate" confirmation dialog; both ADMIN and STAFF
    - `src/pages/products/ProductDetailPage.jsx` — shows full product info, stock history link
    - Client-side validation: min stock ≤ max stock; negative price/stock rejected before API call
    - _Requirements: 3.1–3.11, 16.3–16.10_

- [x] 20. Frontend pages — Purchase Orders
  - [x] 20.1 Implement Purchase Orders list and detail pages
    - `src/pages/purchase-orders/PurchaseOrdersPage.jsx` — paginated table with filters (supplier, status, date range, PO number search) and sort; both ADMIN and STAFF
    - `src/pages/purchase-orders/PurchaseOrderDetailPage.jsx` — displays full PO header and line items table; status transition buttons (ORDERED, RECEIVED, CANCELLED) with confirmation dialogs for destructive actions; RECEIVED button shows warning that stock will be updated
    - _Requirements: 6.1–6.15, 16.3–16.9_

  - [x] 20.2 Implement Create Purchase Order page
    - `src/pages/purchase-orders/CreatePurchaseOrderPage.jsx` — form with supplier selector (active suppliers only), expected delivery date picker, and dynamic line-items table (add/remove rows for product + quantity + purchase price); client-side validation (at least one item, quantity > 0, price > 0, no duplicate products, delivery date ≥ today); on submit returns to list with success toast
    - _Requirements: 6.1–6.5, 16.9_

- [x] 21. Frontend pages — Stock Movements and Low Stock
  - [x] 21.1 Implement Stock Movements page
    - `src/pages/stock-movements/StockMovementsPage.jsx` — paginated table with filters (product, movement type, date range, user); "Record Movement" button opens form modal with: product selector, movement type selector, quantity input, direction selector (visible only when type is ADJUSTMENT), optional note; client-side validation before submit
    - _Requirements: 7.1–7.9, 16.3–16.9_

  - [x] 21.2 Implement Low Stock page
    - `src/pages/low-stock/LowStockPage.jsx` — displays summary bar (LOW_STOCK count, OUT_OF_STOCK count); paginated table of low/out-of-stock products with stock-status filter toggle (ALL, LOW_STOCK, OUT_OF_STOCK); each row shows product name, SKU, category, current stock, minimum stock level, and status badge
    - _Requirements: 8.1–8.8, 16.3–16.5_

- [x] 22. Frontend Dashboard page with Recharts
  - [x] 22.1 Implement Dashboard KPI cards and alert panel
    - `src/pages/dashboard/DashboardPage.jsx` — fetch `/api/v1/dashboard/summary`; render KPI cards: total products, total suppliers, inventory value, total POs, pending POs (ORDERED), low-stock count, out-of-stock count; render low-stock alert panel (product name, SKU, stock status) using data from `/api/v1/low-stock/summary`
    - _Requirements: 9.1, 9.5, 9.6_

  - [x] 22.2 Implement purchase trend and inventory trend charts
    - Add date-range pickers for both charts; fetch `/api/v1/dashboard/purchase-trend` and `/api/v1/dashboard/inventory-trend` with the selected range
    - Render purchase trend as a Recharts `<BarChart>` (x: date, y: total amount)
    - Render inventory trend as a Recharts `<LineChart>` (x: date, y: cumulative inventory value)
    - Handle loading and empty states for both charts
    - _Requirements: 9.3, 9.4, 9.7, 16.11_

  - [x] 22.3 Implement recent stock movements table
    - Fetch `/api/v1/dashboard/recent-movements`; render the 10 most recent movements in a compact table (product, SKU, type, direction, quantity, user, timestamp)
    - _Requirements: 9.2, 9.8_

- [x] 23. Frontend Reports and AI Assistant pages
  - [x] 23.1 Implement Reports page
    - `src/pages/reports/ReportsPage.jsx` — tabbed layout for: Purchase Report, Inventory Report, Supplier Purchase Report, Stock Movement Report, Low Stock Report
    - Each tab has date-range pickers (except Low Stock), a "Generate Report" button, and a results table; client-side validation: start ≤ end date
    - Display loading, empty, and error states per tab
    - _Requirements: 10.1–10.7, 16.3–16.5, 16.9_

  - [x] 23.2 Implement AI Assistant page
    - `src/pages/ai-assistant/AIAssistantPage.jsx` — chat-style interface with a text input and submit button; display user question and AI response in a conversation thread
    - Display a prominent label on every AI response: **"This is a suggestion — please review before taking action"**
    - Display 503 / network error with user-friendly message (never raw error)
    - Provide example query prompts for the 5 supported query types
    - _Requirements: 11.3, 11.4, 11.5, 11.6, 16.3, 16.5_

- [x] 24. Frontend form validation and responsive layout
  - [x] 24.1 Audit and complete client-side form validation across all pages
    - Verify every form has field-level validation (required fields, formats, cross-field rules) that fires before API submission
    - Ensure all forms display field-level error messages using the shared `FormField` component
    - _Requirements: 16.9_

  - [x] 24.2 Verify responsive layout across breakpoints
    - Verify that sidebar collapses correctly on narrow viewports (375px mobile); tables scroll horizontally; modals and forms render usably at 375px and fill correctly at 1920px
    - _Requirements: 16.10_

- [ ] 25. Integration tests for critical transactional paths
  - [ ]* 25.1 Write integration test: full authentication flow
    - Use `@SpringBootTest` with `TestRestTemplate` or MockMvc; seed a blank DB; register → login → access protected endpoint with JWT → verify 200; verify expired/missing token returns 401
    - _Requirements: 1.1, 1.5, 1.7, 1.8, 1.9_

  - [ ]* 25.2 Write integration test: PO RECEIVED transaction end-to-end
    - Seed DB with one supplier, two products (known stock); create PO with two items → transition to ORDERED → transition to RECEIVED; assert both products' `currentStock` increased by item quantities; assert two `StockMovement` records with correct `movementType = PURCHASE`, `direction = ADD`, `stockBefore`, `stockAfter`, `unitCost`, and `note = orderNumber`
    - _Requirements: 6.8, 6.9_

  - [ ]* 25.3 Write integration test: manual stock movement end-to-end
    - Record a SALE movement; assert product stock decremented; assert `StockMovement` created with `direction = REDUCE`, `unitCost = product.purchasePrice` at time of movement, correct `stockBefore` and `stockAfter`; attempt SALE that would drive stock negative; assert HTTP 400 and no DB change
    - _Requirements: 7.1, 7.2, 7.3_

  - [ ]* 25.4 Write integration test: concurrent stock modification (row-level locking)
    - Simulate two simultaneous RECEIVED transitions on overlapping products using two threads; assert no double-counting — final stock equals `initial + quantity_A + quantity_B` and exactly two `StockMovement` records per product
    - _Requirements: 6.8, 6.9_

  - [ ]* 25.5 Write integration test: dashboard KPIs match DB aggregates
    - Seed known data; call `/api/v1/dashboard/summary`; assert each KPI matches independently-computed DB aggregates; call `/api/v1/dashboard/inventory-trend`; assert cumulative values match manual calculation using recorded `unitCost` values
    - _Requirements: 9.1, 9.4_

  - [ ]* 25.6 Write integration test: search result consistency (Property 19)
    - **Property 19: Search results are always consistent with the search query**
    - **Validates: Requirements 13.3**
    - Seed a known product dataset; use jqwik to generate random query strings; assert every result contains the query string in its searchable field (case-insensitive) and no matching item is absent

- [x] 26. Final checkpoint — full system integration
  - Run all unit, property-based, and integration tests; verify all tests pass. Confirm the React frontend communicates correctly with the Spring Boot backend via Axios interceptors (Bearer token, error handling). Confirm role-based route guards redirect as expected. Ask the user if questions arise.

---

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP delivery
- Each task references specific requirements for full traceability
- Checkpoints (tasks 5, 16, 26) validate incremental progress before continuing
- Property-based tests use [jqwik](https://jqwik.net/) (Java) on the backend with `@Property(tries = 100)` minimum
- Frontend tests use Vitest + React Testing Library
- All 20 correctness properties defined in `design.md` are covered by property-based test sub-tasks
- The AI module is architecturally isolated — `AIController → AIService interface → OpenAIServiceImpl` — replacing the provider requires only swapping the implementation bean
- `StockMovement.unitCost` captures the product's purchase price at the time of movement; the inventory trend calculation uses this captured cost, never the current product price

---

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "2.1"] },
    { "id": 2, "tasks": ["1.3", "2.2", "3.1"] },
    { "id": 3, "tasks": ["3.2", "3.3"] },
    { "id": 4, "tasks": ["4.1"] },
    { "id": 5, "tasks": ["4.2"] },
    { "id": 6, "tasks": ["4.3", "6.1", "7.1", "8.1", "9.1"] },
    { "id": 7, "tasks": ["6.2", "7.2", "8.2", "9.2"] },
    { "id": 8, "tasks": ["8.3", "10.1"] },
    { "id": 9, "tasks": ["10.2"] },
    { "id": 10, "tasks": ["10.3"] },
    { "id": 11, "tasks": ["10.4", "11.1"] },
    { "id": 12, "tasks": ["10.5", "11.2"] },
    { "id": 13, "tasks": ["11.3", "12.1"] },
    { "id": 14, "tasks": ["12.2", "13.1"] },
    { "id": 15, "tasks": ["13.2"] },
    { "id": 16, "tasks": ["13.3", "14.1"] },
    { "id": 17, "tasks": ["14.2", "15.1"] },
    { "id": 18, "tasks": ["15.2"] },
    { "id": 19, "tasks": ["15.3", "17.1"] },
    { "id": 20, "tasks": ["17.2"] },
    { "id": 21, "tasks": ["17.3", "17.4"] },
    { "id": 22, "tasks": ["18.1", "18.2"] },
    { "id": 23, "tasks": ["19.1", "19.2", "19.3"] },
    { "id": 24, "tasks": ["20.1", "20.2", "21.1", "21.2"] },
    { "id": 25, "tasks": ["22.1", "22.2", "22.3"] },
    { "id": 26, "tasks": ["23.1", "23.2"] },
    { "id": 27, "tasks": ["24.1", "24.2"] },
    { "id": 28, "tasks": ["25.1", "25.2", "25.3", "25.4", "25.5", "25.6"] }
  ]
}
```
