package com.example.todolist.feature.todolist.domain.repository

import androidx.room.Update
import com.example.todolist.feature.todolist.data.remote.dto.TaskItemDto
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface TaskItemRepository {

    fun getTaskItemsByTaskListIdAsFlow(id: Long): Flow<List<TaskItem>>

    suspend fun getTaskItemsByTaskListId(id: Long): List<TaskItem>

    suspend fun getTaskItemById(id: Long): TaskItem?

    suspend fun insertTaskItem(vararg taskItem: TaskItem) : List<Long>

    suspend fun deleteTaskItem(vararg taskItem: TaskItem)

    suspend fun updateTaskItem(vararg taskItem: TaskItem)

    suspend fun getTaskItemsByTaskListIdOnRemote(taskListId: Long, userId: String): Response<List<TaskItemDto>>

    suspend fun getTaskItemByIdOnRemote(taskItemId: Long, userId: String) : Response<TaskItemDto>

    suspend fun insertTaskItemOnRemote(vararg taskItemDto: TaskItemDto) : Response<Void>

    suspend fun deleteTaskItemOnRemote(vararg taskItemDto: TaskItemDto) : Response<Void>

    suspend fun updateTaskItemOnRemote(vararg taskItemDto: TaskItemDto) : Response<Void>

}