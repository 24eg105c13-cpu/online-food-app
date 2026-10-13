# GourmetBite — Online Food Ordering System

A production-ready, full-stack web application built with **Java 17**, **Spring Boot**, **Spring Security**, **JWT (JSON Web Token)**, **MongoDB**, and modern **HTML5 / CSS3 / Vanilla JavaScript**.

Designed specifically for college final-year capstone projects, academic vivas, and live software demonstrations.

---

## 1. Project Abstract

The **Online Food Ordering System (GourmetBite)** is an end-to-end web platform engineered to modernize the restaurant dining and food delivery experience. 

Customers can seamlessly browse chef-curated food items, perform real-time searches, filter by dietary categories, inspect ingredients, add items to a dynamic shopping cart, place orders with delivery address details, choose **Cash on Delivery (COD)**, and track their order status through a live multi-step timeline.

Administrators have access to a secure, role-restricted dashboard with real-time operational statistics (total orders, gross revenue, registered customers, catalog inventory, and status breakdown), catalog CRUD management (food items and categories with price/availability controls), registered customer directory, and order fulfillment workflows with strict status-transition validations.

---

## 2. Technology Stack

### Backend
- **Language**: Java 17 (tested & compatible with Java 17 - Java 26 LTS runtimes)
- **Framework**: Spring Boot 4.x / 3.x
- **Build Tool**: Apache Maven (includes cross-platform wrapper `mvnw` and `mvnw.cmd`)
- **Web**: Spring Web (RESTful APIs with Spring MVC)
- **Database Access**: Spring Data MongoDB
- **Security**: Spring Security (Stateless filter chain with BCrypt password hashing)
- **Token Security**: JJWT 0.12.6 (HMAC-SHA256 tokens)
- **Validation**: Spring Validation / Jakarta Validation API (`@Valid`, `@NotBlank`, `@Email`, `@Positive`, etc.)
- **Code Generation**: Project Lombok
- **JSON Serialization**: Jackson with Java 8 / JavaTimeModule (`Instant` support)

### Database
- **Engine**: MongoDB (Document-oriented NoSQL)
- **Deployments Supported**: 
  - Local MongoDB instance (`mongodb://localhost:27017/food_ordering_db`)
  - MongoDB Atlas Cloud Database (`mongodb+srv://...`)
  - Dockerized MongoDB container

### Frontend
- **Languages**: HTML5, CSS3, Vanilla JavaScript (ES6+)
- **HTTP Client**: Native Fetch API
- **Design System**: Warm cream aesthetic (`#fdfbf7`), charcoal typography (`#1e242b`), coral/orange primary accents (`#ff5722`), responsive grid layouts, card elevations, animated toasts, and modal dialogs.
- **Frameworks**: 100% Vanilla (No React, Vue, or Angular dependencies required to run).

### Testing
- **Framework**: JUnit 5, Mockito, Spring Boot Test, Spring Security Test, MockMvc
- **Test Coverage**: 36 comprehensive automated test cases testing registration, login, password hashing, JWT authorization, food catalog CRUD, search/filtering, cart operations, server-side pricing snapshots, empty-cart checkout rejection, customer data isolation, and admin status transition state machines.

---

## 3. System Architecture & Design Highlights

```
+-------------------------------------------------------------------------+
|                         Frontend Web Browser                            |
|  (HTML5 / CSS3 / Vanilla JS / Fetch API / LocalStorage JWT Client)      |
+-------------------------------------------------------------------------+
                                     │  HTTP REST (JSON)
                                     ▼
+─────────────────────────────────────────────────────────────────────────+
|                          Spring Security Filter                         |
|  • JwtAuthenticationFilter (Extracts Bearer Token & Validates Claims)   |
|  • BCryptPasswordEncoder (Salted one-way hashing)                       |
|  • JwtAuthenticationEntryPoint & AccessDeniedHandler (JSON 401 & 403)   |
+─────────────────────────────────────────────────────────────────────────+
                                     │
                                     ▼
+─────────────────────────────────────────────────────────────────────────+
|                             REST Controllers                            |
|  • AuthController        • FoodItemController   • CategoryController    |
|  • CartController        • OrderController      • UserController        |
|  • AdminController       • HealthController                             |
+─────────────────────────────────────────────────────────────────────────+
                                     │
                                     ▼
+─────────────────────────────────────────────────────────────────────────+
|                           Business Service Layer                        |
|  • AuthService           • FoodItemService      • CategoryService       |
|  • CartService           • OrderService         • UserService           |
|  • AdminService          • SecurityUtils                                |
+─────────────────────────────────────────────────────────────────────────+
                                     │
                                     ▼
+─────────────────────────────────────────────────────────────────────────+
|                        Data Repositories (Spring Data)                   |
|  • UserRepository        • FoodItemRepository   • CategoryRepository    |
|  • CartRepository        • OrderRepository                              |
+─────────────────────────────────────────────────────────────────────────+
                                     │
                                     ▼
+─────────────────────────────────────────────────────────────────────────+
|                               MongoDB                                   |
|  Collections: users, food_items, categories, carts, orders              |
+─────────────────────────────────────────────────────────────────────────+
```

