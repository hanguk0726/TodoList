package com.example.todolist.feature.todolist.data.data_source

import androidx.room.*
import com.example.todolist.feature.todolist.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {

    @Query("SELECT * FROM TaskList")
    fun getTaskLists(): Flow<List<TaskList>>

    @Query("SELECT * FROM TaskList WHERE id = :id")
    suspend fun getTaskListById(id: Int): TaskList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskList(TaskList: TaskList)

    @Delete
    suspend fun deleteTaskList(TaskList: TaskList)

}