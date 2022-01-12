package com.example.todolist.feature.todolist.domain.use_case.task_item


import android.util.Log
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTaskItem(
    private val repository: TaskItemRepository,
    private val androidId: String
) {
    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(vararg taskItem: TaskItem): List<Long> {
        if (taskItem.any { el -> el.title.isBlank() }) {
            throw InvalidTaskItemException("the name of the task can't be empty")
        }

        val insertedTaskItem = addTaskItemOnLocal(*taskItem)

        val ids = insertedTaskItem.map { it.id!! }

        addTaskItemOnRemote(*insertedTaskItem.toTypedArray())

        return ids
    }


    private suspend fun addTaskItemOnLocal(vararg taskItem: TaskItem): List<TaskItem> {
        val insertedTaskItem = mutableListOf<TaskItem>()
        taskItem.forEach {
            val id = repository.insertTaskItem(it).first()
            val item = it.copy(
                id = id,
                isSynchronizedWithRemote = false
            )
            insertedTaskItem.add(item)
        }
        return insertedTaskItem
    }

    private suspend fun addTaskItemOnRemote(vararg taskItem: TaskItem) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val taskItemDto = taskItem.map {
                    it.toTaskItemDto(userId = androidId)
                }
                val result = repository.insertTaskItemOnRemote(
                    taskItemDto = taskItemDto.toTypedArray()
                )

                if (!result.isSuccessful) return@launch

                val data = taskItem.map {
                    it.copy(isSynchronizedWithRemote = true)
                }
                repository.updateTaskItem(*data.toTypedArray())
            } catch (e: Exception) {
                Log.e("AddTaskItem", e.message.toString())
            }
        }
}