package com.example.todolist.feature.todolist.domain.repository

import androidx.room.Update
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

interface TaskItemRepository {

    fun getTaskItemsByTaskListId(id: Long): Flow<List<TaskItem>>

    suspend fun getTaskItemById(id: Long): TaskItem?

    suspend fun insertTaskItem(taskItem: TaskItem)

    suspend fun deleteTaskItem(taskItem: TaskItem)

    suspend fun updateTaskItem(taskItem: TaskItem)
}