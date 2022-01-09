package com.example.todolist.feature.todolist.data.repository

import com.example.todolist.feature.todolist.data.data_source.TaskListDao
import com.example.todolist.feature.todolist.data.remote.TaskListApi
import com.example.todolist.feature.todolist.data.remote.dto.TaskListDto
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class TaskListRepositoryImpl(
    private val dao: TaskListDao,
    private val api: TaskListApi
) : TaskListRepository {

    override fun getTaskLists(): Flow<List<TaskList>> {
        return dao.getTaskLists()
    }

    override suspend fun getTaskListById(id: Long): TaskList? {
        return dao.getTaskListById(id)
    }

    override suspend fun insertTaskList(vararg taskList: TaskList) : List<Long> {
        return dao.insertTaskList(*taskList)
    }

    override suspend fun deleteTaskList(vararg taskList: TaskList) {
        return dao.deleteTaskList(*taskList)
    }

    override suspend fun updateTaskList(vararg taskList: TaskList) {
        return dao.updateTaskList(*taskList)
    }

    override suspend fun getTaskListsOnRemote(userId: String): List<TaskListDto> {
        return api.getTaskLists(userId)
    }

    override suspend fun getTaskListByIdOnRemote(taskListId: Long, userId: String): TaskListDto {
        return api.getTaskListById(taskListId, userId)
    }

    override suspend fun insertTaskListOnRemote(vararg taskListDto: TaskListDto): Response<Void> {
        return api.insertTaskList(
            taskListDto = *taskListDto)
    }

    override suspend fun deleteTaskListOnRemote(vararg taskListDto: TaskListDto): Response<Void> {
        return api.deleteTaskList(
            taskListDto = *taskListDto)
    }

    override suspend fun updateTaskListOnRemote(vararg taskListDto: TaskListDto): Response<Void> {
        return api.updateTaskList(
            taskListDto = *taskListDto)
    }

}