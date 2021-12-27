package com.example.todolist.feature.todolist.data.remote.dto

import com.example.todolist.feature.todolist.domain.model.TaskItem


data class TaskItemDto(
    val title: String,
    val detail: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val taskListId: Long,
    val id: Long? = null,
    val userId: Long
)


fun TaskItemDto.toTaskItem() : TaskItem {
    return TaskItem(
        title = title,
        detail = detail,
        isCompleted = isCompleted,
        timestamp = timestamp,
        taskListId = taskListId,
        id = id
    )
}

fun TaskItem.toTaskItemDto(userId: Long) : TaskItemDto {
    return TaskItemDto(
        title = title,
        detail = detail,
        isCompleted = isCompleted,
        timestamp = timestamp,
        taskListId = taskListId,
        id = id,
        userId = userId
    )
}