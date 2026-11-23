package com.taskflow.api.dto

import com.taskflow.api.model.TaskPriority
import com.taskflow.api.model.TaskStatus
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignee: String,
    val dueDate: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class CreateTaskRequest(
    val title: String = "",
    val description: String? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val assignee: String = "",
    val dueDate: String? = null,
)

@Serializable
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val assignee: String? = null,
    val dueDate: String? = null,
)

@Serializable
data class PaginatedTasksResponse(
    val data: List<TaskDto>,
    val meta: PageMeta,
)

@Serializable
data class PageMeta(
    val total: Long,
    val page: Int,
    val limit: Int,
    val lastPage: Int,
)

@Serializable
data class HealthResponse(
    val status: String,
    val service: String,
    val database: String,
    val timestamp: String,
)
