package com.taskflow.api.db

import com.taskflow.api.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    fun connect(config: AppConfig): Database {
        val settings = DatabaseUrl.parse(config.databaseUrl)
        val hikari = HikariConfig().apply {
            jdbcUrl = settings.url
            username = settings.user
            password = settings.password
            maximumPoolSize = config.dbMaxConnections
            minimumIdle = 1
            connectionTimeout = CONNECTION_TIMEOUT_MS
            idleTimeout = IDLE_TIMEOUT_MS
            maxLifetime = MAX_LIFETIME_MS
            poolName = POOL_NAME
            connectionTestQuery = "SELECT 1"
        }
        return Database.connect(HikariDataSource(hikari))
    }

    private const val CONNECTION_TIMEOUT_MS = 10_000L
    private const val IDLE_TIMEOUT_MS = 60_000L
    private const val MAX_LIFETIME_MS = 600_000L
    private const val POOL_NAME = "taskflow-kotlin-pool"
}
