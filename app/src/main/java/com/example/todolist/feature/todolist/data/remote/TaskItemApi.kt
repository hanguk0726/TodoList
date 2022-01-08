package com.example.todolist.feature.todolist.data.remote

import com.example.todolist.feature.todolist.data.remote.dto.TaskItemDto
import com.example.todolist.feature.todolist.data.remote.dto.TaskListDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface TaskItemApi {

    @GET("/v1/taskItems/{taskListId}")
    suspend fun getTaskItemsByTaskListId(@Path("taskListId") taskListId: Long, @Query("userId") userId: String): List<TaskItemDto>

    @GET("/v1/taskItems")
    suspend fun getTaskItemById(@Query("taskItemId") taskItemId: Long, @Query("userId") userId: String) : TaskItemDto

    @POST("/v1/taskItems")
    suspend fun insertTaskItem(@Body vararg taskItemDto: TaskItemDto) : Call<ResponseBody>

    @DELETE("/v1/taskItems")
    suspend fun deleteTaskItem(@Body vararg taskItemDto: TaskItemDto) : Call<ResponseBody>

    @PUT("/v1/taskItems")
    suspend fun updateTaskItem(@Body vararg taskItemDto: TaskItemDto) : Call<ResponseBody>

    @POST("/v1/taskItems/synchronizeTaskItem")
    suspend fun synchronizeTaskItem(@Body vararg taskItemDto: TaskItemDto) : Call<ResponseBody>
}