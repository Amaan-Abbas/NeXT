package com.example.next.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey
    val id: String,
    val taskId: String?,
    val sessionType: String,
    val startedAt: Long,
    val endedAt: Long?,
    val plannedDurationSeconds: Int,
    val actualDurationSeconds: Int,
    val isCompleted: Boolean,
    val isInterrupted: Boolean
)
