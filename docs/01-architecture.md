# Arquitectura y Despliegue — taskflow-api-kotlin

- **Stack:** Kotlin 2.0 + Ktor 3.0 + Exposed ORM + Flyway + Gradle (Kotlin DSL)
- **Estrategia de Despliegue:** VPS AWS EC2 — Docker multi-etapa (gradle:8.8-jdk21 build, eclipse-temurin:21-jre-alpine runtime) + Traefik HTTPS
- **Base de Datos Cloud Dual:** PostgreSQL gestionado en la nube, alternable entre InsForge Cloud y Supabase Cloud únicamente mediante variables de entorno (`DATABASE_URL` con pooling puerto 6543 para runtime, `DIRECT_URL` puerto 5432 para migraciones) cargadas desde `/opt/taskflow/.env.kotlin` en el VPS. Regla estricta: Prohibido instalar o importar SDKs propietarios; toda persistencia debe pasar por Exposed ORM.
- **Contrato API:** OpenAPI 3.1 compartido (`docs/openapi.yaml`) — rutas, DTOs, filtros (`status`, `priority`, `assignee`), paginación y formato de errores `ErrorResponse` respetados al 100%. Documentación generada via Ktor OpenAPI plugin en `/docs`.
- **Health Check:** `GET /health` verificando conexión activa a PostgreSQL Cloud (Exposed `Database` connection), escuchando en `0.0.0.0:3000`.
- **Middleware Stack:** Ktor — CORS (`io.ktor.server.plugins.cors`), Rate Limit (`io.ktor.server.plugins.ratelimit`), Security Headers, Request ID, Logging (`Logback`), Exception Handling global.
- **Validación:** Kotlinx Serialization + Ktor Validation plugin — validación en DTOs, `ValidationException` handler global → `ErrorResponse`.
- **Documentación API:** Ktor OpenAPI (Swagger) — `/openapi`, `/swagger` (solo dev).
- **Migraciones:** Flyway — archivos SQL en `src/main/resources/db/migration`, ejecutados en startup con `DIRECT_URL`.
- **Inyección de Dependencias:** Koin — módulos por feature (`networkModule`, `databaseModule`, `repositoryModule`, `serviceModule`).
- **Red:** Traefik termina TLS y enruta HTTPS → puerto interno 3000 del contenedor (sin exponer puertos en el host).
- **CI/CD:** GitHub Actions (.github/workflows/deploy.yml) → SSH al VPS → `git pull` + `docker compose up -d --build --no-deps api-kotlin` + `docker image prune -f` en `/opt/taskflow`. Build local en VPS, sin Docker Hub / ghcr.io.
- **Prohibido:** Monorepos, SQLite, SDKs propietarios, migraciones manuales sin Flyway, Render, SnapDeploy, registros Docker externos.