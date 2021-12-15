package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import kotlinx.coroutines.flow.Flow

class GetTaskItemsByTaskListId (
    private val repository: TaskItemRepository
) {

    @Throws(InvalidTaskItemException::class)
    operator fun invoke(id: Long): Flow<List<TaskItem>>{
        return repository.getTaskItemsByTaskListId(id)
    }
}