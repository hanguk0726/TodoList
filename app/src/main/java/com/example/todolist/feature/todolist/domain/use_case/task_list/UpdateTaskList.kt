package com.example.todolist.feature.todolist.domain.use_case.task_list

import android.util.Log
import com.example.todolist.feature.todolist.data.remote.dto.toTaskListDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateTaskList(
    private val repository: TaskListRepository,
    private val androidId: String
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(vararg taskList: TaskList) {

        taskList.forEach {
            repository.updateTaskList(
                it.copy(
                    isSynchronizedWithRemote = false
                )
            )
        }

        updateTaskListOnRemote(*taskList)

    }

    private suspend fun updateTaskListOnRemote(vararg taskList: TaskList) =
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val taskListDto = taskList.map {
                    it.toTaskListDto(userId = androidId)
                }
                val result = repository.updateTaskListOnRemote(
                    taskListDto = taskListDto.toTypedArray()
                )

                if (!result.isSuccessful) return@launch
                val data = taskList.map {
                    it.copy(isSynchronizedWithRemote = true)
                }
                repository.updateTaskList(*data.toTypedArray())
            } catch (e: Exception) {
                Log.e("UpdateTaskList", e.message.toString())
            }
        }
}