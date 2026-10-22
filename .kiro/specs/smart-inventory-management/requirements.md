# Requirements Document

## Introduction

The Smart Inventory & Purchase Management System is a production-style full-stack web application designed for small and medium-sized businesses. The system enables businesses to manage products, categories, suppliers, purchase orders, inventory stock movements, low-stock alerts, analytics dashboards, reports, and AI-powered inventory insights through a secure, role-based web interface.

The system is composed of a React.js + Vite frontend, a Java Spring Boot backend exposing REST APIs secured with JWT authentication, and a MySQL relational database. An AI assistant module integrates with the OpenAI API and is architecturally isolated so it can be reconfigured or replaced independently.

Two user roles exist: **ADMIN** (full system access) and **STAFF** (operational access with restricted administrative actions).

---

## Glossary

- **System**: The Smart Inventory & Purchase Management System as a whole.
- **API**: The Spring Boot REST API backend.
- **UI**: The React.js frontend application.
- **User**: An authenticated human operator interacting with the System.
- **ADMIN**: A User with the ADMIN role, having full access to all modules and administrative functions.
- **STAFF**: A User with the STAFF role, with access to operational modules and read-only or restricted access to administrative functions.
- **Product**: A stocked item tracked by the System, identified by a unique SKU.
- **SKU**: Stock Keeping Unit — a unique alphanumeric identifier for a Product.
- **Category**: A classification group to which one or more Products belong.
- **Supplier**: An external vendor from whom Products are purchased.
- **Purchase_Order**: A formal record of an intent to purchase one or more Products from a Supplier.
- **Purchase_Order_Item**: A line item within a Purchase_Order representing a quantity of a specific Product at a given purchase price.
- **Stock_Movement**: An immutable audit record of every change to a Product's stock quantity, capturing: movement type, direction (ADD or REDUCE), quantity (a positive integer), unit_cost (the Product's purchase price at the time of the movement), stock_before (stock level immediately before the change), stock_after (stock level immediately after the change), timestamp (UTC), acting User, and an optional note.
- **Movement_Type**: The classification of a Stock_Movement: PURCHASE, SALE, DAMAGED, or ADJUSTMENT.
- **Inventory**: The aggregate of all current stock levels across all active Products.
- **Low_Stock_Alert**: A condition where a Product's current stock is at or below its minimum stock level.
- **Out_Of_Stock**: A condition where a Product's current stock is zero.
- **Dashboard**: The landing page of the UI presenting key performance indicators, alerts, and recent activity.
- **AI_Assistant**: The isolated module that integrates with the OpenAI API to provide inventory insights and recommendations.
- **JWT**: JSON Web Token used for stateless authentication.
- **DTO**: Data Transfer Object — a payload structure used in API requests and responses that does not directly expose database entities.
- **Repository**: The Spring Data JPA data-access layer.
- **Service**: The Spring Boot business-logic layer.
- **Controller**: The Spring Boot REST layer that receives HTTP requests and delegates to Services.
- **BCrypt**: The password hashing algorithm used to store passwords securely.
- **GST**: Goods and Services Tax registration number for a Supplier.
- **Pagination**: Division of large result sets into discrete pages with configurable page size.

---

## Requirements

---

### Requirement 1: User Authentication

**User Story:** As a User, I want to register and log in securely, so that I can access the System with my own identity and appropriate permissions.

#### Acceptance Criteria

