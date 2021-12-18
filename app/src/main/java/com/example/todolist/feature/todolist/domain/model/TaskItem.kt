package com.example.todolist.feature.todolist.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskItem(
    val content: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val taskListId: Long,
    @PrimaryKey val id: Int? = null
)

class InvalidTaskItemException(message: String): Exception(message)