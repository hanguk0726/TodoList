package com.example.todolist.feature.todolist.data.repository

import com.example.todolist.feature.todolist.data.data_source.TaskItemDao
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import kotlinx.coroutines.flow.Flow

class TaskItemRepositoryImpl(
    private val dao: TaskItemDao
) : TaskItemRepository {

    override fun getTaskItemsByTaskListId(taskListId: Int): Flow<List<TaskItem>> {
        return dao.getTaskItemsByTaskListId(taskListId)
    }

    override suspend fun getTaskItemById(id: Int): TaskItem? {
        return dao.getTaskItemById(id)
    }

    override suspend fun insertTaskItem(taskItem: TaskItem) {
        return dao.insertTaskItem(taskItem)
    }

    override suspend fun deleteTaskItem(taskItem: TaskItem) {
        return dao.deleteTaskItem(taskItem)
    }

}