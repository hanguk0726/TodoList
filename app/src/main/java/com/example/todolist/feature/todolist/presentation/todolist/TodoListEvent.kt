package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.ui.focus.FocusState

sealed class TodoListEvent{
    data class EnterTaskListName(val value: String): TodoListEvent()
    data class EnterTaskItemContent(val value: String): TodoListEvent()
    data class ChangeTaskListNameFocus(val focusState: FocusState): TodoListEvent()
    data class ChangeTaskItemContentFocus(val focusState: FocusState): TodoListEvent()
    object saveTaskList: TodoListEvent()
    object deleteTaskList: TodoListEvent()
    object completeTaskItem: TodoListEvent()
    object restoreTaskItemFromCompletion: TodoListEvent()
    object saveTaskItem: TodoListEvent()
    object deleteTaskItem: TodoListEvent()

}
