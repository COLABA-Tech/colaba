# COLABA Task Management System

Выполнили:
Колмаков Дмитрий,
Андреева Божена,
Юхно Таисия

Backend service for task management system (Jira/Trello analogue) built with Spring Boot.

## 🚀 Tech Stack

- **Java 17** + Spring Boot 3.2
- **PostgreSQL** - relational database
- **Liquibase** - database migrations
- **Spring Data JPA** - data access layer
- **Testcontainers** - integration testing
- **Docker** + Docker Compose - containerization
- **OpenAPI 3** - API documentation

## 📋 Features

- Project and task management
- User assignment and role-based access
- Task status tracking (TODO, IN_PROGRESS, DONE)
- Comments and tags system
- RESTful API with proper HTTP statuses
- Data validation and error handling
- Pagination and infinite scroll support

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/example/colaba/
│   │   ├── controller/    # REST controllers
│   │   ├── service/       # Business logic
│   │   ├── repository/    # Data access layer
│   │   ├── entity/        # JPA entities
│   │   ├── dto/           # Data transfer objects
│   │   └── config/        # Configuration classes
│   └── resources/
│       └── db/changelog/  # Liquibase migrations
```

![IMAGE 2025-11-14 10:42:36](https://github.com/user-attachments/assets/7ec71804-c05b-43a4-96cc-253a20b5c971)

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 25 (for local development)

### Running with Docker

```bash
# Clone repository
git clone <repository-url>
cd colaba-task-management

# Start application
docker-compose up --build
```

Application will be available at: http://localhost:8080

### Local Development

```bash
# Run with Maven Wrapper
./mvnw spring-boot:run

# Or build and run tests
./mvnw clean install
```

## 📚 API Documentation

Once application is running:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🧪 Testing

```bash
# Run unit tests
./mvnw test

# Run integration tests (requires Docker)
./mvnw verify
```

## 🔧 Configuration

Environment variables:

- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

## 📦 Deployment

The application is containerized and can be deployed to any Docker-supported environment:

```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 👥 Development Team

Project for academic course on modern JVM stack development.

## 📄 License

Academic project - for educational purposes.
