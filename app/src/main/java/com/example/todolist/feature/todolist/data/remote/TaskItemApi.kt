package com.example.todolist.feature.todolist.data.remote

import com.example.todolist.feature.todolist.data.remote.dto.TaskItemDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface TaskItemApi {

    @GET("/v1/taskItems/{taskListId}")
    suspend fun getTaskItemsByTaskListId(@Path("taskListId") taskListId: Long, @Query("userId") userId: String): List<TaskItemDto>

    @GET("/v1/taskItems/{taskItemId}")
    suspend fun getTaskItemById(@Path("taskItemId") taskItemId: Long, @Query("userId") userId: String) : TaskItemDto

    @POST("/v1/taskItems")
    suspend fun insertTaskItem(vararg taskItemDto: TaskItemDto, @Query("userId") userId: String) : Call<ResponseBody>

    @DELETE("/v1/taskItems")
    suspend fun deleteTaskItem(vararg taskItemDto: TaskItemDto, @Query("userId") userId: String) : Call<ResponseBody>

    @PUT("/v1/taskItems")
    suspend fun updateTaskItem(vararg taskItemDto: TaskItemDto, @Query("userId") userId: String) : Call<ResponseBody>

}