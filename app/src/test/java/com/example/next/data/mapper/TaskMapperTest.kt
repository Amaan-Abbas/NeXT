package com.example.next.data.mapper

import com.example.next.data.local.entity.TaskEntity
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskMapperTest {

    @Test
    fun `toDomain converts TaskEntity to Task domain model correctly`() {
        val entity = TaskEntity(
            id = "t1",
            title = "Test Task",
            description = "Test Description",
            isCompleted = true,
            priority = "HIGH",
            createdAt = 1000L,
            updatedAt = 2000L,
            dueDate = 3000L,
            dueTime = "10:00",
            completedAt = 2000L,
            estimatedPomodoroSessions = 4,
            completedPomodoroSessions = 2
        )

        val domain = entity.toDomain()

        assertEquals("t1", domain.id)
        assertEquals("Test Task", domain.title)
        assertEquals("Test Description", domain.description)
        assertTrue(domain.isCompleted)
        assertEquals(TaskPriority.HIGH, domain.priority)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
        assertEquals(3000L, domain.dueDate)
        assertEquals("10:00", domain.dueTime)
        assertEquals(2000L, domain.completedAt)
        assertEquals(4, domain.estimatedPomodoros)
        assertEquals(2, domain.completedPomodoros)
    }

    @Test
    fun `toEntity converts Task domain model to TaskEntity correctly`() {
        val domain = Task(
            id = "t2",
            title = "Task Two",
            description = "Desc Two",
            isCompleted = false,
            priority = TaskPriority.LOW,
            createdAt = 5000L,
            updatedAt = 6000L,
            estimatedPomodoros = 3,
            completedPomodoros = 1
        )

        val entity = domain.toEntity()

        assertEquals("t2", entity.id)
        assertEquals("Task Two", entity.title)
        assertEquals("Desc Two", entity.description)
        assertEquals(false, entity.isCompleted)
        assertEquals("LOW", entity.priority)
        assertEquals(5000L, entity.createdAt)
        assertEquals(6000L, entity.updatedAt)
        assertEquals(3, entity.estimatedPomodoroSessions)
        assertEquals(1, entity.completedPomodoroSessions)
    }
}
