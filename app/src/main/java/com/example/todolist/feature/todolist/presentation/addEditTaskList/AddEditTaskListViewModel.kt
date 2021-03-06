package com.example.todolist.feature.todolist.presentation.addEditTaskList

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.feature.todolist.domain.model.InvalidTaskListException
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.use_case.task_list.TaskListUseCases
import com.example.todolist.feature.todolist.presentation.todolist.TodoListTextFieldState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskListViewModel @Inject constructor(
    private val taskListUseCases: TaskListUseCases
) : ViewModel() {

    private val _taskListName = mutableStateOf(
        TodoListTextFieldState(
            hint = "새 목록 이름"
        )
    )
    val taskListName: State<TodoListTextFieldState> = _taskListName

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    fun onEvent(event: AddEditTaskListEvent) {
        when (event) {
            is AddEditTaskListEvent.EnterTaskListName -> {
                enterTaskListName(event.value)
            }
            is AddEditTaskListEvent.SaveTaskList -> {
                saveTaskList(event.taskListId)
            }
        }
    }

    fun clearTaskListNameTextField() {
        _taskListName.value = _taskListName.value.copy(
            text = "",
            isHintVisible = true
        )
    }

    fun loadTaskListNameToModify(taskListId: Long) {
        viewModelScope.launch {
            val targetTaskList = taskListUseCases.getTaskListById(taskListId)
            _taskListName.value = _taskListName.value.copy(
                text = targetTaskList!!.name,
                isHintVisible = false
            )
        }
    }

    private fun enterTaskListName(value: String) {
        _taskListName.value = taskListName.value.copy(
            text = value
        )
        _taskListName.value = taskListName.value.copy(
            isHintVisible = taskListName.value.text.isBlank()
        )
    }

    private fun saveTaskList(taskListId: Long) {
        viewModelScope.launch {
            try {
                taskListUseCases.addTaskList(
                    TaskList(
                        name = taskListName.value.text,
                        id = if (taskListId == -1L) null else taskListId
                    )
                )
                _eventFlow.emit(UiEvent.SaveTaskList)
            } catch (e: InvalidTaskListException) {
                Log.e("TodoListViewModel", e.message ?: "Couldn't create the taskList")
            }
        }
    }

    sealed class UiEvent {
        object SaveTaskList : UiEvent()
    }
}