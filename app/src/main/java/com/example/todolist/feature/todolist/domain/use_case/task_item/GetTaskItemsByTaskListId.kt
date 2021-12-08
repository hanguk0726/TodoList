package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository

class GetTaskItemsByTaskListId (
    private val repository: TaskItemRepository
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(id: Int) {
        repository.getTaskItemsByTaskListId(id)
    }
}