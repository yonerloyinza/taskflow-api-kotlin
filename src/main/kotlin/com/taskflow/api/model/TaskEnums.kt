package com.taskflow.api.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    pendiente,
    en_curso,
    hecha,
}

@Serializable
enum class TaskPriority {
    baja,
    media,
    alta,
}
