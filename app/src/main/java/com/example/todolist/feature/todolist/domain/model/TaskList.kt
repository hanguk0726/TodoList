package com.example.todolist.feature.todolist.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskList(
    val name: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val isSynchronizedWithRemote: Boolean = false,
    val needToBeDeleted: Boolean = false,
    @PrimaryKey val id: Long? = null
)

class InvalidTaskListException(message: String): Exception(message)