package com.example.todolist.feature.todolist.data.remote.dto

import com.example.todolist.feature.todolist.domain.model.TaskList
import com.google.gson.annotations.SerializedName


data class TaskListDto(
    val name: String,
    @SerializedName("created_timestamp")
    val createdTimestamp: Long = System.currentTimeMillis(),
    val id: Long? = null,
    @SerializedName("user_id")
    val userId: String
)

fun TaskListDto.toTaskList() : TaskList {
    return TaskList(
        name = name,
        createdTimestamp = createdTimestamp,
        id = id
    )
}


fun TaskList.toTaskListDto(userId: String) : TaskListDto {
    return TaskListDto(
        name = name,
        createdTimestamp = createdTimestamp,
        id = id,
        userId = userId
    )
}