package com.example.todolist.feature.todolist.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList

@Database(
    entities = [TaskList::class, TaskItem::class],
    version = 1,
    exportSchema = false
)

abstract class TodoListDatabase: RoomDatabase() {

    abstract val taskListDao: TaskListDao
    abstract val taskItemDao: TaskItemDao

    companion object {
        const val DATABASE_NAME = "todolist_db"
    }
}