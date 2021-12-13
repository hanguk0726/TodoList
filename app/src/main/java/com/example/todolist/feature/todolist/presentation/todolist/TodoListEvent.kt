package com.example.todolist.feature.todolist.presentation.todolist


sealed class TodoListEvent{
    data class EnterTaskItemContent(val value: String): TodoListEvent()
    object DeleteTaskList: TodoListEvent()
    object CompleteTaskItem: TodoListEvent()
    object RestoreTaskItemFromCompletion: TodoListEvent()
    object SaveTaskItem: TodoListEvent()
    object DeleteTaskItem: TodoListEvent()

}
