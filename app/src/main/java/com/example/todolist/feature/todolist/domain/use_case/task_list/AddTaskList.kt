package com.example.todolist.feature.todolist.domain.use_case.task_list

import android.util.Log
import com.example.todolist.feature.todolist.data.remote.dto.toTaskListDto
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.repository.TaskListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTaskList(
    private val repository: TaskListRepository,
    private val androidId: String
) {

    @Throws(InvalidTaskListException::class)
    suspend operator fun invoke(vararg taskList: TaskList): List<Long> {
        if (taskList.any { el -> el.name.isBlank() }) {
            throw InvalidTaskListException("the name of the list can't be empty")
        }

        val insertedTaskList = addTaskListOnLocal(*taskList)

        val ids = insertedTaskList.map { it.id!! }

        addTaskListOnRemote(*insertedTaskList.toTypedArray())

        return ids
    }

    private suspend fun addTaskListOnLocal(vararg taskList: TaskList): List<TaskList> {
        val insertedTaskList = mutableListOf<TaskList>()
        taskList.forEach {
            val id = repository.insertTaskList(it).first()
            val item = it.copy(
                id = id,
                isSynchronizedWithRemote = true
            )
            insertedTaskList.add(item)
        }
        return insertedTaskList
    }

    private suspend fun addTaskListOnRemote(vararg taskList: TaskList) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val taskListDto = taskList.map {
                    it.toTaskListDto(userId = androidId)
                }
                repository.insertTaskListOnRemote(
                    taskListDto = taskListDto.toTypedArray()
                )


            } catch (e: Exception) {
                Log.e("AddTaskList", e.message.toString())
                val data = taskList.map {
                    it.copy(isSynchronizedWithRemote = false)
                }
                repository.updateTaskList(*data.toTypedArray())
            }
        }
}