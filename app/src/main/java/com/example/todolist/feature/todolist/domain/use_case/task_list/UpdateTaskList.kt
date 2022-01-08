package com.example.todolist.feature.todolist.domain.use_case.task_list

import com.example.todolist.common.Constants
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.data.remote.dto.toTaskListDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import java.math.BigInteger
import javax.inject.Named

class UpdateTaskList(
    private val repository: TaskListRepository,
    @Named("androidId") private val androidId: String
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(vararg taskList: TaskList) {
        val taskItemDto = taskList.map {
            val item = it.copy(
                isSynchronizedWithRemote = false
            )
            item.toTaskListDto(androidId)
        }

        val result = repository.updateTaskListOnRemote(
            taskListDto = *taskItemDto.toTypedArray())

        if(result.isExecuted) {
            val data = taskList.map {
                val item = it.copy(isSynchronizedWithRemote = true)
                item
            }
            repository.updateTaskList(*data.toTypedArray())
        }
    }
}