package com.example.todolist.feature.todolist.data.repository

import com.example.todolist.feature.todolist.data.data_source.TaskListDao
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import kotlinx.coroutines.flow.Flow

class TaskListRepositoryImpl(
    private val dao: TaskListDao
) : TaskListRepository {
    override fun getTaskLists(): Flow<List<TaskList>> {
        return dao.getTaskLists()
    }

    override suspend fun getTaskListById(id: Long): TaskList? {
        return dao.getTaskListById(id)
    }

    override suspend fun insertTaskList(vararg taskList: TaskList) : List<Long> {
        return dao.insertTaskList(*taskList)
    }

    override suspend fun deleteTaskList(vararg taskList: TaskList) {
        return dao.deleteTaskList(*taskList)
    }

    override suspend fun updateTaskList(vararg taskList: TaskList) {
        return dao.updateTaskList(*taskList)
    }

}