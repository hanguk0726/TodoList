package com.example.todolist.feature.todolist.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskItem(
    val title: String,
    val detail: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val taskListId: Long,
    val isSynchronizedWithRemote: Boolean = false,
    val needToBeDeleted: Boolean = false,
    @PrimaryKey val id: Long? = null
)

class InvalidTaskItemException(message: String): Exception(message)