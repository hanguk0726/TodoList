package com.example.todolist.feature.todolist.presentation.todolist

data class TodoListTextFieldState(
    val text: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)
