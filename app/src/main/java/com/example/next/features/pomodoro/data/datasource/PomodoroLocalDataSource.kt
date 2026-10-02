package com.example.next.features.pomodoro.data.datasource

import com.example.next.data.local.dao.PomodoroSessionDao
import com.example.next.data.mapper.toDomainList
import com.example.next.data.mapper.toEntity
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class PomodoroLocalDataSource(
    private val pomodoroSessionDao: PomodoroSessionDao
) {
    fun getSessionsFlow(): Flow<List<PomodoroSession>> {
        return pomodoroSessionDao.getSessions().map { entities ->
            entities.toDomainList()
        }
    }

    fun getSessionsForTaskFlow(taskId: String): Flow<List<PomodoroSession>> {
        return pomodoroSessionDao.getSessionsForTask(taskId).map { entities ->
            entities.toDomainList()
        }
    }

    suspend fun saveSession(session: PomodoroSession) {
        val newId = if (session.id.isBlank()) UUID.randomUUID().toString() else session.id
        val sessionToSave = session.copy(id = newId)
        pomodoroSessionDao.insertSession(sessionToSave.toEntity())
    }

    suspend fun clearAllSessions() {
        pomodoroSessionDao.clearAllSessions()
    }

    suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long) {
        pomodoroSessionDao.deleteSessionsOlderThan(cutoffTimestamp)
    }
}
