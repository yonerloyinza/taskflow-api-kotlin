package com.taskflow.api.service

import com.taskflow.api.dto.CreateTaskRequest
import com.taskflow.api.dto.PageMeta
import com.taskflow.api.dto.PaginatedTasksResponse
import com.taskflow.api.dto.TaskDto
import com.taskflow.api.dto.UpdateTaskRequest
import com.taskflow.api.error.ApiValidationException
import com.taskflow.api.error.TaskNotFoundException
import com.taskflow.api.model.TaskPriority
import com.taskflow.api.model.TaskRecord
import com.taskflow.api.model.TaskStatus
import com.taskflow.api.repository.TaskRepository
import com.taskflow.api.validation.TaskValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

data class TaskQuery(
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val assignee: String? = null,
    val page: Int = DEFAULT_PAGE,
    val limit: Int = DEFAULT_LIMIT,
) {
    fun normalized(): TaskQuery = copy(
        assignee = assignee?.takeIf { it.isNotBlank() },
        page = page.coerceAtLeast(1),
        limit = limit.coerceIn(1, MAX_LIMIT),
    )

    fun offset(): Long = (normalized().page - 1L) * normalized().limit

    companion object {
        const val DEFAULT_PAGE = 1
        const val DEFAULT_LIMIT = 10
        const val MAX_LIMIT = 100
    }
}

interface TaskService {
    suspend fun create(request: CreateTaskRequest): TaskDto

    suspend fun list(query: TaskQuery): PaginatedTasksResponse

    suspend fun get(id: String): TaskDto

    suspend fun update(id: String, request: UpdateTaskRequest): TaskDto

    suspend fun delete(id: String)
}

class TaskServiceImpl(private val repository: TaskRepository) : TaskService {

    override suspend fun create(request: CreateTaskRequest): TaskDto = withContext(Dispatchers.IO) {
        val errors = TaskValidator.validateCreate(request)
        if (errors.isNotEmpty()) throw ApiValidationException(errors)

        val now = LocalDateTime.now()
        val record = repository.insert(
            title = request.title,
            description = request.description,
            status = request.status ?: TaskStatus.pendiente,
            priority = request.priority ?: TaskPriority.media,
            assignee = request.assignee,
            dueDate = TaskValidator.parseDueDate(request.dueDate.orEmpty()),
            now = now,
        )
        record.toDto()
    }

    override suspend fun list(query: TaskQuery): PaginatedTasksResponse = withContext(Dispatchers.IO) {
        val normalized = query.normalized()
        val (total, rows) = repository.findPage(normalized)
        PaginatedTasksResponse(
            data = rows.map { it.toDto() },
            meta = PageMeta(
                total = total,
                page = normalized.page,
                limit = normalized.limit,
                lastPage = lastPage(total, normalized.limit),
            ),
        )
    }

    override suspend fun get(id: String): TaskDto = withContext(Dispatchers.IO) {
        repository.findById(id)?.toDto() ?: throw TaskNotFoundException(id)
    }

    override suspend fun update(id: String, request: UpdateTaskRequest): TaskDto = withContext(Dispatchers.IO) {
        val errors = TaskValidator.validateUpdate(request)
        if (errors.isNotEmpty()) throw ApiValidationException(errors)

        val current: TaskRecord = repository.findById(id) ?: throw TaskNotFoundException(id)
        val updated = repository.update(
            current = current,
            title = request.title ?: current.title,
            description = request.description ?: current.description,
            status = request.status ?: TaskStatus.valueOf(current.status),
            priority = request.priority ?: TaskPriority.valueOf(current.priority),
            assignee = request.assignee ?: current.assignee,
            dueDate = request.dueDate?.let { TaskValidator.parseDueDate(it) } ?: current.dueDate,
            now = LocalDateTime.now(),
        )
        updated.toDto()
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            val deleted = repository.delete(id)
            if (!deleted) throw TaskNotFoundException(id)
        }
    }
}

fun lastPage(total: Long, limit: Int): Int = if (total <= 0L || limit <= 0) 0 else ((total + limit - 1L) / limit).toInt()