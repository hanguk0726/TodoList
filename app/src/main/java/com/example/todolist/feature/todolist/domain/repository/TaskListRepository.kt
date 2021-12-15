package com.example.todolist.feature.todolist.domain.repository

import com.example.todolist.feature.todolist.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

interface TaskListRepository {

    fun getTaskLists(): Flow<List<TaskList>>

    suspend fun getTaskListById(id: Long): TaskList?

    suspend fun insertTaskList(taskList: TaskList) : Long

    suspend fun deleteTaskList(taskList: TaskList)

    suspend fun updateTaskList(TaskList: TaskList)
}