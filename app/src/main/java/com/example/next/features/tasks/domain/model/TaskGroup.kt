package com.example.next.features.tasks.domain.model

enum class TaskGroup(val title: String) {
    OVERDUE("Overdue"),
    TODAY("Due Today"),
    UPCOMING("Upcoming"),
    NO_DUE_DATE("No Due Date");

    companion object {
        fun getGroupForTask(task: Task, startOfTodayMillis: Long, endOfTodayMillis: Long): TaskGroup {
            val dueDate = task.dueDate ?: return NO_DUE_DATE
            return when {
                dueDate < startOfTodayMillis -> OVERDUE
                dueDate in startOfTodayMillis..endOfTodayMillis -> TODAY
                else -> UPCOMING
            }
        }
    }
}
