package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.common.Constants
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository

class DeleteTaskItem (
    private val repository: TaskItemRepository
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(vararg taskItem: TaskItem) {
        repository.deleteTaskItem(*taskItem)

        val taskItemDto = taskItem.map { it.toTaskItemDto(Constants.ANDROID_ID) }

        val result = repository.deleteTaskItemOnRemote(
            taskItemDto = *taskItemDto.toTypedArray(),
            Constants.ANDROID_ID
        )

        if(result.isExecuted) {
            repository.updateTaskItem(*taskItem)
        } else {
            val data = taskItem.map {
                val item = it.copy(needToBeDeleted = true)
                item
            }
            repository.updateTaskItem(*data.toTypedArray())
        }
    }
}