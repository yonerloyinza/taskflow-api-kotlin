# Roadmap de Implementación: taskflow-api-kotlin

## Paso 0 — Setup y contratos (COMPLETADO)
- [x] Repositorio `taskflow-api-kotlin` — Kotlin 2.2.20 + Ktor 3.0 + Exposed 0.54
- [x] Contrato `openapi.yaml` — `docs/openapi.yaml` (copia de `_shared-contract`)
- [x] Auth InsForge/Supabase — la API es pública (sin auth); el ecosistema ya resuelve identidad

## Paso 1 — Scaffolding Ktor (COMPLETADO)
- [x] `build.gradle.kts` — Kotlin 2.2.20, Ktor 3.0, Exposed 0.54, HikariCP, Flyway 10, Koin 4, shadow
- [x] `Application.kt` — `embeddedServer(Netty)` + `Application.module()`
- [x] Security: CORS, rate limit, security headers, call id + call logging
- [x] Validación: `TaskValidator` (title 3-100, description <=500, assignee <=100, dueDate ISO-8601)
- [x] Koin: `AppModule` (config, Database, TaskRepository, TaskService)
- [x] `logback.xml` — INFO/DEBUG prod/dev
- [x] Ktor OpenAPI plugin — `/swagger` (Swagger UI) + `/openapi.yaml`

## Paso 2 — Persistencia (COMPLETADO)
- [x] Modelo `Task` — `Tasks` table mapeada a la tabla compartida `"Task"`
- [x] Migraciones Flyway — `V1__init_task_table.sql` idempotente (types + tabla + índices)
- [x] `TaskRepository` — CRUD, filtros (status/priority/assignee), paginación page/limit
- [x] Vendor-agnostic — `DATABASE_URL` / `DIRECT_URL` (sin SDK propietario)

## Paso 3 — Endpoints (COMPLETADO)
- [x] `GET /health` — 200 con `{status, service, database, timestamp}`, 503 si la DB no responde
- [x] `GET /tasks` — filtros `status`, `priority`, `assignee` + `page`, `limit`
- [x] `POST /tasks` — 201 (defaults `pendiente`/`media`)
- [x] `GET /tasks/{id}` — 200/404
- [x] `PATCH /tasks/{id}` — 200/404
- [x] `DELETE /tasks/{id}` — 204/404
- [x] Errores — `ErrorResponse {statusCode, error, message, timestamp, path}`

## Paso 4 — Testing (COMPLETADO)
- [x] `TaskValidatorTest` — 13 tests de validación y parseo de fechas
- [x] `DatabaseUrlTest` — 5 tests de parsing de `DATABASE_URL`
- [x] `TaskRoutesTest` — 13 tests de routing con `FakeTaskService` (201/200/204/400/404)
- [x] `TaskRepositoryTest` — 5 tests de integración con Testcontainers (se omiten si no hay Docker)
- [x] `gradle test` verde en CI

## Paso 5 — Despliegue (COMPLETADO)
- [x] 5a. Docker local — Dockerfile multi-etapa + `docker-compose.yml` con labels Traefik
- [x] 5b. CI/CD — GitHub Actions: `ci.yml` (ktlint + test + shadowJar) y `deploy.yml` (SSH + build en VPS)
- [x] 5c. Smoke test — `/health`, CRUD completo, filtros, 400/404 y `/swagger` verificados en producción

## Paso 6 — Documentación (COMPLETADO)
- [x] `README.md` — stack, endpoints, variables de entorno, desarrollo local, CI/CD y base de datos
- [x] `docs/01-architecture.md` + `docs/04-ai-log.md` actualizados
- [x] Documentación OpenAPI servida: `/openapi.yaml`, `/openapi` (JSON), `/docs` y `/swagger`

## infra/variables (COMPLETADO)
- [x] `.env.kotlin.example` + `.env.kotlin` en `/opt/taskflow/.env.kotlin` del VPS
- [x] DNS `taskflow-api-kotlin.xprad.com` + Traefik Let's Encrypt (certificado emitido)
- [x] Deploy key `taskflow_api_kotlin_deploy` en el VPS + secrets `VPS_HOST`, `VPS_USER`, `VPS_SSH_PRIVATE_KEY`