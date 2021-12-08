package com.example.todolist.feature.todolist.domain.repository

import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

interface TaskItemRepository {

    fun getTaskItemsByTaskListId(taskListId: Int): Flow<List<TaskItem>>

    suspend fun getTaskItemById(id: Int): TaskItem?

    suspend fun insertTaskItem(taskItem: TaskItem)

    suspend fun deleteTaskItem(taskItem: TaskItem)
}