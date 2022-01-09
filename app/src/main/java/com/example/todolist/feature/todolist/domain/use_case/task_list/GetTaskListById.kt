package com.example.todolist.feature.todolist.domain.use_case.task_list

import android.util.Log
import com.example.todolist.feature.todolist.data.remote.dto.toTaskList
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Named

class GetTaskListById(
    private val repository: TaskListRepository,
    @Named("androidId") private val androidId: String
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(id: Long): TaskList? {
        return repository.getTaskListById(id)
    }

    suspend fun fromRemote(id: Long): TaskList? {
        return try {
            repository.getTaskListByIdOnRemote(id, androidId).toTaskList()
        } catch (e: HttpException) {
            Log.e(
                "GetTaskItemById",
                e.localizedMessage ?: "Couldn't reach server. Check your internet connection",
            )
            repository.getTaskListById(id)
        } catch (e: IOException) {
            Log.e(
                "GetTaskItemById",
                e.localizedMessage ?: "Couldn't reach server. Check your internet connection",
            )
            repository.getTaskListById(id)
        }
    }
}