### Key Architectural Safeguards:
1. **Server-Side Price Authority**: The frontend never controls or sends order total amounts. All line totals and final order sums are calculated server-side from active database records.
2. **Immutable Price Snapshots**: Historical order items record unit price and line totals at checkout time. Future price changes in the food catalog will never alter past orders.
3. **Customer Isolation**: Non-admin customers can only view and mutate their own cart and orders (`SecurityUtils.getCurrentUserId()`).
4. **Finite State Machine for Orders**: Order statuses follow strict linear transitions (`PLACED` -> `CONFIRMED` -> `PREPARING` -> `OUT_FOR_DELIVERY` -> `DELIVERED`). Illegal state transitions are rejected with HTTP 400.
5. **No Public Admin Registration**: Public sign-ups are strictly forced to `ROLE_CUSTOMER`. Admin accounts can only be bootstrapped via protected environment variables at server boot.

---

## 4. Directory Structure

```
online-food-ordering/
├── .mvn/wrapper/                  # Maven wrapper binaries and properties
├── src/
│   ├── main/
│   │   ├── java/com/example/foodordering/
│   │   │   ├── config/            # SecurityConfig, JacksonConfig, DataInitializers
│   │   │   ├── controller/        # REST API Controllers
│   │   │   ├── dto/               # Request & Response Data Transfer Objects
│   │   │   ├── exception/         # Domain Exceptions & GlobalExceptionHandler
│   │   │   ├── model/             # MongoDB Document Models and Enums
│   │   │   ├── repository/        # Spring Data MongoDB Repositories
│   │   │   ├── security/          # JwtUtil, CustomUserDetails, Filters
│   │   │   ├── service/           # Business Logic Services
│   │   │   ├── util/              # SecurityUtils helper
│   │   │   └── FoodOrderingApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/            # Frontend Web UI
│   │           ├── index.html               # Home storefront
│   │           ├── login.html               # Customer login
│   │           ├── register.html            # Customer registration
│   │           ├── menu.html                # Menu with search, sort, filter
│   │           ├── food-details.html        # Single food item view
│   │           ├── cart.html                # Shopping cart
│   │           ├── checkout.html            # Checkout & delivery details
│   │           ├── order-confirmation.html  # Order receipt
│   │           ├── my-orders.html           # Customer order history
│   │           ├── order-tracking.html      # Visual timeline tracker
│   │           ├── profile.html             # Profile management
│   │           ├── admin-login.html         # Staff/Admin login
│   │           ├── admin-dashboard.html     # Real-time metrics
│   │           ├── manage-food.html         # Food CRUD & availability
│   │           ├── manage-categories.html   # Category CRUD
│   │           ├── manage-customers.html    # User directory
│   │           ├── manage-orders.html       # Order status updater
│   │           ├── css/style.css            # Responsive stylesheets
│   │           └── js/api.js                # Unified API client & state
│   └── test/
│       └── java/com/example/foodordering/
│           ├── controller/                  # MockMvc controller tests
│           ├── service/                     # Mockito unit tests
│           └── FoodOrderingApplicationTests.java
├── .env.example                   # Environment configuration template
├── .gitignore
├── mvnw & mvnw.cmd               # Maven wrappers for Linux/macOS and Windows
├── pom.xml                        # Maven dependencies & build configuration
└── README.md
```

---

## 5. Environment Variables & Configuration

Copy `.env.example` to `.env` or set environment variables in your terminal / cloud deployment:

| Variable | Description | Default (Local Dev) |
|---|---|---|
| `PORT` | HTTP Web Server Port | `8080` |
| `MONGODB_URI` | MongoDB Connection URI | `mongodb://localhost:27017/food_ordering_db` |
| `JWT_SECRET` | 256-bit HMAC Secret Key | Built-in fallback for local testing |
| `JWT_EXPIRATION_MS` | Token Expiry in Milliseconds | `86400000` (24 Hours) |
| `ADMIN_EMAIL` | Initial Admin Account Email | *(Empty)* |
| `ADMIN_PASSWORD` | Initial Admin Account Password | *(Empty)* |

---

## 6. How to Set Up and Run

