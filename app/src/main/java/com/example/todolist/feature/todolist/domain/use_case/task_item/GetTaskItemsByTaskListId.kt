package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.common.Constants
import com.example.todolist.common.util.Resource
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItem
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import com.example.todolist.feature.todolist.domain.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception
import java.math.BigInteger
import javax.inject.Named

class GetTaskItemsByTaskListId (
    private val repository: TaskItemRepository,
    @Named("androidId") private val androidId: String
) {

    @Throws(InvalidTaskItemException::class)
    operator fun invoke(
        id: Long,
        order: OrderType = OrderType.Descending): Flow<Resource<List<TaskItem>>>  = flow {
        try {
            emit(Resource.Loading())
            val taskLists = repository.getTaskItemsByTaskListIdOnRemote(
                id,
                androidId).map{ it.toTaskItem() }
            val sorted = when(order) {
                is OrderType.Ascending -> taskLists.sortedBy{ it.timestamp }
                is OrderType.Descending -> taskLists.sortedByDescending { it.timestamp }
            }
            emit(Resource.Success(sorted))
        } catch (e: HttpException) {
            emitLocalData(
                id,
                this,
                order,
                e
            )
        } catch (e: IOException) {
            emitLocalData(
                id,
                this,
                order,
                e
            )
        }
    }

    private suspend fun emitLocalData (
        id : Long,
        flowCollector: FlowCollector<Resource<List<TaskItem>>>,
        order: OrderType,
        e: Exception
    ) {
        val taskLists =  repository.getTaskItemsByTaskListId(id)
        val sorted = when(order) {
            is OrderType.Ascending -> taskLists.sortedBy{ it.timestamp }
            is OrderType.Descending -> taskLists.sortedByDescending { it.timestamp }
        }
        flowCollector.emit(Resource.Error(
            message = e.localizedMessage ?:
            "Couldn't reach server. Check your internet connection",
            data = sorted
        ))
    }
}
