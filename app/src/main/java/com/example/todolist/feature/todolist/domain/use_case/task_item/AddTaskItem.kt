package com.example.todolist.feature.todolist.domain.use_case.task_item


import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AddTaskItem(
    private val repository: TaskItemRepository,
    private val androidId: String
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(vararg taskItem: TaskItem): List<Long> {
        if (taskItem.any { el -> el.title.isBlank() }) {
            throw InvalidTaskItemException("the name of the task can't be empty")
        }

        val ids = mutableListOf<Long>()

        val taskItemDto = taskItem.map {
            val id = repository.insertTaskItem(it).first()
            val item = it.copy(
                id = id,
                isSynchronizedWithRemote = false
            )
            ids.add(id)
            item.toTaskItemDto(androidId)
        }
        CoroutineScope(Dispatchers.IO).async {
            val result = repository.insertTaskItemOnRemote(
                taskItemDto = taskItemDto.toTypedArray()
            )

            if (result.isSuccessful) {
                val data = taskItem.map {
                    val item = it.copy(isSynchronizedWithRemote = true)
                    item
                }
                repository.updateTaskItem(*data.toTypedArray())
            }
        }
        return ids
    }
}