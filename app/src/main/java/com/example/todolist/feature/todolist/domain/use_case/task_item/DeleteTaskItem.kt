package com.example.todolist.feature.todolist.domain.use_case.task_item

import android.util.Log
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteTaskItem(
    private val repository: TaskItemRepository,
    private val androidId: String
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(vararg taskItem: TaskItem) {
        repository.deleteTaskItem(*taskItem)

        deleteTaskItemOnRemote(*taskItem)
    }

    private suspend fun deleteTaskItemOnRemote(vararg taskItem: TaskItem) =
        withContext(Dispatchers.IO) {
           try {
               val taskItemDto = taskItem.map { it.toTaskItemDto(androidId) }

               val result = repository.deleteTaskItemOnRemote(
                   taskItemDto = taskItemDto.toTypedArray()
               )

               if (result.isSuccessful) return@withContext

               Log.e("DeleteTaskItem", "Failed to execute the task on remote")
               val data = taskItem.map {
                   it.copy(needToBeDeleted = true)
               }
               repository.updateTaskItem(*data.toTypedArray())

           } catch (e : Exception){
               Log.e("DeleteTaskItem", e.message.toString())
           }
        }
}