### Prerequisites
1. **Java JDK 17 or higher** installed. Verify with `java -version`.
2. **MongoDB** installed locally, OR a free **MongoDB Atlas** connection string.

### Option A: Using Local MongoDB
1. Start your local MongoDB server:
   ```bash
   # Linux / macOS
   mongod --dbpath /path/to/data
   
   # Windows
   net start MongoDB
   ```
2. Run the application:
   ```bash
   # Windows Powershell / CMD:
   .\mvnw.cmd spring-boot:run

   # Linux / macOS:
   ./mvnw spring-boot:run
   ```

### Option B: Using MongoDB Atlas (Cloud)
1. Log in to [MongoDB Atlas](https://www.mongodb.com/atlas).
2. Create a free shared cluster (M0).
3. Under **Database Access**, create a database user with Read and Write privileges.
4. Under **Network Access**, add `0.0.0.0/0` (Allow Access from Anywhere).
5. Obtain the connection string:
   ```
   mongodb+srv://<username>:<password>@cluster0.mongodb.net/food_ordering_db?retryWrites=true&w=majority
   ```
6. Run the application with the environment variable:
   ```bash
   # Windows PowerShell:
   $env:MONGODB_URI="mongodb+srv://<user>:<password>@cluster0.mongodb.net/food_ordering_db?retryWrites=true&w=majority"
   $env:ADMIN_EMAIL="admin@foodordering.com"
   $env:ADMIN_PASSWORD="AdminPassword123!"
   .\mvnw.cmd spring-boot:run
   ```

---

## 7. Web Application Pages (Frontend URLs)

Once started, open your web browser at `http://localhost:8080`:

| Page | URL | Purpose |
|---|---|---|
| **Home Storefront** | `http://localhost:8080/index.html` | Hero, search, category pills, featured dishes, and benefits |
| **Food Menu** | `http://localhost:8080/menu.html` | Full menu with search, category filters, sorting, and pagination |
| **Dish Details** | `http://localhost:8080/food-details.html?id=<id>` | Image, price, description, quantity selector, add to cart |
| **Shopping Cart** | `http://localhost:8080/cart.html` | Review cart items, quantity +/- stepper, subtotal, checkout link |
| **Checkout** | `http://localhost:8080/checkout.html` | Delivery address form, Cash on Delivery option, order summary |
| **Confirmation** | `http://localhost:8080/order-confirmation.html?id=<id>` | Order receipt with summary and live tracking link |
| **My Orders** | `http://localhost:8080/my-orders.html` | Past order history with timestamps, status badges, totals |
| **Order Tracking** | `http://localhost:8080/order-tracking.html?id=<id>` | Live 5-step visual timeline tracking order fulfillment |
| **Customer Profile** | `http://localhost:8080/profile.html` | View/edit delivery address, phone number, and profile |
| **Customer Login** | `http://localhost:8080/login.html` | Customer sign-in |
| **Customer Sign Up**| `http://localhost:8080/register.html` | New customer registration |
| **Admin Login** | `http://localhost:8080/admin-login.html` | Dedicated admin & staff authentication |
| **Admin Dashboard** | `http://localhost:8080/admin-dashboard.html` | Key metrics, revenue, order distribution, recent activity |
| **Manage Foods** | `http://localhost:8080/manage-food.html` | Add/Edit/Delete dishes, upload image URL, toggle availability |
| **Manage Categories**| `http://localhost:8080/manage-categories.html`| Add/Edit categories, enable/disable from menu |
| **Manage Orders** | `http://localhost:8080/manage-orders.html` | View order snapshots, advance order status (FSM transitions) |
| **Manage Customers**| `http://localhost:8080/manage-customers.html`| Directory of registered customers and contact records |

---

## 8. REST API Reference

All APIs return consistent JSON payloads:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Authentication Endpoints
- `POST /api/auth/register` — Register a customer account
- `POST /api/auth/login` — Authenticate and receive a JWT token

### Food Item Endpoints
- `GET /api/foods` — List foods (params: `search`, `categoryId`, `available`, `page`, `size`, `sortBy`, `direction`)
- `GET /api/foods/{id}` — Get single food item
- `POST /api/foods` — **[ADMIN]** Create a new food item
- `PUT /api/foods/{id}` — **[ADMIN]** Update an existing food item
- `DELETE /api/foods/{id}` — **[ADMIN]** Delete a food item

### Category Endpoints
- `GET /api/categories` — List all categories (param: `activeOnly`)
- `GET /api/categories/{id}` — Get single category
- `POST /api/categories` — **[ADMIN]** Create a new category
- `PUT /api/categories/{id}` — **[ADMIN]** Update category
- `DELETE /api/categories/{id}` — **[ADMIN]** Delete category

### Shopping Cart Endpoints (Authenticated Customer)
- `GET /api/cart` — Get current customer's cart with computed totals
- `POST /api/cart/add` — Add food item and quantity to cart
- `PUT /api/cart/{itemId}` — Update quantity (quantity `0` removes item)
- `DELETE /api/cart/{itemId}` — Remove specific item from cart
- `DELETE /api/cart/clear` — Clear all items in cart

### Orders Endpoints (Authenticated Customer & Admin)
- `POST /api/orders` — Checkout cart, create order snapshot, and clear cart
- `GET /api/orders` — Get current customer's order history (or all orders if admin)
- `GET /api/orders/{id}` — Get single order details (ownership verified)
- `PUT /api/orders/{id}/status` — **[ADMIN]** Update status with valid state transitions

### User Profile Endpoints
- `GET /api/users/profile` — Get authenticated user profile
- `PUT /api/users/profile` — Update user profile details

### Admin Operations Endpoints
- `GET /api/admin/dashboard` — **[ADMIN]** Get revenue, order, and inventory analytics
- `GET /api/admin/orders` — **[ADMIN]** Get paginated list of all orders
- `GET /api/admin/users` — **[ADMIN]** Get list of registered customers

### System Health
- `GET /api/health` — System health check (`{"status": "UP"}`)

---

## 9. Testing & Build Verification

The application includes automated tests across unit, integration, and security layers:

```bash
# Run automated tests
.\mvnw.cmd test

# Build executable production JAR
.\mvnw.cmd package
```

### Test Suite Summary:
- **`AuthServiceTest`**: Validates registration, duplicate email rejection, BCrypt hashing, customer role enforcement, and login credential checks.
- **`FoodItemServiceTest`**: Validates category lookups, positive price constraints, food updates, deletions, and filtered pagination.
- **`CartServiceTest`**: Validates cart creation, unavailable food rejection, quantity updating/removal, and server-side pricing.
- **`OrderServiceTest`**: Validates empty-cart rejection, price snapshotting, cart auto-clearing, customer data isolation, and allowed status transitions.
- **`AuthControllerTest`**: Validates MockMvc JSON endpoints and `@Valid` field validation failures.
- **`FoodItemControllerTest`**: Validates MockMvc food endpoints, pagination response schema, and 404 handling.
- **`OrderControllerTest`**: Validates authenticated MockMvc checkout and status transitions.
- **`FoodOrderingApplicationTests`**: Validates full Spring application context initialization.

---

## 10. Deployment Guide (Render / Cloud / Docker)

### Deploying to Render
1. Push your repository to GitHub.
2. In [Render Dashboard](https://dashboard.render.com), create a new **Web Service**.
3. Choose **Java** environment or Docker.
4. Set Build Command:
   ```bash
   ./mvnw clean package -DskipTests
   ```
5. Set Start Command:
   ```bash
   java -Dserver.port=$PORT -jar target/food-ordering-0.0.1-SNAPSHOT.jar
   ```
6. Add Environment Variables:
   - `MONGODB_URI`: Your MongoDB Atlas URI
   - `JWT_SECRET`: A secure 256-bit random key
   - `ADMIN_EMAIL`: `admin@foodordering.com`
   - `ADMIN_PASSWORD`: Your chosen admin password

---

## 11. Viva & Final-Year Presentation Tips

### Common Viva Questions & Answers:
1. **Why MongoDB instead of MySQL for this project?**
   * *Answer:* Food menus frequently vary in structure (custom ingredients, category tags, allergens, snapshot items). MongoDB's document model allows flexible representation and natural nesting of cart items and order line items within single atomic document writes without requiring multi-table joins.
2. **How do you ensure customers cannot alter their order prices in the browser?**
   * *Answer:* The frontend never supplies prices to `/api/orders`. The backend fetches each food item directly from MongoDB, verifies availability, and calculates line totals and grand totals using `BigDecimal`.
3. **How is stateless authentication achieved?**
   * *Answer:* The backend issues signed HMAC-SHA256 JWT tokens upon login. Every subsequent request transmits this token in the `Authorization: Bearer <token>` HTTP header. `JwtAuthenticationFilter` validates the signature and extracts claims without maintaining server-side session state in memory.
4. **How are administrative endpoints secured?**
   * *Answer:* Spring Security matches URLs in `SecurityConfig`. Mutation endpoints (`POST /api/foods`, `PUT /api/orders/*/status`, etc.) explicitly enforce `.hasRole("ADMIN")`. Attempted access by a customer returns HTTP 403 Forbidden.

---

## 12. License
Academic Open Source Project. Designed and developed for final-year engineering / computer science capstone projects.
