package com.example.todolist.feature.todolist.data.remote

import com.example.todolist.feature.todolist.data.remote.dto.TaskListDto
import com.example.todolist.feature.todolist.domain.model.TaskList
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface TaskListApi {

    @GET("/v1/taskLists")
    suspend fun getTaskLists(@Query("userId") userId: String): List<TaskListDto>

    @GET("/v1/taskLists/{taskListId}")
    suspend fun getTaskListById(@Path("taskListId") taskListId: Long, @Query("userId") userId: String) : TaskListDto

    @POST("/v1/taskLists")
    suspend fun insertTaskList(@Body vararg taskListDto: TaskListDto) : Response<Void>

    //@DELETE DOES NOT ALLOW @Body
    @HTTP(method = "DELETE", path = "/v1/taskLists", hasBody = true)
    suspend fun deleteTaskList(@Body vararg taskListDto: TaskListDto) : Response<Void>

    @PUT("/v1/taskLists")
    suspend fun updateTaskList(@Body vararg taskListDto: TaskListDto) : Response<Void>

    @POST("/v1/taskLists/synchronizeTaskList")
    suspend fun synchronizeTaskList(@Body vararg taskListDto: TaskListDto) : Response<Void>

}