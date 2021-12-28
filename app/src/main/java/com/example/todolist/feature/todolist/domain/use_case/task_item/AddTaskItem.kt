package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.common.Constants
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository

class AddTaskItem(
    private val repository: TaskItemRepository
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(vararg taskItem: TaskItem) {
        if(taskItem.any { el -> el.title.isBlank() }){
            throw InvalidTaskItemException("the name of the task can't be empty")
        }

        val taskItemDto = taskItem.map { it.toTaskItemDto(Constants.ANDROID_ID) }

        val result = repository.insertTaskItemOnRemote(
            taskItemDto = *taskItemDto.toTypedArray(),
            Constants.ANDROID_ID
        )

        if(result.isExecuted) {
            val data = taskItem.map {
                val item = it.copy(isSynchronizedWithRemote = true)
                item
            }
            repository.insertTaskItem(*data.toTypedArray())
        } else {
            repository.insertTaskItem(*taskItem)
        }

    }
}