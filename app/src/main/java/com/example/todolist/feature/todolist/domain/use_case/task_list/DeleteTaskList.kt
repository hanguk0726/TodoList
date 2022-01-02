package com.example.todolist.feature.todolist.domain.use_case.task_list

import com.example.todolist.common.Constants
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.data.remote.dto.toTaskListDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository

class DeleteTaskList(
    private val repository: TaskListRepository
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(vararg taskList: TaskList) {
        val taskListDto = taskList.map { it.toTaskListDto(Constants.ANDROID_ID) }

        val result = repository.deleteTaskListOnRemote(
            taskListDto = *taskListDto.toTypedArray())

        if(result.isExecuted) {
            repository.deleteTaskList(*taskList)
        } else {
            val data = taskList.map {
                val item = it.copy(needToBeDeleted = true)
                item
            }
            repository.updateTaskList(*data.toTypedArray())
        }
    }
}