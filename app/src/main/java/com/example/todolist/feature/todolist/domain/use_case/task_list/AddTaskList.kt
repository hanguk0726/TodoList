package com.example.todolist.feature.todolist.domain.use_case.task_list

import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository

class AddTaskList(
    private val repository: TaskListRepository
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(taskList: TaskList) {
        if(taskList.name.isBlank()){
            throw InvalidTaskListException("the name of the list can't be empty")
        }
        repository.insertTaskList(taskList)
    }
}