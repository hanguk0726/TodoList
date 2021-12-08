package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository

class DeleteTaskItem (
    private val repository: TaskItemRepository
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(taskItem: TaskItem) {
        repository.deleteTaskItem(taskItem)
    }
}