package com.taskflow.api.di

import com.taskflow.api.config.AppConfig
import com.taskflow.api.repository.TaskRepository
import com.taskflow.api.service.TaskService
import com.taskflow.api.service.TaskServiceImpl
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

fun appModule(config: AppConfig, database: Database) = module {
    single { config }
    single { database }
    single { TaskRepository(get()) }
    single<TaskService> { TaskServiceImpl(get()) }
}