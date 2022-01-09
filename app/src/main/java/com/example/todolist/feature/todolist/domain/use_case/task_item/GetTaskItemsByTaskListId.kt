package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import com.example.todolist.feature.todolist.domain.util.OrderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetTaskItemsByTaskListId(
    private val repository: TaskItemRepository
) {

    @Throws(InvalidTaskItemException::class)
    operator fun invoke(
        id: Long,
        order: OrderType = OrderType.Descending,
    ): Flow<List<TaskItem>> {
        return repository.getTaskItemsByTaskListIdAsFlow(id).map { taskItems ->
            when (order) {
                is OrderType.Ascending -> taskItems.sortedBy { it.createdTimestamp }
                is OrderType.Descending -> taskItems.sortedByDescending { it.createdTimestamp }
            }
        }
    }

    @Throws(InvalidTaskItemException::class)
    suspend fun noFlow(
        id: Long,
        order: OrderType = OrderType.Descending
    ): List<TaskItem> {
        val taskItems = repository.getTaskItemsByTaskListId(id)
        return when (order) {
            is OrderType.Ascending -> taskItems.sortedBy { it.createdTimestamp }
            is OrderType.Descending -> taskItems.sortedByDescending { it.createdTimestamp }
        }
    }
}
