package com.example.next.data.mapper

import com.example.next.data.local.entity.TaskEntity
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority

fun TaskEntity.toDomain(): Task {
    val domainPriority = try {
        TaskPriority.valueOf(priority.uppercase())
    } catch (_: Exception) {
        TaskPriority.MEDIUM
    }

    return Task(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        priority = domainPriority,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dueDate = dueDate,
        dueTime = dueTime,
        completedAt = completedAt,
        estimatedPomodoros = estimatedPomodoroSessions,
        completedPomodoros = completedPomodoroSessions
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        priority = priority.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dueDate = dueDate,
        dueTime = dueTime,
        completedAt = completedAt,
        estimatedPomodoroSessions = estimatedPomodoros,
        completedPomodoroSessions = completedPomodoros
    )
}

fun List<TaskEntity>.toDomainList(): List<Task> {
    return map { it.toDomain() }
}

fun List<Task>.toEntityList(): List<TaskEntity> {
    return map { it.toEntity() }
}
