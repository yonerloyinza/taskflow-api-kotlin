package com.taskflow.api.repository

import com.taskflow.api.config.AppConfig
import com.taskflow.api.db.DatabaseFactory
import com.taskflow.api.db.Migrations
import com.taskflow.api.model.TaskPriority
import com.taskflow.api.model.TaskStatus
import com.taskflow.api.service.TaskQuery
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Testcontainers
class TaskRepositoryTest {

    private lateinit var repository: TaskRepository

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")

        @JvmStatic
        private lateinit var database: Database

        @BeforeAll
        @JvmStatic
        fun connect() {
            val config = AppConfig(
                name = "taskflow-api-kotlin-test",
                environment = "test",
                port = 0,
                corsOrigins = listOf("*"),
                databaseUrl = postgres.jdbcUrl,
                directUrl = postgres.jdbcUrl,
                dbMaxConnections = 5,
                rateLimitEnabled = false,
                rateLimitRequests = 100,
                rateLimitWindowSeconds = 60L,
            )
            database = DatabaseFactory.connect(config)
            Migrations.apply(config.directUrl)
        }
    }

    @BeforeEach
    fun setUp() {
        transaction(database) { exec("DELETE FROM \"Task\"") }
        repository = TaskRepository(database)
    }

    @Test
    fun `insert and find by id`() {
        val now = LocalDateTime.now()
        val created = repository.insert(
            title = "Escribir tests",
            description = "Suite de integración",
            status = TaskStatus.pendiente,
            priority = TaskPriority.alta,
            assignee = "ana",
            dueDate = now.plusDays(2),
            now = now,
        )

        val found = repository.findById(created.id)

        assertNotNull(found)
        assertEquals(created.id, found.id)
        assertEquals("Escribir tests", found.title)
        assertEquals(TaskStatus.pendiente.name, found.status)
        assertEquals(TaskPriority.alta.name, found.priority)
        assertNull(repository.findById(UUID.randomUUID().toString()))
    }

    @Test
    fun `find page filters by status priority and assignee`() {
        val now = LocalDateTime.now()
        repository.insert("Uno", null, TaskStatus.pendiente, TaskPriority.alta, "ana", now.plusDays(1), now)
        repository.insert("Dos", null, TaskStatus.hecha, TaskPriority.baja, "ana", now.plusDays(1), now)
        repository.insert("Tres", null, TaskStatus.pendiente, TaskPriority.alta, "beto", now.plusDays(1), now)

        val (total, rows) = repository.findPage(
            TaskQuery(status = TaskStatus.pendiente, priority = TaskPriority.alta, assignee = "ana"),
        )

        assertEquals(1L, total)
        assertEquals(1, rows.size)
        assertEquals("Uno", rows.first().title)
    }

    @Test
    fun `find page paginates results`() {
        val now = LocalDateTime.now()
        repeat(5) { index ->
            repository.insert("Tarea $index", null, TaskStatus.pendiente, TaskPriority.media, "ana", now.plusDays(index.toLong()), now)
        }

        val (total, rows) = repository.findPage(TaskQuery(page = 2, limit = 2))

        assertEquals(5L, total)
        assertEquals(2, rows.size)
    }

    @Test
    fun `update replaces mutable fields`() {
        val now = LocalDateTime.now()
        val created = repository.insert("Original", "desc", TaskStatus.pendiente, TaskPriority.baja, "ana", now.plusDays(1), now)

        val updated = repository.update(
            current = created,
            title = "Modificada",
            description = null,
            status = TaskStatus.en_curso,
            priority = TaskPriority.media,
            assignee = "beto",
            dueDate = now.plusDays(3),
            now = now.plusMinutes(5),
        )

        val found = requireNotNull(repository.findById(created.id))
        assertEquals("Modificada", found.title)
        assertEquals("desc", found.description)
        assertEquals(TaskStatus.en_curso.name, found.status)
        assertEquals(TaskPriority.media.name, found.priority)
        assertEquals("beto", found.assignee)
        assertTrue(found.updatedAt.isAfter(updated.createdAt.minusSeconds(1)))
    }

    @Test
    fun `delete removes the row`() {
        val now = LocalDateTime.now()
        val created = repository.insert("Borrar", null, TaskStatus.hecha, TaskPriority.alta, "ana", now.plusDays(1), now)

        assertTrue(repository.delete(created.id))
        assertNull(repository.findById(created.id))
        assertTrue(!repository.delete(created.id))
    }
}