package com.example.next.data.mapper

import com.example.next.data.local.entity.PomodoroSessionEntity
import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PomodoroSessionMapperTest {

    @Test
    fun `entity to domain and domain to entity mapping is bi-directional`() {
        val domainModel = PomodoroSession(
            id = "sess-123",
            taskId = "task-456",
            sessionType = PomodoroMode.FOCUS,
            startedAt = 1000L,
            endedAt = 2500L,
            plannedDurationSeconds = 1500,
            actualDurationSeconds = 1500,
            isCompleted = true,
            isInterrupted = false
        )

        val entity = domainModel.toEntity()
        assertEquals("sess-123", entity.id)
        assertEquals("task-456", entity.taskId)
        assertEquals("FOCUS", entity.sessionType)
        assertEquals(1500, entity.plannedDurationSeconds)

        val convertedDomain = entity.toDomain()
        assertEquals(domainModel, convertedDomain)
    }
}
