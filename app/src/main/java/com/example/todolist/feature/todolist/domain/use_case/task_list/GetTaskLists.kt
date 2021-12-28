package com.example.todolist.feature.todolist.domain.use_case.task_list

import com.example.todolist.common.Constants
import com.example.todolist.common.util.Resource
import com.example.todolist.feature.todolist.data.remote.dto.toTaskList
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import com.example.todolist.feature.todolist.domain.util.OrderType
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception

class GetTaskLists(
    private val repository: TaskListRepository
) {
    @Throws(InvalidTaskListException::class)
    operator fun invoke(
        order: OrderType = OrderType.Ascending
    ): Flow<Resource<List<TaskList>>> = flow {
        try {
            emit(Resource.Loading())
            val taskLists = repository.getTaskListsOnRemote(Constants.ANDROID_ID).map{ it.toTaskList() }
            val sorted = when(order) {
                is OrderType.Ascending -> taskLists.sortedBy{ it.createdTimestamp }
                is OrderType.Descending -> taskLists.sortedByDescending { it.createdTimestamp }
            }
            emit(Resource.Success(sorted))
        } catch (e: HttpException) {
            emitLocalData(
                this,
                order,
                e
            )
        } catch (e: IOException) {
            emitLocalData(
                this,
                order,
                e
            )
        }
    }

    private suspend fun emitLocalData (
        flowCollector: FlowCollector<Resource<List<TaskList>>>,
        order: OrderType,
        e: Exception
    ) {
        val taskLists =  repository.getTaskLists()
        val sorted = when(order) {
            is OrderType.Ascending -> taskLists.sortedBy{ it.createdTimestamp }
            is OrderType.Descending -> taskLists.sortedByDescending { it.createdTimestamp }
        }
        flowCollector.emit(Resource.Error(
            message = e.localizedMessage ?: "An unexpected error occured",
            data = sorted
        ))
    }
}