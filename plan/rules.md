# Development Rules - PatlaTarLagna

This document details the development constraints, coding standards, and architectural rules to be followed during the construction of the PatlaTarLagna platform.

## 1. Backend Coding Rules (Java / Spring Boot 3.x)
- **Base Package**: Use `com.patlatarlagna` as the root package. All packages (config, controller, dto, entity, etc.) must be placed under it.
- **Dependency Injection**: Always use constructor injection. Field injection (`@Autowired` on variables) is strictly prohibited.
- **Entity Exposure**: Never expose database entity classes directly through REST endpoints. Always use MapStruct mappers to convert between entities and DTOs.
- **API Standards**:
  - Prefix all routes with `/api/v1`.
  - Use proper HTTP methods (GET, POST, PUT, DELETE).
  - Use standard HTTP status codes (200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Internal Server Error).
  - All API responses should wrap data in a standardized wrapper: `ApiResponse<T>` containing status, message, and payload/metadata.
- **Error Handling**: Use a centralized exception handler (`@RestControllerAdvice`) to capture and format errors uniformly.
- **Validation**: Use standard JSR-380 validation annotations (`@NotBlank`, `@Size`, `@Min`, `@Max`, `@Email`, etc.) on DTO parameters.
- **Logging**: Use SLF4J loggers. Avoid `System.out.println()`.
- **Database Rules**:
  - Keep entities lean. Use Lazy Loading (`FetchType.LAZY`) for relationships unless eagerly needed.
  - Implement pagination (`Pageable`) and sorting for search and listing endpoints.
- **Security**: Use Spring Security with stateless JWT validation. Protect user passwords using BCrypt.

## 2. Frontend Coding Rules (React + Vite + Material UI)
- **Design System**: Use a custom Material UI theme. The design must be extremely minimal using ONLY pure black and pure white colors. No other colors (like red, green, blue, pink, or gold) are allowed. This creates a high-contrast luxury monochromatic visual language.
- **Aesthetics**:
  - Include modern elements: subtle gradients, transitions, and hover micro-animations.
  - Optimize layout responsiveness (Desktop, Tablet, Mobile).
  - Do not use plain text placeholders for core UI elements; render rich, engaging component layouts.
- **Authentication Flow**:
  - Store tokens securely. Handle token expiration by redirecting to login or refreshing tokens automatically via Axios interceptors.
- **WebSockets**:
  - Connect to `/ws` using SockJS and STOMP protocols.
  - Ensure connections are cleaned up properly when components unmount to prevent memory leaks.

## 3. General Development Rules
- **Testing**: Maintain test coverage using JUnit 5 and Mockito. Every service and controller addition must be accompanied by relevant test assertions.
- **Documentation**: Write meaningful Javadocs on classes, service interfaces, and controller methods. Keep Swagger annotations up to date.
- **Documentation Integrity**: Maintain comment and structure integrity during file edits.
- **Continuous Tracking**: The files in `/plan/` must be updated if there are changes to scope, endpoints, or DB structures during development.
