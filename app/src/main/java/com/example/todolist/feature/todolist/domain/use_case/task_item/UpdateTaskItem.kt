package com.example.todolist.feature.todolist.domain.use_case.task_item

import android.content.Context
import com.example.todolist.common.util.synchronization.executeSynchronizeWork
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import javax.inject.Named

class UpdateTaskItem(
    private val repository: TaskItemRepository,
    private val androidId: String,
    private val appContext: Context
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(vararg taskItem: TaskItem) {
        if (taskItem.any { el -> el.title.isBlank() }) {
            throw InvalidTaskItemException("the name of the task can't be empty")
        }

        val taskItemDto = taskItem.map {
            val item = it.copy(
                isSynchronizedWithRemote = false
            )
            repository.updateTaskItem(item)
            item.toTaskItemDto(androidId)
        }
        CoroutineScope(Dispatchers.IO).async {
            val result = repository.updateTaskItemOnRemote(
                taskItemDto = taskItemDto.toTypedArray()
            )

            if (result.isSuccessful) {
                val data = taskItem.map {
                    val item = it.copy(isSynchronizedWithRemote = true)
                    item
                }
                repository.updateTaskItem(*data.toTypedArray())
            }
            executeSynchronizeWork(appContext)
        }
    }
}