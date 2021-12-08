package com.example.todolist.feature.todolist.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskItem(
    val content: String,
    val completed: Boolean,
    val timestamp: Long,
    val taskListId: Long,
    @PrimaryKey val id: Int? = null
)

class InvalidTaskItemException(message: String): Exception(message)