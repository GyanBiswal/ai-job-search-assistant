# AI Job Search Assistant — Backend

Spring Boot backend for the AI Job Search Assistant project.

## Tech Stack
- Java 17, Spring Boot 3.3
- PostgreSQL
- Spring Data JPA / Hibernate

## Running locally
1. Start PostgreSQL and create the `ai_job_search` database (see setup notes).
2. Run `BackendApplication.java` from IntelliJ, or `mvn spring-boot:run`.
3. Verify health: `GET http://localhost:8080/actuator/health`