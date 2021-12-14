package com.example.todolist.feature.todolist.presentation.todolist

import com.example.todolist.feature.todolist.domain.model.TaskList

data class TaskListsState(
    val taskLists: List<TaskList> = emptyList(),
)
