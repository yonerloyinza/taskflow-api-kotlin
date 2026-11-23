# Bitácora de IA — taskflow-api-kotlin

| Paso | Qué se pidió al agente | Qué se aceptó | Qué se corrigió manualmente |
| :--- | :--- | :--- | :--- |
| Paso 0 | Inicialización de harness independiente | Estructura aislada con contrato OpenAPI 3.1 compartido | N/A |
| Paso 1 | Estructura Base e Infraestructura | Ktor 3.0, Gradle Kotlin DSL, seguridad (CORS, Rate Limit, Security Headers), Validation, Global Exception Handling, Koin DI, Logback, Ktor OpenAPI | - |
| Paso 2 | Persistencia y Migraciones Duales | Exposed ORM + modelo Task (DAO/Table), dual connection (DATABASE_URL pooler 6543, DIRECT_URL 5432), migraciones Flyway aplicadas a InsForge Cloud | - |
| Paso 3 | Implementación del Contrato (docs/openapi.yaml) | Endpoints CRUD Tasks, filtros (status, priority, assignee), paginación (offset/limit), manejo errores ErrorResponse, GET /health con verificación DB en 0.0.0.0:3000 | - |
| Paso 4 | Testing | Suite unitarias (Service layer) + integración (Routing) con JUnit 5 + Ktor TestApplicationEngine + Testcontainers | Pendiente: JDK 25 incompatibilidad con Kotlin 2.0.0 para ejecutar tests |
| Paso 5a | Documentación y Docker | AGENTS.md actualizado, docs/01-architecture.md, Dockerfile multi-etapa (gradle:8.8-jdk21 → eclipse-temurin:21-jre-alpine) para VPS AWS + Traefik | - |
| Paso 5b | CI/CD VPS | GitHub Actions deploy.yml → SSH → git pull + docker compose up -d --build --no-deps api-kotlin + docker image prune -f en /opt/taskflow. Variables en /opt/taskflow/.env.kotlin. Sin Render, sin registros Docker externos. | - |