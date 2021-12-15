package com.example.todolist.feature.todolist.presentation.todolist

import com.example.todolist.feature.todolist.domain.model.TaskItem

data class TaskItemState(
    val taskItems: List<TaskItem> = emptyList()
)
