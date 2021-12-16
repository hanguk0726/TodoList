package com.example.todolist.feature.todolist.presentation.todolist


sealed class TodoListEvent{
    data class EnterTaskItemContent(val value: String): TodoListEvent()
    data class SelectTaskList(val selectedTaskListId: Long): TodoListEvent()
    data class GetTaskItemsByTaskListId(val taskListId: Long): TodoListEvent()
    object InitLastSelectedTaskListPosition: TodoListEvent()
    object DeleteTaskList: TodoListEvent()
    object CompleteTaskItem: TodoListEvent()
    object RestoreTaskItemFromCompletion: TodoListEvent()
    object SaveTaskItem: TodoListEvent()
    object DeleteTaskItem: TodoListEvent()

}
