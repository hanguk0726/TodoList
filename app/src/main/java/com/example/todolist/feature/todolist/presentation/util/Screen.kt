package com.example.todolist.feature.todolist.presentation.util

sealed class Screen(val route: String) {
    object TodoListScreen: Screen("todolist_screen")
}
