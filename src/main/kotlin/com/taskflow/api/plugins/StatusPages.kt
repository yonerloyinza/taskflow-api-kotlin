package com.taskflow.api.plugins

import com.taskflow.api.dto.errorResponse
import com.taskflow.api.error.ApiValidationException
import com.taskflow.api.error.TaskNotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.request.uri
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                errorResponse(
                    statusCode = HttpStatusCode.BadRequest.value,
                    messages = cause.errors,
                    path = call.request.uri,
                ),
            )
        }

        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                errorResponse(
                    statusCode = HttpStatusCode.BadRequest.value,
                    message = cause.message ?: "Bad request",
                    path = call.request.uri,
                ),
            )
        }

        exception<TaskNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                errorResponse(
                    statusCode = HttpStatusCode.NotFound.value,
                    message = cause.message ?: "Task not found",
                    path = call.request.uri,
                ),
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception while processing ${call.request.uri}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                errorResponse(
                    statusCode = HttpStatusCode.InternalServerError.value,
                    message = "Internal server error",
                    path = call.request.uri,
                ),
            )
        }
    }
}
