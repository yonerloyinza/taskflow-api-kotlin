package com.taskflow.api

import com.taskflow.api.config.AppConfig
import com.taskflow.api.db.DatabaseFactory
import com.taskflow.api.db.Migrations
import com.taskflow.api.di.appModule
import com.taskflow.api.plugins.configureMonitoring
import com.taskflow.api.plugins.configureSecurity
import com.taskflow.api.plugins.configureSerialization
import com.taskflow.api.plugins.configureStatusPages
import com.taskflow.api.plugins.openApiRoutes
import com.taskflow.api.routes.healthRoutes
import com.taskflow.api.routes.taskRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.Netty
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import org.koin.ktor.plugin.Koin

fun main() {
    val config = AppConfig.fromEnv()
    embeddedServer(Netty, port = config.port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val config = AppConfig.fromEnv()
    val migrations = Migrations.apply(config.directUrl)
    val database = DatabaseFactory.connect(config)

    configureSerialization()
    configureMonitoring()
    configureSecurity(config)
    configureStatusPages()
    install(Koin) { modules(appModule(config, database)) }

    routing {
        healthRoutes(config, database)
        taskRoutes()
        openApiRoutes()
    }

    environment.log.info(
        "taskflow-api-kotlin started on port {} (environment: {}, flyway migrations applied: {})",
        config.port,
        config.environment,
        migrations,
    )
}
