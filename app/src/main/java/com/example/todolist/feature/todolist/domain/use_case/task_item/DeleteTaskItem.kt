package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class DeleteTaskItem(
    private val repository: TaskItemRepository,
    private val androidId: String
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(vararg taskItem: TaskItem) {
        repository.deleteTaskItem(*taskItem)
        CoroutineScope(Dispatchers.IO).async {
            val taskItemDto = taskItem.map { it.toTaskItemDto(androidId) }

            val result = repository.deleteTaskItemOnRemote(
                taskItemDto = taskItemDto.toTypedArray()
            )

            if (!result.isSuccessful) {
                val data = taskItem.map {
                    val item = it.copy(needToBeDeleted = true)
                    item
                }
                repository.updateTaskItem(*data.toTypedArray())
            }
        }
    }
}