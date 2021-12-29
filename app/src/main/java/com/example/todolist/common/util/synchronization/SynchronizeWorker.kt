package com.example.todolist.common.util.synchronization

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todolist.common.Constants
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
import javax.inject.Inject

class SynchronizeWorker (context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams){

    @Inject
    private lateinit var taskListRepository: TaskListRepository

    @Inject
    private lateinit var taskItemRepository: TaskItemRepository

    @Inject
    private lateinit var taskListUseCases : TaskListUseCases

    @Inject
    private lateinit var taskItemUseCases : TaskItemUseCases

    @Inject
    private lateinit var taskItemApi : TaskItemApi


    @Inject
    private lateinit var taskListApi : TaskListApi

    private val userId =  Constants.ANDROID_ID

    override suspend fun doWork(): Result {

        // scroll to refresh action 이거나 처음 시작했을 경우 실행
        // 작업이후에 ViewModel가 인식하는가 Check

        val taskListsFromLocal = taskListRepository.getTaskLists()
        val taskListsFromRemote = taskListRepository.getTaskListsOnRemote(userId)

        // Case : local에 있고 remote에 없음
        taskListsFromLocal.forEach { taskList ->
            synchronizeTaskItemLocalToRemote(taskList.id!!)
            synchronizeTaskListLocalToRemote(taskList)
        }

        // Case : remote에 있고 local에 없음
        taskListsFromRemote.forEach { taskListDto ->
            val taskList = taskListDto.toTaskList()

            if( ! taskListsFromLocal.contains(taskList)) {
                taskListRepository.insertTaskList(taskList.copy(
                    isSynchronizedWithRemote = true
                ))
            }

            val taskItemsFromRemote = taskItemRepository
                .getTaskItemsByTaskListIdOnRemote(taskList.id!!, userId).map {
                    it.toTaskItem()
            }
            val taskItemsFromLocal = taskItemRepository.getTaskItemsByTaskListId(taskList.id!!)

            taskItemsFromRemote.forEach {
                if( ! taskItemsFromLocal.contains(it)) {
                    taskItemRepository.insertTaskItem(it)
                }
            }
        }



        return Result.success()
    }

    private suspend fun synchronizeTaskItemLocalToRemote(taskListId : Long) {
        val taskItems = taskItemRepository.getTaskItemsByTaskListId(taskListId)
        taskItems.forEach { taskItem ->
            if(taskItem.needToBeDeleted){
                taskItemUseCases.deleteTaskItem(taskItem)
            } else {
                if( ! taskItem.isSynchronizedWithRemote ) {
                    val item = taskItem.toTaskItemDto(userId)
                    val result = taskItemApi.synchronizeTaskItem(
                        taskItemDto = arrayOf(item), userId)
                    if(result.isExecuted){
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

    private suspend fun synchronizeTaskListLocalToRemote(taskList : TaskList) {
        if(taskList.needToBeDeleted) {
            taskListUseCases.deleteTaskList(taskList)
        } else {
            if( ! taskList.isSynchronizedWithRemote ) {
                val item = taskList.toTaskListDto(userId)
                val result = taskListApi.synchronizeTaskList(
                    taskListDto = arrayOf(item), userId)
                if(result.isExecuted){
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
