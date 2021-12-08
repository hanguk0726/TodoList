package com.example.todolist.feature.todolist.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskList(
    val name: String,
    val lastModificationTimestamp: Long,
    @PrimaryKey val id: Int? = null
)

class InvalidTaskListException(message: String): Exception(message)