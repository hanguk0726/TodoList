package com.example.todolist.feature.todolist.domain.use_case.task_list

data class TaskListUseCases(
    val addTaskList: AddTaskList,
    val deleteTaskList: DeleteTaskList,
    val getTaskListById: GetTaskListById,
    val getTaskLists: GetTaskLists
)