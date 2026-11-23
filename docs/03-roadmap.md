# Roadmap — taskflow-api-kotlin

- [ ] **Paso 0 — Aclaración de contexto:** Verificar variables de entorno y puertos locales. JDK 21+, Gradle 8.8+.
- [ ] **Paso 1 — Estructura Base e Infraestructura:** Scaffolding Ktor 3.0 (Kotlin 2.0), Gradle Kotlin DSL, seguridad (CORS, Rate Limit, Security Headers), Validation, Global Exception Handling, Koin DI, Logback, Ktor OpenAPI.
- [ ] **Paso 2 — Persistencia y Migraciones Duales:** Exposed ORM + modelo Task (DAO/Table), dual connection (`DATABASE_URL` pooler 6543, `DIRECT_URL` 5432), migraciones Flyway aplicadas a InsForge Cloud.
- [ ] **Paso 3 — Implementación del Contrato (`docs/openapi.yaml`):** Endpoints CRUD Tasks, filtros (`status`, `priority`, `assignee`), paginación (offset/limit), manejo errores `ErrorResponse`, `GET /health` con verificación DB en `0.0.0.0:3000`.
- [ ] **Paso 4 — Testing:** Suite unitarias (Service layer) + integración (Routing) con JUnit 5 + Ktor TestApplicationEngine + Testcontainers, 25 tests passing.
- [ ] **Paso 5a — Documentación y Docker:** AGENTS.md actualizado, docs/01-architecture.md, Dockerfile multi-etapa (gradle:8.8-jdk21 → eclipse-temurin:21-jre-alpine) para VPS AWS + Traefik.
- [ ] **Paso 5b — CI/CD VPS:** GitHub Actions deploy.yml → SSH → `git pull` + `docker compose up -d --build --no-deps api-kotlin` + `docker image prune -f` en `/opt/taskflow`. Variables en `/opt/taskflow/.env.kotlin`. Sin Render, sin registros Docker externos.