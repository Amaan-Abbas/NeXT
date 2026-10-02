package com.example.next.features.tasks.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.features.tasks.domain.model.TaskSortOption
import com.example.next.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetTasksUseCaseTest {

    private val sampleTasks = listOf(
        Task(
            id = "t1",
            title = "B Task",
            description = "Desc 1",
            priority = TaskPriority.HIGH,
            dueDate = 2000L,
            createdAt = 100L,
            isCompleted = false
        ),
        Task(
            id = "t2",
            title = "A Task",
            description = "Desc 2",
            priority = TaskPriority.LOW,
            dueDate = 1000L,
            createdAt = 500L,
            isCompleted = true
        ),
        Task(
            id = "t3",
            title = "C Task",
            description = "Desc 3",
            priority = TaskPriority.MEDIUM,
            dueDate = null,
            createdAt = 300L,
            isCompleted = false
        )
    )

    private val fakeRepository = object : TaskRepository {
        override fun getTasks(): Flow<Result<List<Task>>> = flowOf(Result.Success(sampleTasks))
        override fun getTaskById(id: String): Flow<Result<Task?>> = flowOf(Result.Success(sampleTasks.find { it.id == id }))
        override suspend fun saveTask(task: Task): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTask(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> = Result.Success(Unit)
        override fun searchTasks(query: String): Flow<Result<List<Task>>> = flowOf(Result.Success(sampleTasks.filter { it.title.contains(query, ignoreCase = true) }))
        override suspend fun clearAllTasks(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private val useCase = GetTasksUseCase(fakeRepository)

    @Test
    fun `invoke with no filters returns all tasks`() = runBlocking {
        val result = useCase().first()
        assertTrue(result is Result.Success)
        val tasks = (result as Result.Success).data
        assertEquals(3, tasks.size)
    }

    @Test
    fun `invoke with HIGH priority filter returns only high priority tasks`() = runBlocking {
        val result = useCase(priorityFilter = TaskPriority.HIGH).first()
        assertTrue(result is Result.Success)
        val tasks = (result as Result.Success).data
        assertEquals(1, tasks.size)
        assertEquals("t1", tasks.first().id)
    }

    @Test
    fun `invoke with search query filters tasks by title`() = runBlocking {
        val result = useCase(searchQuery = "A Task").first()
        assertTrue(result is Result.Success)
        val tasks = (result as Result.Success).data
        assertEquals(1, tasks.size)
        assertEquals("t2", tasks.first().id)
    }

    @Test
    fun `sorting by DUE_DATE places earliest due date first and nulls last`() = runBlocking {
        val result = useCase(sortOption = TaskSortOption.DUE_DATE).first()
        val tasks = (result as Result.Success).data
        assertEquals("t2", tasks[0].id) // 1000L
        assertEquals("t1", tasks[1].id) // 2000L
        assertEquals("t3", tasks[2].id) // null
    }

    @Test
    fun `sorting by PRIORITY places HIGH priority first`() = runBlocking {
        val result = useCase(sortOption = TaskSortOption.PRIORITY).first()
        val tasks = (result as Result.Success).data
        assertEquals("t1", tasks[0].id) // HIGH
        assertEquals("t3", tasks[1].id) // MEDIUM
        assertEquals("t2", tasks[2].id) // LOW
    }

    @Test
    fun `sorting by ALPHABETICAL sorts titles A to Z`() = runBlocking {
        val result = useCase(sortOption = TaskSortOption.ALPHABETICAL).first()
        val tasks = (result as Result.Success).data
        assertEquals("t2", tasks[0].id) // "A Task"
        assertEquals("t1", tasks[1].id) // "B Task"
        assertEquals("t3", tasks[2].id) // "C Task"
    }

    @Test
    fun `sorting by CREATED_DATE sorts newest first`() = runBlocking {
        val result = useCase(sortOption = TaskSortOption.CREATED_DATE).first()
        val tasks = (result as Result.Success).data
        assertEquals("t2", tasks[0].id) // 500L
        assertEquals("t3", tasks[1].id) // 300L
        assertEquals("t1", tasks[2].id) // 100L
    }
}
