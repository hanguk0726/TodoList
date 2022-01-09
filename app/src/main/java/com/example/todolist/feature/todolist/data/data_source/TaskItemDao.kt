package com.example.todolist.feature.todolist.data.data_source

import androidx.room.*
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskItemDao {

    @Query("SELECT * FROM TaskItem WHERE taskListId = :id")
    fun getTaskItemsByTaskListId(id: Long): Flow<List<TaskItem>>

    @Query("SELECT * FROM TaskItem WHERE id = :id")
    suspend fun getTaskItemById(id: Long): TaskItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskItem(vararg taskItem: TaskItem) : List<Long>

    @Delete
    suspend fun deleteTaskItem(vararg taskItem: TaskItem)

    @Update(entity = TaskItem::class)
    suspend fun updateTaskItem(vararg taskItem: TaskItem)

}