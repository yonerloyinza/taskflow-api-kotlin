package com.taskflow.api.validation

import com.taskflow.api.dto.CreateTaskRequest
import com.taskflow.api.dto.UpdateTaskRequest
import com.taskflow.api.model.TaskPriority
import com.taskflow.api.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskValidatorTest {

    @Test
    fun `create with minimal valid payload returns no errors`() {
        val errors = TaskValidator.validateCreate(
            CreateTaskRequest(
                title = "Nueva tarea",
                assignee = "ana",
                dueDate = "2026-12-01T10:00:00Z",
            ),
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `create rejects blank title`() {
        val errors = TaskValidator.validateCreate(
            CreateTaskRequest(title = "  ", assignee = "ana", dueDate = "2026-12-01T10:00:00Z"),
        )
        assertTrue(errors.any { it.contains("title") })
    }

    @Test
    fun `create rejects short title`() {
        val errors = TaskValidator.validateCreate(
            CreateTaskRequest(title = "ab", assignee = "ana", dueDate = "2026-12-01T10:00:00Z"),
        )
        assertTrue(errors.any { it.contains("at least 3") })
    }

    @Test
    fun `create rejects long title`() {
        val errors = TaskValidator.validateCreate(
            CreateTaskRequest(title = "a".repeat(101), assignee = "ana", dueDate = "2026-12-01T10:00:00Z"),
        )
        assertTrue(errors.any { it.contains("at most 100") })
    }

    @Test
    fun `create rejects blank assignee`() {
        val errors = TaskValidator.validateCreate(
            CreateTaskRequest(title = "Nueva tarea", assignee = "", dueDate = "2026-12-01T10:00:00Z"),
        )
        assertTrue(errors.any { it.contains("assignee is required") })
    }

    @Test
    fun `create rejects too long description`() {
        val errors = TaskValidator.validateCreate(
            CreateTaskRequest(
                title = "Nueva tarea",
                description = "d".repeat(501),
                assignee = "ana",
                dueDate = "2026-12-01T10:00:00Z",
            ),
        )
        assertTrue(errors.any { it.contains("description") })
    }

    @Test
    fun `create rejects missing due date`() {
        val errors = TaskValidator.validateCreate(CreateTaskRequest(title = "Nueva tarea", assignee = "ana"))
        assertTrue(errors.any { it.contains("dueDate is required") })
    }

    @Test
    fun `create rejects invalid due date`() {
        val errors = TaskValidator.validateCreate(
            CreateTaskRequest(title = "Nueva tarea", assignee = "ana", dueDate = "not-a-date"),
        )
        assertTrue(errors.any { it.contains("ISO-8601") })
    }

    @Test
    fun `update accepts empty payload`() {
        assertTrue(TaskValidator.validateUpdate(UpdateTaskRequest()).isEmpty())
    }

    @Test
    fun `update validates provided fields only`() {
        val errors = TaskValidator.validateUpdate(UpdateTaskRequest(title = "ab"))
        assertTrue(errors.any { it.contains("at least 3") })
    }

    @Test
    fun `parse due date normalises offsets to UTC`() {
        val parsed = TaskValidator.parseDueDate("2026-12-01T12:00:00+02:00")
        assertEquals("2026-12-01T10:00", parsed.toString())
    }

    @Test
    fun `parse due date accepts plain local date time`() {
        val parsed = TaskValidator.parseDueDate("2026-12-01T10:00:00")
        assertEquals("2026-12-01T10:00", parsed.toString())
    }

    @Test
    fun `parse due date returns null for invalid input`() {
        assertNull(TaskValidator.parseDueDateOrNull("nope"))
    }

    @Test
    fun `parse status and priority are case sensitive to contract values`() {
        assertEquals(TaskStatus.en_curso, TaskValidator.parseStatus("en_curso"))
        assertNull(TaskValidator.parseStatus("EN_CURSO"))
        assertEquals(TaskPriority.alta, TaskValidator.parsePriority("alta"))
        assertNull(TaskValidator.parsePriority("urgente"))
    }
}
