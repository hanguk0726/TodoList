package com.example.todolist.feature.todolist.domain.repository

import com.example.todolist.feature.todolist.data.remote.dto.TaskListDto
import com.example.todolist.feature.todolist.domain.model.TaskList
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface TaskListRepository {

    fun getTaskLists(): List<TaskList>

    suspend fun getTaskListById(id: Long): TaskList?

    suspend fun insertTaskList(vararg taskList: TaskList) : List<Long>

    suspend fun deleteTaskList(vararg taskList: TaskList)

    suspend fun updateTaskList(vararg taskList: TaskList)

    suspend fun getTaskListsOnRemote(userId: String): List<TaskListDto>

    suspend fun getTaskListByIdOnRemote(taskListId: Long, userId: String) : TaskListDto

    suspend fun insertTaskListOnRemote(vararg taskListDto: TaskListDto, userId: String) : Call<ResponseBody>

    suspend fun deleteTaskListOnRemote(vararg taskListDto: TaskListDto, userId: String) : Call<ResponseBody>

    suspend fun updateTaskListOnRemote(vararg taskListDto: TaskListDto, userId: String) : Call<ResponseBody>
}