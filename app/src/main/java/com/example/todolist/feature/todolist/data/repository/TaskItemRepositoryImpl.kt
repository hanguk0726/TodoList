package com.example.todolist.feature.todolist.data.repository

import com.example.todolist.feature.todolist.data.data_source.TaskItemDao
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import kotlinx.coroutines.flow.Flow

class TaskItemRepositoryImpl(
    private val dao: TaskItemDao
) : TaskItemRepository {


    override fun getTaskItemsByTaskListId(id: Long): Flow<List<TaskItem>> {
        return dao.getTaskItemsByTaskListId(id)
    }

    override suspend fun getTaskItemById(id: Long): TaskItem? {
        return dao.getTaskItemById(id)
    }

    override suspend fun insertTaskItem(vararg taskItem: TaskItem) {
        return dao.insertTaskItem(*taskItem)
    }

    override suspend fun deleteTaskItem(vararg taskItem: TaskItem) {
        return dao.deleteTaskItem(*taskItem)
    }

    override suspend fun updateTaskItem(vararg taskItem: TaskItem) {
        return dao.updateTaskItem(*taskItem)
    }

}