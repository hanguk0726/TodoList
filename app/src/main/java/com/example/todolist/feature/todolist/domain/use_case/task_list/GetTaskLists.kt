package com.example.todolist.feature.todolist.domain.use_case.task_list

import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import com.example.todolist.feature.todolist.domain.util.OrderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetTaskLists(
    private val repository: TaskListRepository
) {
    @Throws(InvalidTaskListException::class)
    operator fun invoke(
        order: OrderType = OrderType.Ascending
    ): Flow<List<TaskList>> {
        return repository.getTaskListsAsFlow().map { taskLists ->
            when (order) {
                is OrderType.Ascending -> taskLists.sortedBy { it.createdTimestamp }
                is OrderType.Descending -> taskLists.sortedByDescending { it.createdTimestamp }
            }
        }
    }

    @Throws(InvalidTaskItemException::class)
    suspend fun noFlow(
        order: OrderType = OrderType.Ascending
    ): List<TaskList> {
        val taskLists = repository.getTaskLists()
        return when (order) {
            is OrderType.Ascending -> taskLists.sortedBy { it.createdTimestamp }
            is OrderType.Descending -> taskLists.sortedByDescending { it.createdTimestamp }
        }
    }
}