package com.example.todolist.feature.todolist.domain.use_case.task_item

import android.util.Log
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItem
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Named

class GetTaskItemById(
    private val repository: TaskItemRepository,
    @Named("androidId") private val androidId: String
) {

    @Throws(InvalidTaskItemException::class)
    suspend operator fun invoke(id: Long): TaskItem? {

        return repository.getTaskItemById(id)

    }

    suspend fun fromRemote(id: Long): TaskItem? {
        return try {
            repository.getTaskItemByIdOnRemote(id, androidId).toTaskItem()
        } catch (e: HttpException) {
            Log.e(
                "GetTaskItemById",
                e.localizedMessage ?: "Couldn't reach server. Check your internet connection",
            )
            repository.getTaskItemById(id)
        } catch (e: IOException) {
            Log.e(
                "GetTaskItemById",
                e.localizedMessage ?: "Couldn't reach server. Check your internet connection",
            )
            repository.getTaskItemById(id)
        }
    }
}