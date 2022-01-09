package com.example.todolist.feature.todolist.data.remote

import com.example.todolist.feature.todolist.data.remote.dto.TaskItemDto
import com.example.todolist.feature.todolist.data.remote.dto.TaskListDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface TaskItemApi {

    @GET("/v1/taskItems/{taskListId}")
    suspend fun getTaskItemsByTaskListId(@Path("taskListId") taskListId: Long, @Query("userId") userId: String): Response<List<TaskItemDto>>

    @GET("/v1/taskItems")
    suspend fun getTaskItemById(@Query("taskItemId") taskItemId: Long, @Query("userId") userId: String) : Response<TaskItemDto>

    @POST("/v1/taskItems")
    suspend fun insertTaskItem(@Body vararg taskItemDto: TaskItemDto) : Response<Void>

    //@DELETE DOES NOT ALLOW @Body
    @HTTP(method = "DELETE", path = "/v1/taskItems", hasBody = true)
    suspend fun deleteTaskItem(@Body vararg taskItemDto: TaskItemDto) : Response<Void>

    @PUT("/v1/taskItems")
    suspend fun updateTaskItem(@Body vararg taskItemDto: TaskItemDto) : Response<Void>

    @POST("/v1/taskItems/synchronizeTaskItem")
    suspend fun synchronizeTaskItem(@Body vararg taskItemDto: TaskItemDto) : Response<Void>
}