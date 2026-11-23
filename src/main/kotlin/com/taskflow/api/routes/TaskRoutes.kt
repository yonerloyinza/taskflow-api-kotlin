package com.taskflow.api.routes

import com.taskflow.api.dto.CreateTaskRequest
import com.taskflow.api.dto.UpdateTaskRequest
import com.taskflow.api.error.ApiValidationException
import com.taskflow.api.model.TaskPriority
import com.taskflow.api.model.TaskStatus
import com.taskflow.api.service.TaskQuery
import com.taskflow.api.service.TaskService
import com.taskflow.api.validation.TaskValidator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.taskRoutes() {
    rateLimit {
        route("/tasks") {
            post {
                val service: TaskService by call.inject()
                val request = call.receive<CreateTaskRequest>()
                call.respond(HttpStatusCode.Created, service.create(request))
            }

            get {
                val service: TaskService by call.inject()
                call.respond(HttpStatusCode.OK, service.list(queryOf(call)))
            }

            route("/{id}") {
                get {
                    val service: TaskService by call.inject()
                    call.respond(HttpStatusCode.OK, service.get(call.taskId()))
                }

                patch {
                    val service: TaskService by call.inject()
                    val request = call.receive<UpdateTaskRequest>()
                    call.respond(HttpStatusCode.OK, service.update(call.taskId(), request))
                }

                delete {
                    val service: TaskService by call.inject()
                    service.delete(call.taskId())
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

private fun ApplicationCall.taskId(): String = parameters["id"].orEmpty()

private fun queryOf(call: ApplicationCall): TaskQuery {
    val params = call.request.queryParameters
    val errors = mutableListOf<String>()

    val status = params["status"]?.let { raw ->
        TaskValidator.parseStatus(raw).also { parsed ->
            if (parsed == null) errors += statusError()
        }
    }
    val priority = params["priority"]?.let { raw ->
        TaskValidator.parsePriority(raw).also { parsed ->
            if (parsed == null) errors += priorityError()
        }
    }

    fun intParameter(name: String, default: Int, max: Int?): Int {
        val raw = params[name] ?: return default
        val parsed = raw.toIntOrNull()
        if (parsed == null || parsed < 1 || (max != null && parsed > max)) {
            errors += if (max == null) {
                "$name must be an integer greater than or equal to 1"
            } else {
                "$name must be an integer between 1 and $max"
            }
            return default
        }
        return parsed
    }

    val page = intParameter("page", TaskQuery.DEFAULT_PAGE, null)
    val limit = intParameter("limit", TaskQuery.DEFAULT_LIMIT, TaskQuery.MAX_LIMIT)

    if (errors.isNotEmpty()) throw ApiValidationException(errors)

    return TaskQuery(
        status = status,
        priority = priority,
        assignee = params["assignee"],
        page = page,
        limit = limit,
    )
}

private fun statusError(): String = "status must be one of: ${TaskStatus.entries.joinToString(", ")}"

private fun priorityError(): String = "priority must be one of: ${TaskPriority.entries.joinToString(", ")}"
