package com.example.todolist.di

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.todolist.common.Constants
import com.example.todolist.feature.todolist.data.data_source.TodoListDatabase
import com.example.todolist.feature.todolist.data.remote.TaskItemApi
import com.example.todolist.feature.todolist.data.remote.TaskListApi
import com.example.todolist.feature.todolist.data.repository.TaskItemRepositoryImpl
import com.example.todolist.feature.todolist.data.repository.TaskListRepositoryImpl
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import com.example.todolist.feature.todolist.domain.use_case.task_item.*
import com.example.todolist.feature.todolist.domain.use_case.task_list.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = {
                appContext.preferencesDataStoreFile("settings")
            }
        )

    @SuppressLint("HardwareIds")
    @Provides
    @Singleton
    @Named("androidId")
    fun provideAndroidId(@ApplicationContext appContext: Context): String {
        return Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
    }

    @Provides
    @Singleton
    fun provideTaskItemApi(): TaskItemApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaskItemApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTaskListApi(): TaskListApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaskListApi::class.java)
    }

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
    fun provideTaskListRepository(db: TodoListDatabase, api: TaskListApi): TaskListRepository {
        return TaskListRepositoryImpl(db.taskListDao, api)
    }

    @Provides
    @Singleton
    fun provideTaskItemRepository(db: TodoListDatabase, api: TaskItemApi): TaskItemRepository {
        return TaskItemRepositoryImpl(db.taskItemDao, api)
    }

    @Provides
    @Singleton
    fun provideTaskListUseCases(
        repository: TaskListRepository,
        @Named("androidId") androidId: String,
        @ApplicationContext appContext: Context
    ): TaskListUseCases {
        return TaskListUseCases(
            addTaskList = AddTaskList(repository, androidId, appContext),
            deleteTaskList = DeleteTaskList(repository, androidId, appContext),
            getTaskListById = GetTaskListById(repository),
            getTaskLists = GetTaskLists(repository, androidId),
            updateTaskList = UpdateTaskList(repository, androidId, appContext)
        )
    }

    @Provides
    @Singleton
    fun provideTaskItemUseCases(
        repository: TaskItemRepository,
        @Named("androidId") androidId: String,
        @ApplicationContext appContext: Context
    ): TaskItemUseCases {
        return TaskItemUseCases(
            addTaskItem = AddTaskItem(repository, androidId, appContext),
            deleteTaskItem = DeleteTaskItem(repository, androidId, appContext),
            getTaskItemById = GetTaskItemById(repository),
            getTaskItemsByTaskListId = GetTaskItemsByTaskListId(repository, androidId),
            updateTaskItem = UpdateTaskItem(repository, androidId, appContext)
        )
    }


}