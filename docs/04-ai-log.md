# AI Log — taskflow-api-kotlin

## 2026-11-22 — Preparación inicial
- Lectura de `AGENTS.md` y `docs/01-architecture.md`
- Verificación del contrato maestro (`_shared-contract/openapi.yaml`) y del esquema real de la base `insforge` (tabla compartida `"Task"`, enums `"TaskStatus"`/`"TaskPriority"`)
- Confirmación de que el pooler 6543 no está expuesto en InsForge (cerrado) → runtime y migraciones usan 5432

## 2026-11-22 — Implementación completa (pasos 1 a 5)
- Ktor 3.0 + Kotlin 2.2.20 + Exposed 0.54 + HikariCP + Flyway 10 + Koin 4
- `GET /health`, CRUD `/tasks` con filtros y paginación, `ErrorResponse` alineado al contrato
- Migración `V1__init_task_table.sql` idempotente (types, tabla e índices sobre la tabla compartida)
- CI (ktlint + tests + shadowJar) y deploy por SSH con build en el VPS
- Repositorio creado en GitHub, secrets configurados, deploy key en el VPS y `.env.kotlin` en `/opt/taskflow/.env.kotlin`

### Problemas resueltos
- El plugin `io.ktor.plugin` arrastra `com.github.johnrengelman.shadow:7.1.2` y choca por capability con `com.gradleup.shadow` → se retiró el plugin de Ktor (no se usaban sus tasks)
- `com.github.jengelman.gradle.plugins.shadow:8.1.1` no resuelve → coordenadas `com.gradleup.shadow:8.3.5`
- `kotlinx-serialization` se resolvía a 1.9.0 (metadata Kotlin 2.2) contra un toolchain 2.0.0 → toolchain subido a 2.2.20
- Healthcheck con `wget --spider` hacía HEAD → Ktor respondía 405 y Traefik descartaba el contenedor; se cambió a GET
- `flyway_schema_history` con checksum distinto en la BD compartida → `repair()` antes de `migrate()`

### Verificación
- `https://taskflow-api-kotlin.xprad.com/health` → `{"status":"ok","service":"taskflow-api-kotlin","database":"up"}`
- Certificado Let's Encrypt emitido para `taskflow-api-kotlin.xprad.com`
- CRUD completo, filtros, paginación, 400/404 y `/swagger` verificados contra la BD real
## 2026-11-23 — Paso 6 (documentación + OpenAPI)
- `README.md` completo: stack, tabla de endpoints, variables de entorno, desarrollo local, CI/CD y notas de base de datos
- OpenAPI servido en `/openapi.yaml` (YAML), `/openapi` (JSON, convertido en caliente) y Swagger UI en `/docs` y `/swagger`
- Roadmap actualizado: pasos 0 a 6 completos
