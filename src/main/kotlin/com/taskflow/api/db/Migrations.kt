package com.taskflow.api.db

import org.flywaydb.core.Flyway

object Migrations {

    fun apply(directUrl: String): Int {
        val settings = DatabaseUrl.parse(directUrl)
        val flyway = Flyway.configure()
            .dataSource(settings.url, settings.user ?: "", settings.password ?: "")
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .validateOnMigrate(true)
            .load()

        return flyway.migrate().migrationsExecuted
    }
}