1. WHEN a registration request is received at the public registration endpoint with a unique username, valid email, and a password of at least 8 characters containing at least one uppercase letter, one lowercase letter, and one digit, THE API SHALL create a new User account with the STAFF role and return HTTP 201; the public registration endpoint SHALL NOT accept a role field, and any submitted role value SHALL be silently ignored.
2. WHEN a registration request is received with a username or email that already exists in the database, THE API SHALL return HTTP 409 with an error message identifying the duplicate field.
3. WHEN a registration request is received with a password that does not meet the complexity requirements, THE API SHALL return HTTP 400 with a descriptive validation error message.
4. THE API SHALL store all User passwords as BCrypt hashes and SHALL NOT store plain-text passwords.
5. WHEN a login request is received with a valid username and correct password, THE API SHALL return HTTP 200 with a signed JWT access token and the authenticated User's role.
6. WHEN a login request is received with an unrecognized username or an incorrect password, THE API SHALL return HTTP 401 with a generic error message that does not distinguish between the two failure reasons.
7. WHEN a request is received that includes a valid, non-expired JWT in the Authorization header, THE API SHALL authenticate the request and permit access to the requested resource if the User's role is authorized.
8. WHEN a request is received that includes an expired or malformed JWT, THE API SHALL return HTTP 401.
9. WHEN a request is received for a protected endpoint without an Authorization header, THE API SHALL return HTTP 401.
10. THE JWT SHALL have a configurable expiry duration driven by an environment variable and SHALL be signed with a secret key stored as an environment variable.
11. WHEN a logout action is initiated from the UI, THE UI SHALL discard the stored JWT and redirect the User to the login page.
12. THE API SHALL provide a protected user-management endpoint accessible only to authenticated ADMIN Users, which allows creation of new User accounts with either the ADMIN or STAFF role; this protected endpoint is the sole mechanism by which ADMIN accounts may be created, apart from an explicitly defined initial system bootstrap (e.g., a database seed or first-run setup mechanism).
13. THE API SHALL NOT expose password hashes or internal User identifiers in any API response DTO.

---

### Requirement 2: Role-Based Access Control

**User Story:** As a system owner, I want API endpoints and UI routes to be protected by role, so that STAFF cannot perform administrative actions.

#### Acceptance Criteria

1. THE API SHALL enforce role-based authorization on every protected endpoint, returning HTTP 403 when an authenticated User's role is insufficient.
2. WHERE the ADMIN role is required, THE API SHALL restrict the following operations to ADMIN Users: creating/deactivating Categories, creating/deactivating Suppliers, deleting or permanently removing any record, and accessing user-management endpoints.
3. WHILE a User is authenticated as STAFF, THE API SHALL permit the User to create Products, create Purchase Orders, record Stock Movements, and view all read-only endpoints.
4. THE UI SHALL hide navigation items and action buttons that are unavailable to the currently authenticated User's role.
5. THE UI SHALL enforce protected routes so that navigating directly to a restricted URL redirects an unauthorized User to an appropriate error or login page.

---

### Requirement 3: Product Management

**User Story:** As an ADMIN or STAFF User, I want to manage Products, so that the System maintains an accurate catalog of all stocked items.

#### Acceptance Criteria

1. WHEN a create-product request is received with a name, unique SKU, Category ID, purchase price, selling price, minimum stock level, maximum stock level, and unit of measure, THE API SHALL persist the Product with an initial current stock of zero and active status, and return HTTP 201 with the created Product DTO.
2. WHEN a create-product request is received with a SKU that already exists in the database, THE API SHALL return HTTP 409 with an error message stating that the SKU is already in use.
3. WHEN an update-product request is received for an existing Product ID with valid field values, THE API SHALL update the Product fields and return HTTP 200 with the updated Product DTO.
4. WHEN an update-product request is received with a SKU value that belongs to a different Product, THE API SHALL return HTTP 409.
5. WHEN a deactivate-product request is received for an existing Product ID, THE API SHALL set the Product's active status to false and return HTTP 200; THE API SHALL NOT delete the Product record.
6. WHEN a get-products request is received, THE API SHALL return a paginated list of Products with support for filtering by name (partial match), SKU (partial match), Category ID, and active status, and support for sorting by name, SKU, current stock, and creation date.
7. WHEN a get-product-by-id request is received for a non-existent Product ID, THE API SHALL return HTTP 404.
8. THE Product DTO returned by the API SHALL include: product ID, name, SKU, category name, description, purchase price, selling price, current stock, minimum stock level, maximum stock level, unit, and active status.
9. THE API SHALL NOT expose the internal database entity directly in any Product API response.
10. WHEN a create or update-product request is received with a negative purchase price, negative selling price, negative minimum stock level, or negative maximum stock level, THE API SHALL return HTTP 400 with field-level validation error messages.
11. WHEN a create or update-product request is received where the minimum stock level is greater than the maximum stock level, THE API SHALL return HTTP 400 with a descriptive validation error.

---

### Requirement 4: Category Management

