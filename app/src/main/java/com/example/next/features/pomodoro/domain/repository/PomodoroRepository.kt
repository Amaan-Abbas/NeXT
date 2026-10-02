package com.example.next.features.pomodoro.domain.repository

import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow

interface PomodoroRepository {
    fun getSessions(): Flow<Result<List<PomodoroSession>>>
    fun getSessionsForTask(taskId: String): Flow<Result<List<PomodoroSession>>>
    suspend fun saveSession(session: PomodoroSession): Result<Unit>
    suspend fun clearAllSessions(): Result<Unit>
    suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Result<Unit>
}
