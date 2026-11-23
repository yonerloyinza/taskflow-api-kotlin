package com.taskflow.api.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Tasks : Table("\"Task\"") {
    val id = varchar("id", 36)
    val title = varchar("title", 100)
    val description = varchar("description", 500).nullable()
    val status = varchar("status", 20)
    val priority = varchar("priority", 10)
    val assignee = varchar("assignee", 100)
    val dueDate = datetime("dueDate")
    val createdAt = datetime("createdAt")
    val updatedAt = datetime("updatedAt")
}
