package com.taskflow.api.config

data class AppConfig(
    val name: String,
    val environment: String,
    val port: Int,
    val corsOrigins: List<String>,
    val databaseUrl: String,
    val directUrl: String,
    val dbMaxConnections: Int,
    val rateLimitEnabled: Boolean,
    val rateLimitRequests: Int,
    val rateLimitWindowSeconds: Long,
) {
    companion object {
        fun fromEnv(): AppConfig = AppConfig(
            name = env("APP_NAME", "taskflow-api-kotlin"),
            environment = env("ENVIRONMENT", "production"),
            port = env("PORT", "3000").toIntOrNull() ?: DEFAULT_PORT,
            corsOrigins = env("CORS_ORIGINS", "*")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() },
            databaseUrl = requireEnv("DATABASE_URL"),
            directUrl = env("DIRECT_URL", env("DATABASE_URL", "")),
            dbMaxConnections = env("DB_MAX_CONNECTIONS", "10").toIntOrNull() ?: DEFAULT_POOL_SIZE,
            rateLimitEnabled = env("RATE_LIMIT_ENABLED", "true").toBoolean(),
            rateLimitRequests = env("RATE_LIMIT_REQUESTS", "100").toIntOrNull() ?: DEFAULT_RATE_LIMIT,
            rateLimitWindowSeconds = env("RATE_LIMIT_WINDOW_SECONDS", "60").toLongOrNull() ?: DEFAULT_RATE_WINDOW_SECONDS,
        )

        private const val DEFAULT_PORT = 3000
        private const val DEFAULT_POOL_SIZE = 10
        private const val DEFAULT_RATE_LIMIT = 100
        private const val DEFAULT_RATE_WINDOW_SECONDS = 60L

        private fun env(key: String, default: String): String = System.getenv(key)?.takeIf { it.isNotBlank() } ?: default

        private fun requireEnv(key: String): String = System.getenv(key)?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Missing required environment variable: $key")
    }
}