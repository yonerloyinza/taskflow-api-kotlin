package com.taskflow.api.repository

import com.taskflow.api.db.Tasks
import com.taskflow.api.model.TaskPriority
import com.taskflow.api.model.TaskRecord
import com.taskflow.api.model.TaskStatus
import com.taskflow.api.service.TaskQuery
import java.time.LocalDateTime
import java.util.UUID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class TaskRepository(private val database: Database) {

    fun findById(id: String): TaskRecord? = transaction(database) {
        Tasks.selectAll().where { Tasks.id eq id }.singleOrNull()?.toRecord()
    }

    fun findPage(query: TaskQuery): Pair<Long, List<TaskRecord>> = transaction(database) {
        val predicate = query.predicate()
        val total = if (predicate == null) {
            Tasks.selectAll().count()
        } else {
            Tasks.selectAll().where(predicate).count()
        }
        val rows = (if (predicate == null) Tasks.selectAll() else Tasks.selectAll().where(predicate))
            .orderBy(Tasks.createdAt, SortOrder.DESC)
            .limit(query.limit, query.offset())
            .map { it.toRecord() }
        total to rows
    }

    fun insert(
        title: String,
        description: String?,
        status: TaskStatus,
        priority: TaskPriority,
        assignee: String,
        dueDate: LocalDateTime,
        now: LocalDateTime,
    ): TaskRecord = transaction(database) {
        val record = TaskRecord(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            status = status.name,
            priority = priority.name,
            assignee = assignee,
            dueDate = dueDate,
            createdAt = now,
            updatedAt = now,
        )
        Tasks.insert {
            it[Tasks.id] = record.id
            it[Tasks.title] = record.title
            it[Tasks.description] = record.description
            it[Tasks.status] = record.status
            it[Tasks.priority] = record.priority
            it[Tasks.assignee] = record.assignee
            it[Tasks.dueDate] = record.dueDate
            it[Tasks.createdAt] = record.createdAt
            it[Tasks.updatedAt] = record.updatedAt
        }
        record
    }

    fun update(
        current: TaskRecord,
        title: String,
        description: String?,
        status: TaskStatus,
        priority: TaskPriority,
        assignee: String,
        dueDate: LocalDateTime,
        now: LocalDateTime,
    ): TaskRecord = transaction(database) {
        val updated = TaskRecord(
            id = current.id,
            title = title,
            description = description,
            status = status.name,
            priority = priority.name,
            assignee = assignee,
            dueDate = dueDate,
            createdAt = current.createdAt,
            updatedAt = now,
        )
        Tasks.update({ Tasks.id eq current.id }) {
            it[Tasks.title] = updated.title
            it[Tasks.description] = updated.description
            it[Tasks.status] = updated.status
            it[Tasks.priority] = updated.priority
            it[Tasks.assignee] = updated.assignee
            it[Tasks.dueDate] = updated.dueDate
            it[Tasks.updatedAt] = updated.updatedAt
        }
        updated
    }

    fun delete(id: String): Boolean = transaction(database) {
        Tasks.deleteWhere { with(it) { Tasks.id eq id } } > 0
    }

    private fun TaskQuery.predicate(): Op<Boolean>? = with(SqlExpressionBuilder) {
        val conditions = buildList {
            status?.let { value -> add(Tasks.status eq value.name) }
            priority?.let { value -> add(Tasks.priority eq value.name) }
            assignee?.takeIf { it.isNotBlank() }?.let { value -> add(Tasks.assignee eq value) }
        }
        conditions.reduceOrNull { accumulator, condition -> accumulator and condition }
    }
}

private fun ResultRow.toRecord(): TaskRecord = TaskRecord(
    id = this[Tasks.id],
    title = this[Tasks.title],
    description = this[Tasks.description],
    status = this[Tasks.status],
    priority = this[Tasks.priority],
    assignee = this[Tasks.assignee],
    dueDate = this[Tasks.dueDate],
    createdAt = this[Tasks.createdAt],
    updatedAt = this[Tasks.updatedAt],
)
