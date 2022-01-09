package com.example.todolist.feature.todolist.presentation.todolist

import com.example.todolist.feature.todolist.domain.model.TaskItem


sealed class TodoListEvent{
    data class EnterTaskItemTitle(val value: String): TodoListEvent()
    data class SelectTaskList(val selectedTaskListId: Long): TodoListEvent()
    data class ConfirmDeleteTaskList(val selectedTaskListId: Long): TodoListEvent()
    data class ConfirmDeleteCompletedTaskItems(val selectedTaskListId: Long): TodoListEvent()
    data class ToggleTaskItemCompletionState(val taskItem: TaskItem): TodoListEvent()
    data class DeleteTaskList(val selectedTaskListId: Long) : TodoListEvent()
    data class SaveTaskItem(val selectedTaskListId: Long) : TodoListEvent()
    data class DeleteCompletedTaskItems(val selectedTaskListId: Long) : TodoListEvent()
    object LoadLastSelectedTaskListPosition: TodoListEvent()
    object LastTaskListPositionHasSelected: TodoListEvent()


}
