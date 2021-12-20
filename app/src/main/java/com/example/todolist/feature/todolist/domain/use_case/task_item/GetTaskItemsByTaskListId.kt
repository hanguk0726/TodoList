package com.example.todolist.feature.todolist.domain.use_case.task_item

import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import com.example.todolist.feature.todolist.domain.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTaskItemsByTaskListId (
    private val repository: TaskItemRepository
) {

    @Throws(InvalidTaskItemException::class)
    operator fun invoke(
        id: Long,
        order: OrderType = OrderType.Descending): Flow<List<TaskItem>>{
        return repository.getTaskItemsByTaskListId(id).map {  taskItems ->
            when(order) {
                is OrderType.Ascending -> taskItems.sortedBy{ it.timestamp }
                is OrderType.Descending -> taskItems.sortedByDescending { it.timestamp }
            }
        }
    }
}
