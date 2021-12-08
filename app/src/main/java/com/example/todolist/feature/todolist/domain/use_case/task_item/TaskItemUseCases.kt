package com.example.todolist.feature.todolist.domain.use_case.task_item

data class TaskItemUseCases(
    val addTaskItem: AddTaskItem,
    val deleteTaskItem: DeleteTaskItem,
    val getTaskItemById: GetTaskItemById,
    val getTaskItemsByTaskListId: GetTaskItemsByTaskListId
)