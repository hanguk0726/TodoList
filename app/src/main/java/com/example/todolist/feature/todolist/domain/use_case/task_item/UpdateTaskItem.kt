package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository

class UpdateTaskItem(
    private val repository: TaskItemRepository
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(vararg taskItem: TaskItem) {
        repository.updateTaskItem(*taskItem)
    }
}