package com.taskflow.api.validation

import com.taskflow.api.dto.CreateTaskRequest
import com.taskflow.api.dto.UpdateTaskRequest
import com.taskflow.api.error.ApiValidationException
import com.taskflow.api.model.TaskPriority
import com.taskflow.api.model.TaskStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

object TaskValidator {

    const val TITLE_MIN_LENGTH = 3
    const val TITLE_MAX_LENGTH = 100
    const val DESCRIPTION_MAX_LENGTH = 500
    const val ASSIGNEE_MAX_LENGTH = 100

    fun validateCreate(request: CreateTaskRequest): List<String> {
        val errors = mutableListOf<String>()
        errors += titleErrors(request.title)
        request.description?.let { if (it.length > DESCRIPTION_MAX_LENGTH) errors += descriptionError() }
        if (request.assignee.isBlank()) {
            errors += "assignee is required"
        } else if (request.assignee.length > ASSIGNEE_MAX_LENGTH) {
            errors += assigneeError()
        }
        if (request.dueDate.isNullOrBlank()) {
            errors += "dueDate is required"
        } else if (parseDueDateOrNull(request.dueDate) == null) {
            errors += dueDateError()
        }
        return errors
    }

    fun validateUpdate(request: UpdateTaskRequest): List<String> {
        val errors = mutableListOf<String>()
        request.title?.let { errors += titleErrors(it) }
        request.description?.let { if (it.length > DESCRIPTION_MAX_LENGTH) errors += descriptionError() }
        request.assignee?.let {
            if (it.isBlank()) {
                errors += "assignee cannot be blank"
            } else if (it.length > ASSIGNEE_MAX_LENGTH) {
                errors += assigneeError()
            }
        }
        request.dueDate?.let { if (parseDueDateOrNull(it) == null) errors += dueDateError() }
        return errors
    }

    fun parseDueDate(raw: String): LocalDateTime = parseDueDateOrNull(raw) ?: throw ApiValidationException(listOf(dueDateError()))

    fun parseDueDateOrNull(raw: String): LocalDateTime? {
        val value = raw.trim()
        if (value.isEmpty()) return null
        return runCatching { OffsetDateTime.parse(value) }
            .recoverCatching { Instant.parse(value).atOffset(ZoneOffset.UTC) }
            .recoverCatching { LocalDateTime.parse(value).atOffset(ZoneOffset.UTC) }
            .map { it.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() }
            .getOrNull()
    }

    fun parseStatus(value: String): TaskStatus? = TaskStatus.entries.firstOrNull { it.name == value }

    fun parsePriority(value: String): TaskPriority? = TaskPriority.entries.firstOrNull { it.name == value }

    private fun titleErrors(title: String): List<String> {
        val errors = mutableListOf<String>()
        when {
            title.isBlank() -> errors += "title is required"
            title.length < TITLE_MIN_LENGTH -> errors += "title must be at least $TITLE_MIN_LENGTH characters"
            title.length > TITLE_MAX_LENGTH -> errors += "title must be at most $TITLE_MAX_LENGTH characters"
        }
        return errors
    }

    private fun descriptionError(): String = "description must be at most $DESCRIPTION_MAX_LENGTH characters"

    private fun assigneeError(): String = "assignee must be at most $ASSIGNEE_MAX_LENGTH characters"

    private fun dueDateError(): String = "dueDate must be a valid ISO-8601 date-time"
}
