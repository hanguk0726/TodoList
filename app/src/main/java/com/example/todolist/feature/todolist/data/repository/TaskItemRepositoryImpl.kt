package com.example.todolist.feature.todolist.data.repository

import com.example.todolist.feature.todolist.data.data_source.TaskItemDao
import com.example.todolist.feature.todolist.data.remote.TaskItemApi
import com.example.todolist.feature.todolist.data.remote.dto.TaskItemDto
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class TaskItemRepositoryImpl(
    private val dao: TaskItemDao,
    private val api: TaskItemApi
) : TaskItemRepository {

    override fun getTaskItemsByTaskListId(id: Long): Flow<List<TaskItem>> {
        return dao.getTaskItemsByTaskListId(id)
    }

    override suspend fun getTaskItemById(id: Long): TaskItem? {
        return dao.getTaskItemById(id)
    }

    override suspend fun insertTaskItem(vararg taskItem: TaskItem) : List<Long> {
        return dao.insertTaskItem(*taskItem)
    }

    override suspend fun deleteTaskItem(vararg taskItem: TaskItem) {
        return dao.deleteTaskItem(*taskItem)
    }

    override suspend fun updateTaskItem(vararg taskItem: TaskItem) {
        return dao.updateTaskItem(*taskItem)
    }

    override suspend fun getTaskItemsByTaskListIdOnRemote(taskListId: Long, userId: String): List<TaskItemDto> {
        return api.getTaskItemsByTaskListId(taskListId, userId)
    }

    override suspend fun getTaskItemByIdOnRemote(taskItemId: Long, userId: String): TaskItemDto {
        return api.getTaskItemById(taskItemId, userId)
    }

    override suspend fun insertTaskItemOnRemote(vararg taskItemDto:TaskItemDto): Response<Void> {
        return api.insertTaskItem(
            taskItemDto = *taskItemDto)
    }

    override suspend fun deleteTaskItemOnRemote(vararg taskItemDto: TaskItemDto): Response<Void> {
        return api.deleteTaskItem(
            taskItemDto = *taskItemDto)
    }

    override suspend fun updateTaskItemOnRemote(vararg taskItemDto: TaskItemDto): Response<Void> {
        return api.updateTaskItem(
            taskItemDto = *taskItemDto)
    }

}