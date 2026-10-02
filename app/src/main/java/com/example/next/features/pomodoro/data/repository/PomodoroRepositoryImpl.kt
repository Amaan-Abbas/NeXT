package com.example.next.features.pomodoro.data.repository

import com.example.next.core.common.DispatcherProvider
import com.example.next.core.common.Result
import com.example.next.core.common.asResult
import com.example.next.features.pomodoro.data.datasource.PomodoroLocalDataSource
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PomodoroRepositoryImpl(
    private val localDataSource: PomodoroLocalDataSource,
    private val dispatchers: DispatcherProvider
) : PomodoroRepository {

    override fun getSessions(): Flow<Result<List<PomodoroSession>>> {
        return localDataSource.getSessionsFlow()
            .asResult()
            .flowOn(dispatchers.io)
    }

    override fun getSessionsForTask(taskId: String): Flow<Result<List<PomodoroSession>>> {
        return localDataSource.getSessionsForTaskFlow(taskId)
            .asResult()
            .flowOn(dispatchers.io)
    }

    override suspend fun saveSession(session: PomodoroSession): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.saveSession(session)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun clearAllSessions(): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.clearAllSessions()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.deleteSessionsOlderThan(cutoffTimestamp)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }
}
