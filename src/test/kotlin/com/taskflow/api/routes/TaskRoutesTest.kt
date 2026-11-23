package com.taskflow.api.routes

import com.taskflow.api.dto.CreateTaskRequest
import com.taskflow.api.dto.PageMeta
import com.taskflow.api.dto.PaginatedTasksResponse
import com.taskflow.api.dto.TaskDto
import com.taskflow.api.dto.UpdateTaskRequest
import com.taskflow.api.error.ApiValidationException
import com.taskflow.api.error.TaskNotFoundException
import com.taskflow.api.model.TaskPriority
import com.taskflow.api.model.TaskStatus
import com.taskflow.api.plugins.configureSerialization
import com.taskflow.api.plugins.configureStatusPages
import com.taskflow.api.service.TaskQuery
import com.taskflow.api.service.TaskService
import com.taskflow.api.service.lastPage
import com.taskflow.api.validation.TaskValidator
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

class TaskRoutesTest {

    private val service = FakeTaskService()

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `POST creates a task`() = testApplication {
        configureTestApp()

        val response = client.postJson("/tasks", """{"title":"Nueva tarea","assignee":"ana","dueDate":"2026-12-01T10:00:00Z"}""")

        assertEquals(HttpStatusCode.Created, response.status)
        val payload = response.jsonObject()
        assertEquals("Nueva tarea", payload.string("title"))
        assertEquals("pendiente", payload.string("status"))
        assertEquals("media", payload.string("priority"))
        assertEquals("ana", payload.string("assignee"))
    }

    @Test
    fun `POST returns 400 with ErrorResponse when validation fails`() = testApplication {
        configureTestApp()

        val response = client.postJson("/tasks", """{"title":"ab","assignee":"ana","dueDate":"2026-12-01T10:00:00Z"}""")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val payload = response.jsonObject()
        assertEquals(400, payload.int("statusCode"))
        assertEquals("Bad Request", payload.string("error"))
        assertTrue(payload["message"] is JsonArray)
        assertTrue(payload.string("path").startsWith("/tasks"))
    }

