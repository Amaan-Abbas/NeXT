package com.example.next.features.tasks.domain.model

data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val completedAt: Long? = null,
    val estimatedPomodoros: Int? = 1,
    val completedPomodoros: Int = 0
)
