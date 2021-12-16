package com.example.todolist.feature.todolist.presentation.todolist

import com.example.todolist.feature.todolist.domain.model.TaskList
import kotlinx.coroutines.Job

data class TaskListsState(
    val taskLists: List<TaskList> = emptyList(),
)

