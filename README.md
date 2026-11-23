# taskflow-api-kotlin

API REST de TaskFlow implementada con **Kotlin 2.2 + Ktor 3.0 + Exposed ORM**, siguiendo el contrato OpenAPI 3.1 compartido (`docs/openapi.yaml`).

Desplegada en el VPS AWS (Docker + Traefik) en: **https://taskflow-api-kotlin.xprad.com**

## Stack

| Capa           | Tecnología                                                            |
| -------------- | --------------------------------------------------------------------- |
| Runtime        | Kotlin 2.2.20, JVM 21, Ktor 3.0 (Netty)                               |
| Persistencia   | Exposed ORM 0.54 + HikariCP + PostgreSQL Cloud (InsForge)              |
| Migraciones    | Flyway 10 (ejecutadas en el arranque con `DIRECT_URL`)                 |
| DI             | Koin 4                                                                 |
| Serialización  | kotlinx-serialization 1.9 (JSON)                                       |
| Documentación  | Swagger UI + `openapi.yaml` (contrato maestro)                         |
| Build          | Gradle 8.8 (Kotlin DSL) + Shadow plugin                                |
| Deploy         | GitHub Actions → SSH → `docker compose up --build` en el VPS           |

## Endpoints

| Método   | Ruta              | Descripción                                                     |
| -------- | ----------------- | --------------------------------------------------------------- |
| `GET`    | `/health`         | Health check (200 OK / 503 si la base de datos no responde)      |
| `GET`    | `/tasks`          | Lista paginada. Filtros: `status`, `priority`, `assignee`        |
| `POST`   | `/tasks`          | Crea una tarea (201). Defaults: `status=pendiente`, `priority=media` |
| `GET`    | `/tasks/{id}`     | Obtiene una tarea (200 / 404)                                    |
| `PATCH`  | `/tasks/{id}`     | Actualización parcial (200 / 404)                                |
| `DELETE` | `/tasks/{id}`     | Elimina una tarea (204 / 404)                                    |
| `GET`    | `/openapi.yaml`   | Contrato OpenAPI 3.1 en YAML                                     |
| `GET`    | `/openapi`        | Contrato OpenAPI 3.1 en JSON                                     |
| `GET`    | `/docs`, `/swagger` | Swagger UI                                                     |

### Paginación

`GET /tasks?page=1&limit=10` → `limit` máximo 100.

```json
{
  "data": [ ... ],
  "meta": { "total": 42, "page": 1, "limit": 10, "lastPage": 5 }
}
```

### Errores

Todas las respuestas de error siguen el contrato:

```json
{
  "statusCode": 400,
  "error": "Bad Request",
  "message": ["title must be at least 3 characters"],
  "timestamp": "2026-11-23T15:21:30.703Z",
  "path": "/tasks"
}
```

## Variables de entorno

Copia `.env.kotlin.example` a `.env.kotlin` (en el VPS vive en `/opt/taskflow/.env.kotlin`).

| Variable                    | Default                | Descripción                                   |
| --------------------------- | ---------------------- | --------------------------------------------- |
| `APP_NAME`                  | `taskflow-api-kotlin`  | Nombre del servicio (aparece en `/health`)     |
| `ENVIRONMENT`               | `production`           | Entorno de ejecución                           |
| `PORT`                      | `3000`                 | Puerto HTTP interno                            |
| `CORS_ORIGINS`              | `*`                    | Orígenes permitidos separados por coma         |
| `RATE_LIMIT_ENABLED`        | `true`                 | Activa el rate limit                           |
| `RATE_LIMIT_REQUESTS`       | `100`                  | Peticiones por ventana                         |
| `RATE_LIMIT_WINDOW_SECONDS` | `60`                   | Duración de la ventana                         |
| `DB_MAX_CONNECTIONS`        | `10`                   | Tamaño del pool Hikari                         |
| `DATABASE_URL`              | —                      | URL de conexión (obligatoria)                  |
| `DIRECT_URL`                | `DATABASE_URL`         | URL para migraciones Flyway                    |

## Desarrollo local

```bash
# Tests + lint (requiere Docker para los tests de Testcontainers)
gradle ktlintCheck test

# Ejecutar con la base de datos configurada
DATABASE_URL=postgresql://... DIRECT_URL=postgresql://... gradle run

# Imagen y contenedor
docker compose up --build
```

## CI/CD

- **CI** (`.github/workflows/ci.yml`): `ktlint` → `test` → `shadowJar` en cada PR y push a `main`.
- **Deploy** (`.github/workflows/deploy.yml`): en push a `main`, hace `git fetch` + `reset --hard` en `/opt/taskflow-ecosystem/taskflow-api-kotlin`, reconstruye la imagen (`docker compose up -d --build --no-deps api-kotlin`) y verifica `https://taskflow-api-kotlin.xprad.com/health`.

Secrets necesarios: `VPS_HOST`, `VPS_USER`, `VPS_SSH_PRIVATE_KEY`.

## Base de datos

La API comparte la base `insforge` con el resto del ecosistema. La tabla `"Task"` (tipos `"TaskStatus"` y `"TaskPriority"`) es la del contrato, por lo que la migración `V1__init_task_table.sql` es **idempotente** y solo crea lo que falte.

## Tests

```bash
gradle test
```

- `TaskValidatorTest` — validación de payloads y parseo de fechas.
- `DatabaseUrlTest` — parsing de `DATABASE_URL` (credenciales, parámetros, `stringtype`).
- `TaskRoutesTest` — routing completo con un `TaskService` falso (201/200/204/400/404).
- `TaskRepositoryTest` — integración real con PostgreSQL vía Testcontainers (se omite si no hay Docker).