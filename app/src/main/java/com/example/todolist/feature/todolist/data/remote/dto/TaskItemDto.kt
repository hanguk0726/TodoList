package com.example.todolist.feature.todolist.data.remote.dto

import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.google.gson.annotations.SerializedName


data class TaskItemDto(
    val title: String,
    val detail: String = "",
    @SerializedName("is_completed")
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("task_list_id")
    val taskListId: Long,
    val id: Long? = null,
    @SerializedName("user_id")
    val userId: String
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

fun TaskItem.toTaskItemDto(userId: String) : TaskItemDto {
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