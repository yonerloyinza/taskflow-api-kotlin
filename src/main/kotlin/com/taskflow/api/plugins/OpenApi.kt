package com.taskflow.api.plugins

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.yaml.snakeyaml.Yaml

private const val OPENAPI_RESOURCE = "openapi.yaml"

fun Route.openApiRoutes() {
    swaggerUI(path = "swagger", swaggerFile = OPENAPI_RESOURCE)
    swaggerUI(path = "docs", swaggerFile = OPENAPI_RESOURCE)

    get("/openapi.yaml") {
        val spec = loadSpec()
        if (spec == null) {
            call.respondText("openapi.yaml not found", ContentType.Text.Plain, HttpStatusCode.NotFound)
            return@get
        }
        call.respondText(spec, ContentType.Text.Plain)
    }

    get("/openapi") {
        val spec = loadSpec()
        if (spec == null) {
            call.respondText("openapi.yaml not found", ContentType.Text.Plain, HttpStatusCode.NotFound)
            return@get
        }
        call.respondText(toJson(spec), ContentType.Application.Json)
    }
}

private fun loadSpec(): String? = javaClass.classLoader.getResourceAsStream(OPENAPI_RESOURCE)
    ?.bufferedReader()
    ?.use { it.readText() }

private fun toJson(spec: String): String {
    val document: Map<String, Any?> = Yaml().load(spec)
    return Json.encodeToString(JsonElement.serializer(), document.toJsonElement())
}

private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Map<*, *> -> JsonObject(entries.associate { (key, value) -> key.toString() to value.toJsonElement() })
    is List<*> -> JsonArray(map { it.toJsonElement() })
    else -> JsonPrimitive(toString())
}
