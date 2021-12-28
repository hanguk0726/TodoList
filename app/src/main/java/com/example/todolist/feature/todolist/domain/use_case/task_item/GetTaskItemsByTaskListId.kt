package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.common.Constants
import com.example.todolist.common.util.Resource
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItem
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import com.example.todolist.feature.todolist.domain.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class GetTaskItemsByTaskListId (
    private val repository: TaskItemRepository
) {

    @Throws(InvalidTaskItemException::class)
    operator fun invoke(
        id: Long,
        order: OrderType = OrderType.Descending): Flow<Resource<List<TaskItem>>>  = flow {
        try {
            emit(Resource.Loading())
            val taskLists = repository.getTaskItemsByTaskListIdOnRemote(
                id,
                Constants.ANDROID_ID).map{ it.toTaskItem() }
            val sorted = when(order) {
                is OrderType.Ascending -> taskLists.sortedBy{ it.timestamp }
                is OrderType.Descending -> taskLists.sortedByDescending { it.timestamp }
            }
            emit(Resource.Success(sorted))
        } catch (e: HttpException) {
            val taskLists =  repository.getTaskItemsByTaskListId(id)
            val sorted = when(order) {
                is OrderType.Ascending -> taskLists.sortedBy{ it.timestamp }
                is OrderType.Descending -> taskLists.sortedByDescending { it.timestamp }
            }
            emit(Resource.Error(
                message = e.localizedMessage ?: "An unexpected error occured",
                data = sorted
            ))
        } catch (e: IOException) {
            val taskLists =  repository.getTaskItemsByTaskListId(id)
            val sorted = when(order) {
                is OrderType.Ascending -> taskLists.sortedBy{ it.timestamp }
                is OrderType.Descending -> taskLists.sortedByDescending { it.timestamp }
            }
            emit(Resource.Error(
                message = e.localizedMessage ?:
                "Couldn't reach server. Check your internet connection",
                data = sorted
            ))
        }
    }
}
