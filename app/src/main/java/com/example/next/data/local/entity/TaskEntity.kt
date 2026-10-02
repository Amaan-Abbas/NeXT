package com.example.next.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val completedAt: Long? = null,
    val estimatedPomodoroSessions: Int? = 1,
    val completedPomodoroSessions: Int = 0
)
