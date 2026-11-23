package com.taskflow.api.plugins

import com.taskflow.api.config.AppConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.ratelimit.RateLimit
import kotlin.time.Duration.Companion.seconds

fun Application.configureSecurity(config: AppConfig) {
    install(CORS) {
        if (config.corsOrigins.contains(ANY_ORIGIN)) {
            anyHost()
        } else {
            config.corsOrigins.forEach { origin -> allowHost(origin.substringAfter("://")) }
        }
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
    }

    install(DefaultHeaders) {
        header("X-Content-Type-Options", "nosniff")
        header("X-Frame-Options", "DENY")
        header("Referrer-Policy", "no-referrer")
        header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
    }

    install(RateLimit) {
        register {
            rateLimiter(
                limit = if (config.rateLimitEnabled) config.rateLimitRequests else Int.MAX_VALUE,
                refillPeriod = config.rateLimitWindowSeconds.seconds,
            )
            requestKey { call -> call.request.local.remoteHost }
        }
    }
}

private const val ANY_ORIGIN = "*"
