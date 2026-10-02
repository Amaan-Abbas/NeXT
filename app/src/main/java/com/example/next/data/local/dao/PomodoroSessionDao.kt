package com.example.next.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.next.data.local.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC")
    fun getSessions(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY startedAt DESC")
    fun getSessionsForTask(taskId: String): Flow<List<PomodoroSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSessionEntity)

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun clearAllSessions(): Int

    @Query("DELETE FROM pomodoro_sessions WHERE startedAt < :cutoffTimestamp")
    suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Int
}
