package com.example.next.features.tasks.domain.model

enum class TaskSortOption(val label: String) {
    DUE_DATE("Due Date"),
    PRIORITY("Priority"),
    CREATED_DATE("Newest First"),
    ALPHABETICAL("Title (A-Z)")
}