**User Story:** As an ADMIN User, I want to manage Categories, so that Products can be organized and browsed by classification.

#### Acceptance Criteria

1. WHEN a create-category request is received by an ADMIN User with a unique category name and optional description, THE API SHALL persist the Category with active status and return HTTP 201 with the created Category DTO.
2. WHEN a create-category request is received with a category name that already exists (case-insensitive), THE API SHALL return HTTP 409.
3. WHEN an update-category request is received by an ADMIN User for an existing Category ID with a valid name, THE API SHALL update the Category and return HTTP 200 with the updated Category DTO.
4. WHEN a deactivate-category request is received by an ADMIN User for an existing Category ID, THE API SHALL set the Category's active status to false and return HTTP 200; THE API SHALL NOT delete the Category record.
5. WHEN a get-products-by-category request is received for a valid Category ID, THE API SHALL return a paginated list of active Products belonging to that Category.
6. WHEN a get-categories request is received, THE API SHALL return a list of all Categories with their name, description, active status, and product count.
7. WHEN a get-products-by-category request is received for a non-existent Category ID, THE API SHALL return HTTP 404.

---

### Requirement 5: Supplier Management

**User Story:** As an ADMIN User, I want to manage Suppliers, so that purchase orders can be linked to the correct vendor.

#### Acceptance Criteria

1. WHEN a create-supplier request is received by an ADMIN User with a supplier name, company name, email, phone number, and address, THE API SHALL persist the Supplier with active status and return HTTP 201 with the created Supplier DTO.
2. WHEN a create-supplier request is received with an email address that already exists in the Supplier table, THE API SHALL return HTTP 409.
3. WHEN an update-supplier request is received by an ADMIN User for an existing Supplier ID with valid field values, THE API SHALL update the Supplier and return HTTP 200 with the updated Supplier DTO.
4. WHEN a deactivate-supplier request is received by an ADMIN User for an existing Supplier ID, THE API SHALL set the Supplier's active status to false and return HTTP 200; THE API SHALL NOT delete the Supplier record.
5. WHEN a get-suppliers request is received, THE API SHALL return a paginated list of Suppliers with support for filtering by supplier name (partial match), company name (partial match), and active status.
6. WHEN a get-supplier-by-id request is received for a non-existent Supplier ID, THE API SHALL return HTTP 404.
7. THE Supplier DTO returned by the API SHALL include: supplier ID, supplier name, company name, email, phone, address, GST number, active status, and total purchase order count.
8. WHEN a create or update-supplier request is received with an invalid email format, THE API SHALL return HTTP 400.
9. WHEN a create or update-supplier request is received with a phone number that does not match the expected numeric format, THE API SHALL return HTTP 400.

---

### Requirement 6: Purchase Order Management

**User Story:** As an ADMIN or STAFF User, I want to create and manage Purchase Orders, so that the business can track procurement from Suppliers.

#### Acceptance Criteria

