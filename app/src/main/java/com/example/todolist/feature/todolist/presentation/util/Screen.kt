package com.example.todolist.feature.todolist.presentation.util

sealed class Screen(val route: String) {
    object TodoListScreen: Screen("todolist_screen")
    object AddEditTaskListScreen: Screen("add_edit_tasklist_screen")
    object EditTaskItemScreen: Screen("edit_task_item_screen")
}
