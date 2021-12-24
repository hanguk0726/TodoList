package com.example.todolist.feature.todolist.presentation.addEditTaskList

import androidx.compose.ui.focus.FocusState
import com.example.todolist.feature.todolist.presentation.todolist.TodoListEvent

sealed class AddEditTaskListEvent {
    data class EnterTaskListName(val value: String): AddEditTaskListEvent()
    data class SaveTaskList(val taskListId: Long): AddEditTaskListEvent()
}
