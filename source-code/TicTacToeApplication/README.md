# Tic Tac Toe Application

## Overview

A backend server for a real-time Tic Tac Toe game. It exposes an **HTTP API** for authentication and user management, and a **WebSocket server** for real-time gameplay (joining rooms, making moves, receiving game updates).

This is a academic project built to practice backend architecture, real-time communication, and clean separation of concerns in Java.

## Stack

- **Java 8 SE**
- **Javalin** - lightweight HTTP web framework
- **Java-WebSocket** - WebSocket server implementation
- **EclipseLink (JPA)** - persistence
- **H2 Database** - embedded database
- **JWT (jjwt)** - stateless authentication
- **BCrypt (jBCrypt)** - password hashing
- **Jackson** - JSON serialization/deserialization
- **Logback + SLF4J** - logging
- **Maven** - build and dependency management

## Dependencies

| Dependency | Purpose |
|---|---|
| `io.javalin:javalin` | HTTP server and routing |
| `org.java-websocket:Java-WebSocket` | WebSocket server |
| `com.fasterxml.jackson.core:jackson-databind` | JSON handling |
| `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` | JSON support for `java.time` types |
| `ch.qos.logback:logback-classic` | Logging |
| `org.mindrot:jbcrypt` | Password hashing |
| `io.github.cdimascio:dotenv-java` | Loading `.env` environment variables |
| `org.eclipse.persistence:eclipselink` | JPA provider |
| `io.jsonwebtoken:jjwt-api/impl/jackson` | JWT generation and validation |
| `javax.persistence:javax.persistence-api` | JPA specification |
| `com.h2database:h2` | Embedded database |

## Architecture

The project follows a **layered architecture**, inspired by Clean Architecture / DDD principles:

```
com.tic.tac.toe/
├── domain/               Core business rules: entities, exceptions, events, repository interfaces
├── application/          Use cases and orchestration: services, DTOs, event mechanism
├── infrastructure/       Technical implementations: database (JPA), JWT, environment config
└── presentation/         Entry points: HTTP controllers, WebSocket handlers, middlewares
```

**Dependency rule:** outer layers depend on inner layers, never the other way around.
- `domain` has no dependencies on other layers - it's pure business logic.
- `application` depends only on `domain` (through interfaces).
- `infrastructure` implements interfaces defined in `domain`/`application`.
- `presentation` wires everything together and exposes it as HTTP routes or WebSocket events.

### Authentication
- Login/register happens over HTTP (`/api/v1/auth`), returning a JWT.
- The JWT is used to authenticate both HTTP requests (via cookie + middleware) and WebSocket connections (validated on handshake).

## Running the project

1. Copy `env.example` to `.env` and fill in the required values.
2. Build and run with Maven:
   ```bash
   mvn clean install
   mvn exec:java -Dexec.mainClass="com.tic.tac.toe.Application"
   ```
3. The HTTP server starts on `HTTP_PORT` and the WebSocket server on `SOCKET_PORT` (both configurable via `.env`).