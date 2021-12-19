package com.example.todolist.feature.todolist.domain.use_case.task_list

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
        return repository.insertTaskList(*taskList)
    }
}