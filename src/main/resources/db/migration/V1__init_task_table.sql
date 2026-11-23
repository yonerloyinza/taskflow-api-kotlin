-- Flyway Migration: V1__init_task_table.sql
-- Esquema de la entidad Task (alineado con el contrato OpenAPI 3.1 compartido).
-- Idempotente: la base de datos TaskFlow es compartida por varios backends, por lo que
-- el types y la tabla solo se crean si aún no existen.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'TaskStatus') THEN
        CREATE TYPE "TaskStatus" AS ENUM ('pendiente', 'en_curso', 'hecha');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'TaskPriority') THEN
        CREATE TYPE "TaskPriority" AS ENUM ('baja', 'media', 'alta');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS "Task" (
    "id"          TEXT PRIMARY KEY,
    "title"       VARCHAR(100) NOT NULL,
    "description" VARCHAR(500),
    "status"      "TaskStatus" NOT NULL DEFAULT 'pendiente',
    "priority"    "TaskPriority" NOT NULL DEFAULT 'media',
    "assignee"    VARCHAR(100) NOT NULL,
    "dueDate"     TIMESTAMP(3) WITHOUT TIME ZONE NOT NULL,
    "createdAt"   TIMESTAMP(3) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt"   TIMESTAMP(3) WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS "Task_status_idx" ON "Task" ("status");
CREATE INDEX IF NOT EXISTS "Task_priority_idx" ON "Task" ("priority");
CREATE INDEX IF NOT EXISTS "Task_assignee_idx" ON "Task" ("assignee");
CREATE INDEX IF NOT EXISTS "Task_createdAt_idx" ON "Task" ("createdAt" DESC);