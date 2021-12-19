package com.example.todolist.feature.todolist.domain.use_case.task_list

import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository

class DeleteTaskList(
    private val repository: TaskListRepository
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(vararg taskList: TaskList) {
        repository.deleteTaskList(*taskList)
    }
}