    @Test
    fun `POST returns 400 for malformed json`() = testApplication {
        configureTestApp()

        val response = client.postJson("/tasks", """{"title":""")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET lists tasks with pagination meta`() = testApplication {
        configureTestApp()
        client.postJson("/tasks", """{"title":"Primera","assignee":"ana","dueDate":"2026-12-01T10:00:00Z"}""")

        val response = client.get("/tasks")

        assertEquals(HttpStatusCode.OK, response.status)
        val payload = response.jsonObject()
        assertEquals(1, payload.jsonObject["data"]?.jsonArray?.size)
        val meta = payload.jsonObject["meta"]?.jsonObject
        assertEquals(1, meta?.int("total"))
        assertEquals(1, meta?.int("page"))
        assertEquals(10, meta?.int("limit"))
        assertEquals(1, meta?.int("lastPage"))
    }

    @Test
    fun `GET forwards filters and pagination to the service`() = testApplication {
        configureTestApp()

        client.get("/tasks?status=pendiente&priority=alta&assignee=ana&page=2&limit=5")

        val query = requireNotNull(service.lastQuery)
        assertEquals(TaskStatus.pendiente, query.status)
        assertEquals(TaskPriority.alta, query.priority)
        assertEquals("ana", query.assignee)
        assertEquals(2, query.page)
        assertEquals(5, query.limit)
    }

    @Test
    fun `GET returns 400 for unknown status`() = testApplication {
        configureTestApp()

        val response = client.get("/tasks?status=desconocido")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET returns 400 when limit exceeds the maximum`() = testApplication {
        configureTestApp()

        val response = client.get("/tasks?limit=500")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET by id returns the task`() = testApplication {
        configureTestApp()
        val created = client.postJson("/tasks", """{"title":"Detalle","assignee":"ana","dueDate":"2026-12-01T10:00:00Z"}""").jsonObject()
        val id = created.string("id")

        val response = client.get("/tasks/$id")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(id, response.jsonObject().string("id"))
    }

    @Test
    fun `GET by unknown id returns 404`() = testApplication {
        configureTestApp()

        val response = client.get("/tasks/00000000-0000-0000-0000-000000000000")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(404, response.jsonObject().int("statusCode"))
    }

    @Test
    fun `PATCH updates the task`() = testApplication {
        configureTestApp()
        val id = client.postJson("/tasks", """{"title":"Original","assignee":"ana","dueDate":"2026-12-01T10:00:00Z"}""").jsonObject().string("id")

        val response = client.patchJson("/tasks/$id", """{"status":"hecha"}""")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("hecha", response.jsonObject().string("status"))
        assertEquals("Original", response.jsonObject().string("title"))
    }

    @Test
    fun `PATCH returns 404 for unknown id`() = testApplication {
        configureTestApp()

        val response = client.patchJson("/tasks/00000000-0000-0000-0000-000000000000", """{"status":"hecha"}""")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE returns 204`() = testApplication {
        configureTestApp()
        val id = client.postJson("/tasks", """{"title":"Eliminar","assignee":"ana","dueDate":"2026-12-01T10:00:00Z"}""").jsonObject().string("id")

        val response = client.delete("/tasks/$id")

        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals(id, service.lastDeleted)
    }

    @Test
    fun `DELETE returns 404 for unknown id`() = testApplication {
        configureTestApp()

        val response = client.delete("/tasks/00000000-0000-0000-0000-000000000000")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    private fun io.ktor.server.testing.TestApplicationBuilder.configureTestApp() {
        application {
            configureSerialization()
            configureStatusPages()
            install(Koin) { modules(module { single<TaskService> { service } }) }
            routing { taskRoutes() }
        }
    }

    private suspend fun HttpClient.postJson(path: String, body: String): HttpResponse = post(path) {
        setBody(TextContent(body, ContentType.Application.Json))
    }

    private suspend fun HttpClient.patchJson(path: String, body: String): HttpResponse = patch(path) {
        setBody(TextContent(body, ContentType.Application.Json))
    }

    private suspend fun HttpResponse.jsonObject(): JsonObject = Json.parseToJsonElement(bodyAsText()).jsonObject

    private fun JsonObject.string(name: String): String = this[name]?.jsonPrimitive?.content ?: error("Missing field $name")

    private fun JsonObject.int(name: String): Int = this[name]?.jsonPrimitive?.int ?: error("Missing field $name")
}

private class FakeTaskService : TaskService {

    private val tasks = linkedMapOf<String, TaskDto>()
    var lastQuery: TaskQuery? = null
    var lastDeleted: String? = null

    override suspend fun create(request: CreateTaskRequest): TaskDto {
        val errors = TaskValidator.validateCreate(request)
        if (errors.isNotEmpty()) throw ApiValidationException(errors)

        val now = LocalDateTime.now()
        val task = TaskDto(
            id = UUID.randomUUID().toString(),
            title = request.title,
            description = request.description,
            status = request.status ?: TaskStatus.pendiente,
            priority = request.priority ?: TaskPriority.media,
            assignee = request.assignee,
            dueDate = TaskValidator.parseDueDate(request.dueDate.orEmpty()).toString(),
            createdAt = now.toString(),
            updatedAt = now.toString(),
        )
        tasks[task.id] = task
        return task
    }

    override suspend fun list(query: TaskQuery): PaginatedTasksResponse {
        lastQuery = query
        val all = tasks.values.toList()
        return PaginatedTasksResponse(
            data = all,
            meta = PageMeta(
                total = all.size.toLong(),
                page = query.page,
                limit = query.limit,
                lastPage = lastPage(all.size.toLong(), query.limit),
            ),
        )
    }

    override suspend fun get(id: String): TaskDto = tasks[id] ?: throw TaskNotFoundException(id)

    override suspend fun update(id: String, request: UpdateTaskRequest): TaskDto {
        val errors = TaskValidator.validateUpdate(request)
        if (errors.isNotEmpty()) throw ApiValidationException(errors)

        val current = tasks[id] ?: throw TaskNotFoundException(id)
        val updated = current.copy(
            title = request.title ?: current.title,
            description = request.description ?: current.description,
            status = request.status ?: current.status,
            priority = request.priority ?: current.priority,
            assignee = request.assignee ?: current.assignee,
            dueDate = request.dueDate?.let { TaskValidator.parseDueDate(it).toString() } ?: current.dueDate,
        )
        tasks[id] = updated
        return updated
    }

    override suspend fun delete(id: String) {
        lastDeleted = id
        if (tasks.remove(id) == null) throw TaskNotFoundException(id)
    }
}
