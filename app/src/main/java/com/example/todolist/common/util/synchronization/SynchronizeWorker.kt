package com.example.todolist.common.util.synchronization

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.todolist.feature.todolist.data.remote.TaskItemApi
import com.example.todolist.feature.todolist.data.remote.TaskListApi
import com.example.todolist.feature.todolist.data.remote.dto.*
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskItemRepository
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import com.example.todolist.feature.todolist.domain.use_case.task_item.TaskItemUseCases
import com.example.todolist.feature.todolist.domain.use_case.task_list.TaskListUseCases
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
) : CoroutineWorker(context, workerParams) {

    private val userId = androidId

    override suspend fun doWork(): Result {
        try {
            val localTaskLists = taskListRepository.getTaskLists()
            val remoteTaskLists =
                taskListRepository.getTaskListsOnRemote(userId).body() ?: emptyList()
            val localTaskListIds = localTaskLists.map { it.id!! }
            val remoteTaskListIds = remoteTaskLists.map { it.id!! }
            val allRemoteTaskItems = with(mutableListOf<TaskItemDto>()) {
                remoteTaskListIds.forEach { taskListId ->
                    val remoteTaskItems =
                        taskItemRepository.getTaskItemsByTaskListIdOnRemote(taskListId, userId)
                            .body()
                            ?: emptyList()
                    this.addAll(remoteTaskItems)
                }
                this
            }


            // Case : Local is latest, Remote is not
            if (remoteTaskLists.isNotEmpty()) {
                synchronizeRemoteTaskListsToLocal(remoteTaskLists, localTaskLists)
                synchronizeRemoteTaskItemsToLocal(remoteTaskListIds)
            }

            // Case : Remote is latest, Local is not
            if (localTaskLists.isNotEmpty()) {
                synchronizeLocalTaskItemsToRemote(
                    localTaskListIds,
                    allRemoteTaskItems
                )
                synchronizeLocalTaskListsToRemote(localTaskLists, remoteTaskLists, userId)
            }

            return Result.success()

        } catch (e: Exception) {
            Log.e(
                "SynchronizeWorker",
                "Failed to execute method [executeSynchronizeWork]. it will retry automatically.\n" +
                        "cause :: ${e.message}"
            )
            return Result.retry()
        }
    }

    private suspend fun synchronizeRemoteTaskItemsToLocal(remoteTaskListIds: List<Long>) {
        remoteTaskListIds.forEach { taskListId ->

            val remoteTaskItems = taskItemRepository
                .getTaskItemsByTaskListIdOnRemote(taskListId, userId).body()?.map {
                    it.toTaskItem()
                } ?: emptyList()
            val localTaskItems =
                taskItemRepository.getTaskItemsByTaskListId(taskListId)

            remoteTaskItems.forEach { remoteTaskItem ->
                val doesNotExistInLocal = localTaskItems.none {
                    it.id == remoteTaskItem.id && it.taskListId == remoteTaskItem.taskListId
                }
                if (doesNotExistInLocal) {
                    taskItemRepository.insertTaskItem(remoteTaskItem)
                }
            }
        }
    }

    private suspend fun synchronizeRemoteTaskListsToLocal(
        remoteTaskLists: List<TaskListDto>,
        localTaskLists: List<TaskList>
    ) {
        remoteTaskLists.forEach { taskListDto ->
            val taskList = taskListDto.toTaskList()
            val doesNotExistInLocal = localTaskLists.none {
                it.id == taskListDto.id && userId == taskListDto.userId
            }
            if (doesNotExistInLocal) {
                taskListRepository.insertTaskList(
                    taskList.copy(
                        isSynchronizedWithRemote = true
                    )
                )
            }

        }
    }

    //SocketTimeoutException
    @Throws(Exception::class)
    private suspend fun synchronizeLocalTaskItemsToRemote(
        localTaskListIds: List<Long>,
        allRemoteTaskItems: List<TaskItemDto>
    ) {
        localTaskListIds.forEach { taskListId ->

            val taskItems = taskItemRepository.getTaskItemsByTaskListId(taskListId)

            if (taskItems.isEmpty()) return@forEach

            taskItems.forEach taskItem@{ taskItem ->

                if (taskItem.needToBeDeleted) {
                    taskItemUseCases.deleteTaskItem(taskItem)
                    return@taskItem
                }

                val existsAndUpdatedInRemote = allRemoteTaskItems.none {
                    it.id == taskItem.id && it.taskListId == taskItem.taskListId
                            && it.userId == userId
                } && taskItem.isSynchronizedWithRemote

                if (existsAndUpdatedInRemote) return@taskItem

                val result = withContext(Dispatchers.IO) {
                    taskItemApi.synchronizeTaskItem(
                        taskItemDto = arrayOf(taskItem.toTaskItemDto(userId))
                    )
                }

                if (result.isSuccessful) {
                    taskItemUseCases.updateTaskItem(
                        taskItem.copy(
                            isSynchronizedWithRemote = true
                        )
                    )
                } else {
                    Log.e(
                        "SynchronizeWorker",
                        "Failed to execute method [synchronizeTaskItemLocalToRemote]"
                    )
                }
            }
        }
    }

    //SocketTimeoutException
    @Throws(Exception::class)
    private suspend fun synchronizeLocalTaskListsToRemote(
        localTaskLists: List<TaskList>,
        remoteTaskLists: List<TaskListDto>,
        userId: String
    ) {
        localTaskLists.forEach { taskList ->
            if (taskList.needToBeDeleted) {
                taskListUseCases.deleteTaskList(taskList)
                return@forEach
            }

            val existsAndUpdatedInRemote =
                remoteTaskLists.any { it.id == taskList.id && it.userId == userId } &&
                        taskList.isSynchronizedWithRemote

            if (existsAndUpdatedInRemote) return@forEach

            val result = withContext(Dispatchers.IO) {
                taskListApi.synchronizeTaskList(
                    taskListDto = arrayOf(taskList.toTaskListDto(userId))
                )
            }

            if (result.isSuccessful) {
                taskListUseCases.updateTaskList(
                    taskList.copy(
                        isSynchronizedWithRemote = true
                    )
                )
            } else {
                Log.e(
                    "SynchronizeWorker",
                    "Failed to execute method [synchronizeTaskListLocalToRemote]"
                )
            }
        }
    }
}


private const val uniqueWorkName = "synchronizeWork"

private val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

private val synchronizeWorkRequest =
    OneTimeWorkRequest.Builder(SynchronizeWorker::class.java)
        .setConstraints(constraints)
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .build()

fun executeSynchronizeWork(appContext: Context) {

    Log.i("SynchronizeWorker", "SynchronizeWorker is requested")

    WorkManager
        .getInstance(appContext)
        .enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.KEEP, synchronizeWorkRequest)

}