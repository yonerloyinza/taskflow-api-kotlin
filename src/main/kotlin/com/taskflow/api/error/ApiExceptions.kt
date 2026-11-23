package com.taskflow.api.error

class ApiValidationException(val errors: List<String>) : RuntimeException(errors.joinToString(", "))

class TaskNotFoundException(val id: String) : RuntimeException("Task with id $id was not found")
