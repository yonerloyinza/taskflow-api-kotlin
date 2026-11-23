package com.taskflow.api.model

import com.taskflow.api.dto.TaskDto
import java.time.LocalDateTime
import java.time.ZoneOffset

data class TaskRecord(
    val id: String,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,
    val assignee: String,
    val dueDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    fun toDto(): TaskDto = TaskDto(
        id = id,
        title = title,
        description = description,
        status = TaskStatus.valueOf(status),
        priority = TaskPriority.valueOf(priority),
        assignee = assignee,
        dueDate = formatDateTime(dueDate),
        createdAt = formatDateTime(createdAt),
        updatedAt = formatDateTime(updatedAt),
    )
}

fun formatDateTime(value: LocalDateTime): String = value.toInstant(ZoneOffset.UTC).toString()