1. WHEN a create-purchase-order request is received with a valid Supplier ID, at least one Purchase_Order_Item (each containing a Product ID, quantity greater than zero, and purchase price greater than zero), and an expected delivery date on or after the order date, THE API SHALL persist the Purchase_Order with status DRAFT, auto-generate a unique purchase order number, calculate the subtotal for each Purchase_Order_Item (quantity × purchase price), calculate the total as the sum of all subtotals, and return HTTP 201 with the Purchase Order DTO.
2. WHEN a create-purchase-order request is received with a Supplier ID that does not exist or is inactive, THE API SHALL return HTTP 400.
3. WHEN a create-purchase-order request is received with a Product ID in any Purchase_Order_Item that does not exist or is inactive, THE API SHALL return HTTP 400.
4. WHEN a create-purchase-order request is received with an expected delivery date earlier than the order date, THE API SHALL return HTTP 400.
5. WHEN a create-purchase-order request is received with a duplicate Product ID across multiple Purchase_Order_Items, THE API SHALL return HTTP 400 with an error message indicating the duplicated Product.
6. WHEN an update-purchase-order-status request is received for a Purchase_Order in DRAFT status, THE API SHALL permit transition to ORDERED or CANCELLED only.
7. WHEN an update-purchase-order-status request is received for a Purchase_Order in ORDERED status, THE API SHALL permit transition to RECEIVED or CANCELLED only.
8. WHEN an update-purchase-order-status request is received to transition a Purchase_Order to RECEIVED, THE API SHALL execute the following steps within a single database transaction: for each Purchase_Order_Item, capture the Product's current stock as stock_before, increment the Product's current stock by the Purchase_Order_Item quantity to produce stock_after, and create one immutable Stock_Movement record with movement_type = PURCHASE, direction = ADD, quantity = the Purchase_Order_Item quantity (a positive integer), unit_cost = the Purchase_Order_Item purchase_price, stock_before = the captured pre-increment stock level, stock_after = the post-increment stock level, note = the purchase order number, timestamp = current UTC time, and acting_user = the authenticated User performing the transition; after all items are processed, set the Purchase_Order status to RECEIVED; if any step in the transaction fails, THE API SHALL roll back all stock increments, Stock_Movement inserts, and the status update, and return HTTP 500.
9. IF the database transaction described in criterion 8 fails for any reason, THEN THE API SHALL roll back all stock and Stock_Movement changes and return HTTP 500 with an error message.
10. WHEN an update-purchase-order-status request is received to transition a Purchase_Order to CANCELLED, THE API SHALL set the status to CANCELLED without modifying any stock levels.
11. WHEN an update-purchase-order-status request is received for an invalid transition (e.g., RECEIVED → ORDERED), THE API SHALL return HTTP 400 with a message describing the invalid transition.
12. WHEN a get-purchase-orders request is received, THE API SHALL return a paginated list of Purchase Orders with support for filtering by Supplier ID, status, order date range, and purchase order number (partial match), and support for sorting by order date and total amount.
13. WHEN a get-purchase-order-by-id request is received for a non-existent Purchase Order ID, THE API SHALL return HTTP 404.
14. THE Purchase Order DTO returned by the API SHALL include: purchase order ID, purchase order number, supplier name, order date, expected delivery date, status, list of Purchase_Order_Items (each with product name, SKU, quantity, purchase price, subtotal), and total amount.
15. WHEN a get-purchase-order-items request is received for a valid Purchase Order ID, THE API SHALL return the full list of Purchase_Order_Items for that order.

---

### Requirement 7: Inventory Stock Movement

**User Story:** As an ADMIN or STAFF User, I want every stock change to be recorded as a Stock_Movement, so that the business maintains a complete, auditable inventory history.

#### Acceptance Criteria

