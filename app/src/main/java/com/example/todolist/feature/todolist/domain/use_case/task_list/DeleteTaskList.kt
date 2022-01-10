package com.example.todolist.feature.todolist.domain.use_case.task_list

import com.example.todolist.feature.todolist.data.remote.dto.toTaskListDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class DeleteTaskList(
    private val repository: TaskListRepository,
    private val androidId: String
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(vararg taskList: TaskList) {
        repository.deleteTaskList(*taskList)

        CoroutineScope(Dispatchers.IO).async {
            val taskListDto = taskList.map { it.toTaskListDto(androidId) }
            val result = repository.deleteTaskListOnRemote(
                taskListDto = taskListDto.toTypedArray()
            )
            if (!result.isSuccessful) {
                val data = taskList.map {
                    val item = it.copy(needToBeDeleted = true)
                    item
                }
                repository.updateTaskList(*data.toTypedArray())
            }
        }
    }
}