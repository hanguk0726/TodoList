package com.example.todolist.feature.todolist.domain.repository

import com.example.todolist.feature.todolist.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

interface TaskListRepository {

    fun getTaskLists(): Flow<List<TaskList>>

    suspend fun getTaskListById(id: Long): TaskList?

    suspend fun insertTaskList(vararg taskList: TaskList) : List<Long>

    suspend fun deleteTaskList(vararg taskList: TaskList)

    suspend fun updateTaskList(vararg taskList: TaskList)
}