1. WHEN a record-stock-movement request is received with a valid Product ID, Movement_Type (PURCHASE, SALE, DAMAGED, or ADJUSTMENT), a positive integer quantity (1–1,000,000), an optional note of at most 500 characters, and — when Movement_Type is ADJUSTMENT — an explicit direction of ADD or REDUCE, THE API SHALL, within a single database transaction: derive the signed stock delta (PURCHASE → +quantity; SALE → −quantity; DAMAGED → −quantity; ADJUSTMENT ADD → +quantity; ADJUSTMENT REDUCE → −quantity), capture the Product's current stock as stock_before, apply the delta to yield stock_after, reject the operation with HTTP 400 if stock_after would be negative, create an immutable Stock_Movement record containing movement_type, direction (ADD for PURCHASE and ADJUSTMENT ADD; REDUCE for SALE, DAMAGED, and ADJUSTMENT REDUCE), quantity, unit_cost = the Product's current purchase_price at the time of the movement, stock_before, stock_after, timestamp in UTC, acting User ID, and note, update the Product's current stock to stock_after, and return HTTP 201 with the Stock_Movement DTO.
2. IF a record-stock-movement transaction would reduce a Product's current stock below zero, THEN THE API SHALL return HTTP 400 with a message stating the resulting stock would be negative, and SHALL NOT persist any changes.
3. IF the database transaction in criterion 1 fails for any reason, THEN THE API SHALL roll back all changes and return HTTP 500.
4. THE API SHALL NOT permit direct modification of a Product's current stock field outside of the stock movement transaction described in criterion 1 and the purchase order receipt transaction described in Requirement 6 criterion 8.
5. WHEN a get-stock-movements request is received, THE API SHALL return a paginated list of Stock_Movement records with support for filtering by Product ID, Movement_Type, date range, and acting User ID, and support for sorting by timestamp descending by default.
6. THE Stock_Movement record SHALL be immutable after creation; THE API SHALL NOT provide an endpoint to update or delete Stock_Movement records.
7. THE Stock_Movement DTO SHALL include: movement ID, product name, SKU, movement_type, direction (ADD or REDUCE), quantity (a positive integer), unit_cost (the Product's purchase_price captured at the time of movement), stock_before (stock level immediately before the movement), stock_after (stock level immediately after the movement), timestamp (UTC), acting username, and note.
8. WHEN a record-stock-movement request is received with a quantity that is not a positive integer, is zero, or exceeds 1,000,000, THE API SHALL return HTTP 400 with a descriptive error message and SHALL NOT persist any changes.
9. WHEN a record-stock-movement request is received with Movement_Type ADJUSTMENT and no direction field (ADD or REDUCE) is provided, THE API SHALL return HTTP 400 with a message stating that direction is required for ADJUSTMENT movements.

---

### Requirement 8: Low Stock Management

**User Story:** As an ADMIN or STAFF User, I want to see which Products are low on stock or out of stock, so that the business can reorder before stockouts occur.

#### Acceptance Criteria

1. THE API SHALL define a Product as Low_Stock_Alert when the Product's current stock is greater than zero and less than or equal to the Product's minimum stock level.
2. THE API SHALL define a Product as Out_Of_Stock when the Product's current stock equals zero.
3. WHEN a get-low-stock request is received, THE API SHALL return a paginated list of active Products whose current stock is less than or equal to their minimum stock level, including Out_Of_Stock Products.
4. WHEN a get-out-of-stock request is received, THE API SHALL return a paginated list of active Products whose current stock equals zero.
5. WHEN a get-low-stock-summary request is received, THE API SHALL return an aggregate count of Low_Stock_Alert Products, Out_Of_Stock Products, and the list of up to 10 most critically low Products ordered as follows: Products whose minimum stock level is greater than zero SHALL be sorted by the ratio (current stock ÷ minimum stock level) ascending; Products whose minimum stock level is zero SHALL be treated as having a criticality ratio of zero and placed after all ratio-ranked Products, sorted among themselves by product name ascending; ties in ratio SHALL be broken by product name ascending.
6. WHEN a get-low-stock request is received with a stock-status filter of LOW_STOCK, THE API SHALL return only Products meeting the Low_Stock_Alert definition.
7. WHEN a get-low-stock request is received with a stock-status filter of OUT_OF_STOCK, THE API SHALL return only Products meeting the Out_Of_Stock definition.
8. THE API SHALL include the fields product name, SKU, category name, current stock, minimum stock level, and stock status (LOW_STOCK or OUT_OF_STOCK) in every low-stock response item.

---

### Requirement 9: Dashboard

**User Story:** As an ADMIN or STAFF User, I want a Dashboard that summarizes key business metrics and recent activity, so that I can quickly assess the state of the inventory and operations.

#### Acceptance Criteria

1. WHEN a get-dashboard-summary request is received, THE API SHALL return the following aggregate metrics computed from live database data: total active product count, total active supplier count, total inventory value (sum of current stock × purchase price for all active Products), total purchase order count, count of Purchase Orders with status ORDERED, count of Low_Stock_Alert Products, and count of Out_Of_Stock Products.
2. WHEN a get-recent-stock-movements request is received, THE API SHALL return the 10 most recent Stock_Movement records ordered by timestamp descending.
3. WHEN a get-purchase-trend request is received with a date range, THE API SHALL return aggregated Purchase Order totals grouped by day within the requested range, including only Purchase Orders with status ORDERED or RECEIVED.
4. WHEN a get-inventory-trend request is received with a start date and end date, THE API SHALL first compute the opening inventory value as the net sum of (quantity × unit_cost) for all Stock_Movement records with timestamp strictly before the start date — where PURCHASE and ADJUSTMENT ADD movements add (quantity × unit_cost) and SALE, DAMAGED, and ADJUSTMENT REDUCE movements subtract (quantity × unit_cost) — and then, for each day within the requested date range (inclusive), apply the net delta of that day's Stock_Movement records using each movement's recorded unit_cost (not the Product's current purchase_price) to produce a cumulative daily inventory value; the response SHALL contain one entry per day in the range with the date and the cumulative inventory value at end of that day.
5. THE UI Dashboard SHALL display all metrics returned by the get-dashboard-summary endpoint as individual KPI cards.
6. THE UI Dashboard SHALL display a low-stock alert panel listing Products in Low_Stock_Alert or Out_Of_Stock status.
7. THE UI Dashboard SHALL display a purchase trend chart and an inventory trend chart using Recharts.
8. THE UI Dashboard SHALL display the recent Stock_Movement table.

---

### Requirement 10: Reports

**User Story:** As an ADMIN or STAFF User, I want to generate business reports filtered by date, so that I can analyze procurement, inventory, and supplier performance.

#### Acceptance Criteria

1. WHEN a get-purchase-report request is received with a start date and end date, THE API SHALL return a summary of all Purchase Orders within the date range including: total order count, total amount, breakdown by status, and a list of Purchase Orders with order number, supplier name, order date, status, and total amount.
2. WHEN an get-inventory-report request is received with a start date and end date, THE API SHALL return a list of active Products with their current stock, minimum stock level, maximum stock level, total purchase quantity received in the date range, and total sale/damaged/adjustment quantity in the date range.
3. WHEN a get-supplier-purchase-report request is received with a start date and end date, THE API SHALL return a list of Suppliers with the count and total value of their Purchase Orders within the date range, ordered by total value descending.
4. WHEN a get-stock-movement-report request is received with a start date, end date, and optional Movement_Type filter, THE API SHALL return a list of Stock_Movement records within the date range matching the filter, including total quantity moved per Movement_Type.
5. WHEN a get-low-stock-report request is received, THE API SHALL return the current list of all active Products in Low_Stock_Alert or Out_Of_Stock status, including their recommended reorder quantity (calculated as maximum stock level minus current stock level).
6. WHEN any report endpoint is called with a start date that is after the end date, THE API SHALL return HTTP 400.
7. THE API SHALL structure all report responses as DTOs suitable for direct rendering in a table or chart on the UI without further transformation.

---

### Requirement 11: AI Inventory Assistant

**User Story:** As an ADMIN or STAFF User, I want an AI assistant that answers natural-language questions about inventory and provides recommendations, so that I can make more informed purchasing decisions.

#### Acceptance Criteria

1. THE AI_Assistant module SHALL be implemented as an isolated Service/module within the backend, accessed only through a dedicated AI Controller, and SHALL NOT share business logic with any other Service.
2. THE AI_Assistant SHALL NOT directly create, update, or delete any Product, Stock_Movement, Purchase_Order, or Supplier record.
3. WHEN an ai-query request is received with a natural-language question, THE AI_Assistant SHALL retrieve the relevant inventory summary data from the database, construct a prompt containing that data and the user's question, call the OpenAI API, and return the response text to the UI.
4. WHEN the AI_Assistant returns a recommendation that implies a business action (e.g., "reorder 50 units of Product X"), THE UI SHALL display the recommendation with a clear label indicating it is a suggestion requiring User confirmation before any business action is taken.
5. THE AI_Assistant SHALL support the following query types: reorder quantity recommendation for a specific Product, demand trend analysis for a date range, overall inventory health summary, explanation of why a specific Product is low on stock, and purchase trend summary.
6. WHEN the OpenAI API call fails or times out, THE API SHALL return HTTP 503 with a message indicating the AI service is temporarily unavailable, without exposing the raw OpenAI error.
7. THE OpenAI API key SHALL be stored as an environment variable and SHALL NOT be hardcoded in source code or committed to version control.
8. THE AI_Assistant module SHALL be designed so that the underlying AI provider can be replaced by modifying only the AI Service implementation, without changes to the AI Controller or any other module.
9. WHEN an ai-query request is received from a User who is not authenticated, THE API SHALL return HTTP 401.

---

### Requirement 12: API Response Consistency and Error Handling

**User Story:** As a frontend developer, I want all API responses to follow a consistent structure and all errors to be descriptive, so that the UI can handle responses uniformly.

#### Acceptance Criteria

1. THE API SHALL wrap all successful responses in a consistent envelope DTO containing at minimum: a success flag (true), an HTTP status code, a message string, and the data payload.
2. THE API SHALL wrap all error responses in a consistent envelope DTO containing at minimum: a success flag (false), an HTTP status code, a message string describing the error, and a timestamp.
3. WHEN a validation error occurs, THE API SHALL return HTTP 400 with the error envelope containing a field-level error map listing each invalid field and its violation message.
4. THE API SHALL implement a global exception handler that intercepts all unhandled exceptions and returns a structured error response without exposing stack traces or internal implementation details.
5. WHEN a resource is not found, THE API SHALL return HTTP 404 with a message identifying the resource type and the requested identifier.
6. WHEN an operation is forbidden due to insufficient role, THE API SHALL return HTTP 403 with a message identifying the required role.
7. THE API SHALL return HTTP 405 for requests made to a valid URL with an unsupported HTTP method.

---

### Requirement 13: Pagination, Search, Filtering, and Sorting

**User Story:** As a User, I want list endpoints to support pagination, search, filtering, and sorting, so that I can efficiently navigate large data sets.

#### Acceptance Criteria

1. THE API SHALL support pagination on all list endpoints using page number (zero-indexed), page size, and a configurable default page size of 20.
2. THE API SHALL return pagination metadata in every paginated response including: current page number, page size, total elements, total pages, and whether the current page is the first or last.
3. WHEN a search parameter is provided to a list endpoint, THE API SHALL perform case-insensitive partial matching on the designated searchable fields for that resource.
4. WHEN a filter parameter is provided to a list endpoint, THE API SHALL apply exact matching on the designated filterable fields for that resource.
5. WHEN a sort parameter is provided to a list endpoint, THE API SHALL sort results by the specified field in the specified direction (ASC or DESC); THE API SHALL apply a deterministic default sort when no sort parameter is provided.
6. WHEN a page number greater than the total number of pages is requested, THE API SHALL return an empty data list with accurate pagination metadata rather than an error.

---

### Requirement 14: Data Validation

**User Story:** As a developer, I want all incoming API request data to be validated before processing, so that invalid data never reaches the database.

#### Acceptance Criteria

1. THE API SHALL validate all incoming request bodies using Bean Validation annotations before the Service layer is invoked.
2. WHEN any required field in a request body is missing or blank, THE API SHALL return HTTP 400 with a field-level error message for each missing field.
3. WHEN a numeric field in a request body contains a non-numeric value, THE API SHALL return HTTP 400 with a descriptive type-mismatch error.
4. WHEN a date field in a request body contains an improperly formatted date string, THE API SHALL return HTTP 400 with a descriptive format error.
5. THE API SHALL validate that all foreign key references (Category ID, Supplier ID, Product ID) in request bodies correspond to existing records before processing, returning HTTP 400 or HTTP 404 as appropriate.

---

### Requirement 15: Security

**User Story:** As a system administrator, I want the application to follow security best practices, so that sensitive data and business operations are protected.

#### Acceptance Criteria

1. THE API SHALL hash all passwords using BCrypt before persisting them and SHALL never log or return password values.
2. THE API SHALL sign all JWTs with an HS256 or stronger algorithm using a secret key of at least 256 bits stored in an environment variable.
3. THE API SHALL validate the JWT signature and expiry on every protected request without querying the database for the token.
4. THE API SHALL configure CORS to allow requests only from the configured frontend origin, set via an environment variable.
5. THE API SHALL NOT include sensitive fields (password hash, internal system metadata) in any response DTO.
6. THE UI SHALL store the JWT in memory or in httpOnly cookies and SHALL NOT store the JWT in localStorage.
7. ALL environment-specific secrets (database password, JWT secret, OpenAI API key, frontend origin) SHALL be supplied via environment variables and SHALL NOT appear in source code or version-controlled configuration files.
8. THE API SHALL apply Spring Security's CSRF protection policy appropriate for a stateless JWT-based API (disabled for REST endpoints, session creation set to STATELESS).

---

### Requirement 16: Frontend User Interface

**User Story:** As a User, I want a responsive, professional web interface, so that I can perform all inventory management tasks efficiently on any device.

#### Acceptance Criteria

1. THE UI SHALL implement a persistent sidebar navigation that adapts its menu items based on the authenticated User's role.
2. THE UI SHALL implement separate route layouts for ADMIN and STAFF Users, redirecting unauthorized navigation attempts to a 403 or login page.
3. THE UI SHALL display a loading state indicator whenever an API request is in flight.
4. THE UI SHALL display a meaningful empty state message when a list or table returns zero results.
5. WHEN an API request returns an error, THE UI SHALL display an in-page error message or toast notification with the error message from the API response.
6. WHEN an API request completes successfully for a create, update, or status-change operation, THE UI SHALL display a success toast notification.
7. WHEN a User initiates a destructive operation (deactivation, cancellation, status change to CANCELLED), THE UI SHALL display a confirmation dialog requiring explicit User confirmation before submitting the request.
8. THE UI SHALL implement all data tables with server-side pagination, search, filtering, and sorting controls that map to the corresponding API query parameters.
9. THE UI SHALL implement form validation on all input forms, displaying field-level error messages for invalid or missing inputs before submitting to the API.
10. THE UI SHALL be responsive and render correctly on screen widths from 375px (mobile) to 1920px (desktop).
11. THE UI SHALL use Recharts to render the purchase trend and inventory trend charts on the Dashboard.
12. THE UI SHALL use React Router for client-side routing and SHALL implement route guards that redirect unauthenticated Users to the login page.
13. WHEN the UI sends any authenticated request, THE UI SHALL include the JWT in the Authorization header as a Bearer token using Axios interceptors.

---

### Requirement 17: Backend Architecture and Code Quality

**User Story:** As a developer, I want the backend to follow a clean, layered architecture, so that the codebase is maintainable and testable.

#### Acceptance Criteria

1. THE API SHALL implement a three-layer architecture: Controller (HTTP handling), Service (business logic), and Repository (data access), with no business logic in the Controller layer and no database queries in the Service layer.
2. THE API SHALL use DTOs for all request and response payloads and SHALL NOT expose JPA entity objects directly in Controller method signatures or response bodies.
3. THE API SHALL define all entity relationships using JPA/Hibernate annotations with appropriate cascade types and fetch strategies to avoid N+1 query problems on list endpoints.
4. THE API SHALL use Spring Data JPA Repository interfaces for all database access and SHALL NOT use raw JDBC queries except where JPA is demonstrably insufficient.
5. THE API SHALL annotate all Service methods that modify database state with @Transactional to ensure atomicity.
6. THE API project SHALL be structured into logical packages: controller, service, repository, entity, dto, exception, security, and config.
7. THE API SHALL NOT hardcode any configurable value (database URL, credentials, JWT secret, token expiry, OpenAI key, CORS origin) in source code; all SHALL be externalized to application.properties or environment variables.

---

### Requirement 18: Database Design

**User Story:** As a developer, I want the database schema to accurately model all business entities and their relationships, so that data integrity is enforced at the persistence layer.

#### Acceptance Criteria

1. THE database SHALL enforce unique constraints on: User.username, User.email, Product.sku, Category.name, and Supplier.email.
2. THE database SHALL enforce foreign key constraints between: Product → Category, Purchase_Order → Supplier, Purchase_Order_Item → Purchase_Order, Purchase_Order_Item → Product, Stock_Movement → Product, and Stock_Movement → User.
3. THE database SHALL store all monetary values — including Product.purchase_price, Product.selling_price, Purchase_Order_Item.purchase_price, Purchase_Order_Item.subtotal, Purchase_Order.total, and Stock_Movement.unit_cost — as DECIMAL(12,2) to avoid floating-point precision errors and to accommodate values up to 999,999,999.99.
4. THE database SHALL store all timestamps in UTC using an appropriate datetime type.
5. THE database schema SHALL be managed via Hibernate DDL generation in development and via migration scripts in production-ready configuration.
6. THE Stock_Movement table SHALL include the following non-nullable columns: stock_before INT (the Product's stock level immediately before the movement), stock_after INT (the Product's stock level immediately after the movement), and unit_cost DECIMAL(12,2) (the Product's purchase_price at the time the movement was recorded); these columns enable accurate point-in-time inventory value reconstruction without referencing current Product data.
