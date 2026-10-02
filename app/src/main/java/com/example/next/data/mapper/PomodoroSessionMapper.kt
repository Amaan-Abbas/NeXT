package com.example.next.data.mapper

import com.example.next.data.local.entity.PomodoroSessionEntity
import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.PomodoroSession

fun PomodoroSessionEntity.toDomain(): PomodoroSession {
    val mode = try {
        PomodoroMode.valueOf(sessionType.uppercase())
    } catch (_: Exception) {
        PomodoroMode.FOCUS
    }

    return PomodoroSession(
        id = id,
        taskId = taskId,
        sessionType = mode,
        startedAt = startedAt,
        endedAt = endedAt,
        plannedDurationSeconds = plannedDurationSeconds,
        actualDurationSeconds = actualDurationSeconds,
        isCompleted = isCompleted,
        isInterrupted = isInterrupted
    )
}

fun PomodoroSession.toEntity(): PomodoroSessionEntity {
    return PomodoroSessionEntity(
        id = id,
        taskId = taskId,
        sessionType = sessionType.name,
        startedAt = startedAt,
        endedAt = endedAt,
        plannedDurationSeconds = plannedDurationSeconds,
        actualDurationSeconds = actualDurationSeconds,
        isCompleted = isCompleted,
        isInterrupted = isInterrupted
    )
}

fun List<PomodoroSessionEntity>.toDomainList(): List<PomodoroSession> {
    return map { it.toDomain() }
}

fun List<PomodoroSession>.toEntityList(): List<PomodoroSessionEntity> {
    return map { it.toEntity() }
}
