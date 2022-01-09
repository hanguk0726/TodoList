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
    private val repository: TaskListRepository
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(id: Long): TaskList? {
        return repository.getTaskListById(id)
    }
}