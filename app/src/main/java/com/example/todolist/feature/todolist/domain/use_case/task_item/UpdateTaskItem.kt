package com.example.todolist.feature.todolist.domain.use_case.task_item

import android.util.Log
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateTaskItem(
    private val repository: TaskItemRepository,
    private val androidId: String
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(vararg taskItem: TaskItem) {
        if (taskItem.any { el -> el.title.isBlank() }) {
            throw InvalidTaskItemException("the name of the task can't be empty")
        }

        taskItem.forEach {
            repository.updateTaskItem(
                it.copy(
                    isSynchronizedWithRemote = true
                )
            )
        }
        updateTaskItemOnRemote(*taskItem)
    }

    private suspend fun updateTaskItemOnRemote(vararg taskItem: TaskItem) =
        CoroutineScope(Dispatchers.IO).launch {

            try {
                val taskItemDto = taskItem.map {
                    it.toTaskItemDto(userId = androidId)
                }
                 repository.updateTaskItemOnRemote(
                    taskItemDto = taskItemDto.toTypedArray()
                )


            } catch (e: Exception) {
                Log.e("UpdateTaskItem", e.message.toString())

                val data = taskItem.map {
                    it.copy(isSynchronizedWithRemote = false)
                }
                repository.updateTaskItem(*data.toTypedArray())
            }
        }
}