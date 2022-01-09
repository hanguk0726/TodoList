package com.example.todolist.feature.todolist.domain.use_case.task_list

import android.content.Context
import com.example.todolist.common.util.synchronization.executeSynchronizeWork
import com.example.todolist.feature.todolist.data.remote.dto.toTaskListDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AddTaskList(
    private val repository: TaskListRepository,
    private val androidId: String,
    private val appContext: Context
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(vararg taskList: TaskList): List<Long> {
        if (taskList.any { el -> el.name.isBlank() }) {
            throw InvalidTaskListException("the name of the list can't be empty")
        }

        val ids = mutableListOf<Long>()

        val taskItemDto = taskList.map {
            val id = repository.insertTaskList(it).first()
            val item = it.copy(
                id = id,
                isSynchronizedWithRemote = false
            )
            ids.add(id)
            item.toTaskListDto(androidId)
        }

        CoroutineScope(Dispatchers.IO).async {
            val result = repository.insertTaskListOnRemote(
                taskListDto = *taskItemDto.toTypedArray()
            )

            if (result.isSuccessful) {
                val data = taskList.map {
                    val item = it.copy(isSynchronizedWithRemote = true)
                    item
                }
                repository.updateTaskList(*data.toTypedArray())
            }
            executeSynchronizeWork(appContext)
        }

        return ids
    }
}