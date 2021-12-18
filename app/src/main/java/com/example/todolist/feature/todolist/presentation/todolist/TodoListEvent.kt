package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.ui.focus.FocusState
import com.example.todolist.feature.todolist.domain.model.TaskItem


sealed class TodoListEvent{
    data class EnterTaskItemContent(val value: String): TodoListEvent()
    data class SelectTaskList(val selectedTaskListId: Long): TodoListEvent()
    data class GetTaskItemsByTaskListId(val taskListId: Long): TodoListEvent()
    data class CompleteTaskItem(val taskItem: TaskItem): TodoListEvent()
    object LoadLastSelectedTaskListPosition: TodoListEvent()
    object LastTaskListPositionHasSelected: TodoListEvent()
    object DeleteTaskList: TodoListEvent()
    object RestoreTaskItemFromCompletion: TodoListEvent()
    object SaveTaskItem: TodoListEvent()
    object DeleteTaskItem: TodoListEvent()

}
