package com.example.todolist.feature.todolist.data.remote.dto

import com.example.todolist.feature.todolist.domain.model.TaskList


data class TaskListDto(
    val name: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val id: Long? = null,
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