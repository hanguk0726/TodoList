package com.example.todolist.feature.todolist.domain.use_case.task_list

import com.example.todolist.common.Constants
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.data.remote.dto.toTaskListDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository

class AddTaskList(
    private val repository: TaskListRepository
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(vararg taskList: TaskList) : List<Long> {
        if(taskList.any { el -> el.name.isBlank() }){
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
            item.toTaskListDto(Constants.ANDROID_ID)
        }

        val result = repository.insertTaskListOnRemote(
            taskListDto = *taskItemDto.toTypedArray(),
            Constants.ANDROID_ID
        )

        if(result.isExecuted ) {
            val data = taskList.map {
                val item = it.copy(isSynchronizedWithRemote = true)
                item
            }
            repository.updateTaskList(*data.toTypedArray())
        }

        return ids
    }
}