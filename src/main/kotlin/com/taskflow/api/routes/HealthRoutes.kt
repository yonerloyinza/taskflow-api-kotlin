package com.taskflow.api.routes

import com.taskflow.api.config.AppConfig
import com.taskflow.api.dto.HealthResponse
import com.taskflow.api.dto.errorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.time.Instant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.healthRoutes(config: AppConfig, database: Database) {
    get("/health") {
        val reachable = isDatabaseReachable(database)
        if (reachable) {
            call.respond(
                HttpStatusCode.OK,
                HealthResponse(
                    status = "ok",
                    service = config.name,
                    database = "up",
                    timestamp = Instant.now().toString(),
                ),
            )
        } else {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                errorResponse(
                    statusCode = HttpStatusCode.ServiceUnavailable.value,
                    message = "Database connection failed",
                    path = call.request.uri,
                ),
            )
        }
    }
}

private fun isDatabaseReachable(database: Database): Boolean = runCatching {
    transaction(database) { exec("SELECT 1") }
}.isSuccess
