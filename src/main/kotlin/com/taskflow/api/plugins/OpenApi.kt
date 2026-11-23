package com.taskflow.api.plugins

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.plugins.swagger.swaggerUI

fun Route.openApiRoutes() {
    swaggerUI(path = "swagger", swaggerFile = "openapi.yaml")

    get("/openapi.yaml") {
        val resource = javaClass.classLoader.getResourceAsStream("openapi.yaml")
        if (resource == null) {
            call.respondText("openapi.yaml not found", ContentType.Text.Plain, HttpStatusCode.NotFound)
            return@get
        }
        call.respondText(resource.bufferedReader().use { it.readText() }, ContentType.Text.Plain)
    }
}