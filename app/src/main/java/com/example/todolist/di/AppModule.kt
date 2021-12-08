package com.example.todolist.di

import android.app.Application
import androidx.room.Room
import com.example.todolist.feature.todolist.data.data_source.TodoListDatabase
import com.example.todolist.feature.todolist.data.repository.TaskItemRepositoryImpl
import com.example.todolist.feature.todolist.data.repository.TaskListRepositoryImpl
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import com.example.todolist.feature.todolist.domain.use_case.task_item.*
import com.example.todolist.feature.todolist.domain.use_case.task_list.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoListDataBase(app: Application): TodoListDatabase {
        return Room.databaseBuilder(
            app,
            TodoListDatabase::class.java,
            TodoListDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideTaskListRepository(db:TodoListDatabase): TaskListRepository {
        return TaskListRepositoryImpl(db.taskListDao)
    }

    @Provides
    @Singleton
    fun provideTaskItemRepository(db:TodoListDatabase): TaskItemRepository {
        return TaskItemRepositoryImpl(db.taskItemDao)
    }

    @Provides
    @Singleton
    fun provideTaskListUseCases(repository: TaskListRepository): TaskListUseCases {
        return TaskListUseCases(
            addTaskList = AddTaskList(repository),
            deleteTaskList = DeleteTaskList(repository),
            getTaskListById = GetTaskListById(repository),
            getTaskLists = GetTaskLists(repository)
        )
    }

    @Provides
    @Singleton
    fun provideTaskItemUseCases(repository: TaskItemRepository): TaskItemUseCases {
        return TaskItemUseCases(
            addTaskItem = AddTaskItem(repository),
            deleteTaskItem = DeleteTaskItem(repository),
            getTaskItemById = GetTaskItemById(repository),
            getTaskItemsByTaskListId = GetTaskItemsByTaskListId(repository)
        )
    }
}