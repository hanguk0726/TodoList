package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository

class AddTaskItem(
    private val repository: TaskItemRepository
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(vararg taskItem: TaskItem) {
        if(taskItem.any { el -> el.title.isBlank() }){
            throw InvalidTaskItemException("the name of the task can't be empty")
        }
        repository.insertTaskItem(*taskItem)

    }
}