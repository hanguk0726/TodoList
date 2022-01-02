package com.example.todolist.feature.todolist.data.remote

import com.example.todolist.feature.todolist.data.remote.dto.TaskListDto
import com.example.todolist.feature.todolist.domain.model.TaskList
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface TaskListApi {

    @GET("/v1/taskLists")
    suspend fun getTaskLists(@Query("userId") userId: String): List<TaskListDto>

    @GET("/v1/taskLists/{taskListId}")
    suspend fun getTaskListById(@Path("taskListId") taskListId: Long, @Query("userId") userId: String) : TaskListDto

    @POST("/v1/taskLists")
    suspend fun insertTaskList(vararg taskListDto: TaskListDto) : Call<ResponseBody>

    @DELETE("/v1/taskLists")
    suspend fun deleteTaskList(vararg taskListDto: TaskListDto) : Call<ResponseBody>

    @PUT("/v1/taskLists")
    suspend fun updateTaskList(vararg taskListDto: TaskListDto) : Call<ResponseBody>

    @POST("/v1/taskLists/synchronizeTaskList")
    suspend fun synchronizeTaskList(vararg taskListDto: TaskListDto) : Call<ResponseBody>

}