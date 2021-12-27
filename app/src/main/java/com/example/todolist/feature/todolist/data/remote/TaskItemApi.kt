package com.example.todolist.feature.todolist.data.remote

import com.example.todolist.feature.todolist.data.remote.dto.TaskItemDto
import com.example.todolist.feature.todolist.data.remote.dto.TaskListDto
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface TaskItemApi {

    @GET("/v1/taskItems/{taskListId}")
    suspend fun getTaskItemsByTaskListId(@Path("taskListId") taskListId: Long, @Query("userId") userId: Long): List<TaskItemDto>

    @GET("/v1/taskItems/{taskItemId}")
    suspend fun getTaskItemById(@Path("taskItemId") taskItemId: Long, @Query("userId") userId: Long) : TaskItemDto

    @POST("/v1/taskItems")
    suspend fun insertTaskItem(vararg taskItemDto: TaskItemDto, @Query("userId") userId: Long) : Call<ResponseBody>

    @DELETE("/v1/taskItems")
    suspend fun deleteTaskItem(vararg taskItemDto: TaskItemDto, @Query("userId") userId: Long) : Call<ResponseBody>

    @PUT("/v1/taskItems")
    suspend fun updateTaskItem(vararg taskItemDto: TaskItemDto, @Query("userId") userId: Long) : Call<ResponseBody>

}