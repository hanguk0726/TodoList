package com.example.todolist.common.util.synchronization

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.todolist.feature.todolist.data.remote.TaskItemApi
import com.example.todolist.feature.todolist.data.remote.TaskListApi
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItem
import com.example.todolist.feature.todolist.data.remote.dto.toTaskItemDto
import com.example.todolist.feature.todolist.data.remote.dto.toTaskList
import com.example.todolist.feature.todolist.data.remote.dto.toTaskListDto
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import com.example.todolist.feature.todolist.domain.use_case.task_item.TaskItemUseCases
import com.example.todolist.feature.todolist.domain.use_case.task_list.TaskListUseCases
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.TimeUnit
import javax.inject.Named

@HiltWorker
class SynchronizeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @Named("androidId") androidId: String,
    val taskListRepository: TaskListRepository,
    val taskItemRepository: TaskItemRepository,
    val taskListUseCases: TaskListUseCases,
    val taskItemUseCases: TaskItemUseCases,
    val taskItemApi: TaskItemApi,
    val taskListApi: TaskListApi
) : CoroutineWorker(context, workerParams), CoroutineScope {

    private val userId = androidId

    override suspend fun doWork(): Result {
        try {
            // scroll to refresh action 이거나 처음 시작했을 경우 실행
            // 작업이후에 ViewModel가 인식하는가 Check
            val taskListsFromLocal = taskListRepository.getTaskLists()
            val taskListsFromRemote =
                taskListRepository.getTaskListsOnRemote(userId).body() ?: emptyList()

            // Case : local에 있고 remote에 없음
            if (taskListsFromLocal.isNotEmpty()) {
                taskListsFromLocal.forEach { taskList ->
                    synchronizeTaskItemLocalToRemote(taskList.id!!)
                    synchronizeTaskListLocalToRemote(taskList)
                }
            }

            // Case : remote에 있고 local에 없음
            if (taskListsFromRemote.isNotEmpty()) {
                taskListsFromRemote.forEach { taskListDto ->
                    val taskList = taskListDto.toTaskList()
                    if (!taskListsFromLocal.contains(taskList)) {
                        taskListRepository.insertTaskList(
                            taskList.copy(
                                isSynchronizedWithRemote = true
                            )
                        )
                    }
                    val taskItemsFromRemote = taskItemRepository
                        .getTaskItemsByTaskListIdOnRemote(taskList.id!!, userId).body()?.map {
                            it.toTaskItem()
                        } ?: emptyList()
                    val taskItemsFromLocal =
                        taskItemRepository.getTaskItemsByTaskListId(taskList.id)

                    taskItemsFromRemote.forEach {
                        if (!taskItemsFromLocal.contains(it)) {
                            taskItemRepository.insertTaskItem(it)
                        }
                    }
                }
            }
            return Result.success()

        } catch (e: Exception) {
            Log.e(
                "SynchronizeWorker",
                "Failed to [executeSynchronizeWork]. it will retry automatically.\n" +
                        "cause :: ${e.message}"
            )
            return Result.retry()
        }
    }

    private suspend fun synchronizeTaskItemLocalToRemote(taskListId: Long) {
        val taskItems = taskItemRepository.getTaskItemsByTaskListId(taskListId)
        if (taskItems.isEmpty()) return
        taskItems.forEach { taskItem ->
            if (taskItem.needToBeDeleted) {
                taskItemUseCases.deleteTaskItem(taskItem)
            } else {
                if (!taskItem.isSynchronizedWithRemote) {
                    val item = taskItem.toTaskItemDto(userId)
                    val result = taskItemApi.synchronizeTaskItem(
                        taskItemDto = arrayOf(item)
                    )
                    if (result.isSuccessful) {
                        taskItemUseCases.updateTaskItem(
                            taskItem.copy(
                                isSynchronizedWithRemote = true
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun synchronizeTaskListLocalToRemote(taskList: TaskList) {
        if (taskList.needToBeDeleted) {
            taskListUseCases.deleteTaskList(taskList)
        } else {
            if (!taskList.isSynchronizedWithRemote) {
                val item = taskList.toTaskListDto(userId)
                val result = taskListApi.synchronizeTaskList(
                    taskListDto = arrayOf(item)
                )
                if (result.isSuccessful) {
                    taskListUseCases.updateTaskList(
                        taskList.copy(
                            isSynchronizedWithRemote = true
                        )
                    )
                }
            }
        }
    }
}


fun executeSynchronizeWork(appContext: Context) {
    Log.i("SynchronizeWorker", "SynchronizeWorker is requested")
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val synchronizeWorkRequest =
        OneTimeWorkRequest.Builder(SynchronizeWorker::class.java)
//            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
    WorkManager
        .getInstance(appContext)
        .enqueue(synchronizeWorkRequest)

}