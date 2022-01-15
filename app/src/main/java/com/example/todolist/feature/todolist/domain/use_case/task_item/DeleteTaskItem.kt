package com.example.todolist.feature.todolist.domain.use_case.task_item

import android.util.Log
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        CoroutineScope(Dispatchers.IO).launch {
           try {
               val taskItemDto = taskItem.map { it.toTaskItemDto(androidId) }

                repository.deleteTaskItemOnRemote(
                   taskItemDto = taskItemDto.toTypedArray()
               )

           } catch (e : Exception){
               Log.e("DeleteTaskItem", e.message.toString())

               val data = taskItem.map {
                   it.copy(needToBeDeleted = true)
               }
               repository.insertTaskItem(*data.toTypedArray())
           }
        }
}