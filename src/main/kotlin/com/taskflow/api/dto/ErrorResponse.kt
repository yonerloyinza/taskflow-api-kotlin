package com.taskflow.api.dto

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant

@Serializable
data class ErrorResponse(
    val statusCode: Int,
    val error: String,
    val message: JsonElement,
    val timestamp: String,
    val path: String,
)

fun errorResponse(statusCode: Int, message: String, path: String): ErrorResponse = build(
    statusCode = statusCode,
    message = JsonPrimitive(message),
    path = path,
)

fun errorResponse(statusCode: Int, messages: List<String>, path: String): ErrorResponse = build(
    statusCode = statusCode,
    message = JsonArray(messages.map { JsonPrimitive(it) }),
    path = path,
)

private fun build(statusCode: Int, message: JsonElement, path: String): ErrorResponse = ErrorResponse(
    statusCode = statusCode,
    error = HttpStatusCode.fromValue(statusCode)?.description ?: "Error",
    message = message,
    timestamp = Instant.now().toString(),
    path = path,
)