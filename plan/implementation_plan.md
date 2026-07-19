# Implementation Plan - PatlaTarLagna

Building a production-ready, enterprise-level full-stack Matrimonial Matchmaking Platform (**PatlaTarLagna**) using Java 21, Spring Boot 3.x, Spring Security, JWT, MySQL, WebSockets, React, Vite, and Material UI.

## Technology Stack & Architecture

### Backend
- **Java**: Version 21 (LTS)
- **Spring Boot**: 3.x (Latest Stable, parent configured as 3.3.4 or similar)
- **Spring Security & JWT**: For stateless, role-based authorization
- **Spring Data JPA & Hibernate**: For database mapping and querying
- **MySQL 8+**: Main database
- **MapStruct**: For Entity-DTO mapping
- **Lombok**: For boilerplate reduction
- **Spring Mail**: For email OTPs and alerts
- **Spring WebSocket**: For real-time chat utilizing STOMP
- **Springdoc OpenAPI / Swagger**: For API documentation
- **JUnit 5 & Mockito**: For backend testing

### Frontend
- **React**: Version 18+ via Vite
- **Material UI (MUI)**: For a sleek, high-contrast minimal look using a pure black and pure white aesthetic
- **Axios**: For API requests with interceptors for JWT token propagation
- **React Router**: For client-side routing
- **SockJS-client & StompJS**: For WebSocket client support

---

## Folder Structure

### Backend (`patlatrlagna`)
```
com.patlatarlagna
├── config             # SecurityConfig, WebSocketConfig, MailConfig, SwaggerConfig
├── controller         # REST APIs under /api/v1 (Auth, Profiles, Matches, Chat, Admin)
├── dto                # Data Transfer Objects with validation annotations
├── entity             # JPA Entities
├── repository         # Spring Data Repositories
├── service            # Service Interfaces
│   └── impl           # Service Implementations
├── mapper             # MapStruct interfaces
├── security           # Custom UserDetails, JWT Filters, BCrypt utils
│   └── jwt
├── validation         # Custom validation logic and validators
├── exception          # Global exception handler & custom exceptions
├── util               # Helper classes (OTP generators, token utilities)
├── constants          # System constants
├── enums              # System-wide enums (Roles, InterestStatus, NotificationType)
├── audit              # Audit logging hooks
├── notification       # Email and in-app notifications service
├── websocket          # WebSocket configs and brokers
├── chat               # Chat specific controllers and message logs
├── admin              # Admin reports, user status updates, dashboard analytics
└── search             # Advanced search logic (Specifications)
```

### Frontend (`patlaTarLagnaFrontend`)
```
src
├── assets             # Brand images and default assets
├── components         # Shared UI parts (Header, Footer, ChatBox, UserCard)
├── context            # AuthContext, NotificationContext, ThemeContext
├── hooks              # Custom React hooks (useAuth, useWebSocket)
├── layouts            # AuthLayout, AppLayout, AdminLayout
├── pages              # Landing, Home, Search, Profile, Admin
├── routes             # Private and public routes configurations
├── services           # Axios clients and API hooks
├── theme              # Custom Material UI theme (Pure black and pure white minimal looks, no other colors)
└── utils              # Formatting and helper utilities
```

---

## Database Design

### Relationships Overview
- **Users** has one-to-one **Profiles**
- **Users** has many-to-many **Roles**
- **Profiles** has one-to-many **Photos**
- **Users** has one-to-one **MatchPreferences**
- **Users** has many **Interests** (both sender and receiver)
- **Users** has many **Matches** (both user1 and user2)
- **Users** has many **Messages** (both sender and receiver)
- **Users** has many **Notifications**
- **Users** has many **Reports** (reporter, reported)
- **Users** has many **Blocks** (blocker, blocked)
- **Users** has many **AuditLogs**
- **Users** has one-to-many **RefreshTokens**

---

## Deterministic Preference-Based Matchmaking
Instead of premium tiers or complex AI, matching uses a clear, preference-based percentage score:
1. Calculates compatibility on scale of 30% to 100%.
2. Compares target profile criteria directly against partner preferences set by the searching user (and vice versa, then averages).
3. Prefs compared: Age range, Height range, Religion, Caste, Lifestyle, and Location.

---

## Verification Plan

### Backend Verification
- Ensure Maven compiles the project with: `mvn clean install`
- Run service and repository unit tests with Mockito and H2.
- Verify Swagger interface is accessible at `http://localhost:8080/swagger-ui/index.html`.

### Frontend Verification
- Run Vite dev server on `http://localhost:5173`.
- Validate standard features: registration, profile builder, advanced filters, WebSocket-based real-time chat, and admin panels.
- Build production files using `npm run build` to confirm bundler success.
