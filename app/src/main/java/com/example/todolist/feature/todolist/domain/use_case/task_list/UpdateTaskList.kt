package com.example.todolist.feature.todolist.domain.use_case.task_list

import com.example.todolist.feature.todolist.data.remote.dto.toTaskListDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class UpdateTaskList(
    private val repository: TaskListRepository,
    private val androidId: String
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(vararg taskList: TaskList) {
        val taskItemDto = taskList.map {
            val item = it.copy(
                isSynchronizedWithRemote = false
            )
            repository.updateTaskList(item)
            item.toTaskListDto(androidId)
        }

        CoroutineScope(Dispatchers.IO).async {
            val result = repository.updateTaskListOnRemote(
                taskListDto = *taskItemDto.toTypedArray()
            )
            if (result.isSuccessful) {
                val data = taskList.map {
                    val item = it.copy(isSynchronizedWithRemote = true)
                    item
                }
                repository.updateTaskList(*data.toTypedArray())
            }
        }
    }
}