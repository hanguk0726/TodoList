package com.example.todolist.feature.todolist.presentation.addEditTaskList

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
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
    private val taskListUseCases: TaskListUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _taskListName = mutableStateOf(
        TodoListTextFieldState(
            hint = "새 목록 이름"
        )
    )

    val taskListName: State<TodoListTextFieldState> = _taskListName

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentTaskListId: Long? = null

    init {
        savedStateHandle.get<Long>("taskListId")?.let { _taskListId ->
            if (_taskListId != -1L) {
                viewModelScope.launch {
                    taskListUseCases.getTaskListById(_taskListId)?.also { _taskList ->
                        currentTaskListId = _taskListId
                        _taskListName.value = taskListName.value.copy(
                            text = _taskList.name,
                            isHintVisible = false
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditTaskListEvent) {
        when (event) {
            is AddEditTaskListEvent.EnterTaskListName -> {
                _taskListName.value = taskListName.value.copy(
                    text = event.value
                )
                _taskListName.value = taskListName.value.copy(
                    isHintVisible = taskListName.value.text.isBlank()
                )
            }
            is AddEditTaskListEvent.SaveTaskList -> {
                viewModelScope.launch {
                    try {
                        taskListUseCases.addTaskList(
                            TaskList(
                                name = taskListName.value.text,
                                id = currentTaskListId
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveTaskList)
                    } catch (e: InvalidTaskListException) {
                        Log.e("TodoListViewModel", "${e.message ?: "Couldn't create the taskList"}")
                    }
                }
            }
        }
    }
    fun clearTaskListNameTextField() {
        _taskListName.value = _taskListName.value.copy(
            text = "",
            isHintVisible = true
        )
    }

    fun loadTaskListNameToModify() {
        viewModelScope.launch {
            val targetTaskList = taskListUseCases.getTaskListById(currentTaskListId!!)
            _taskListName.value = _taskListName.value.copy(
                text = targetTaskList!!.name,
                isHintVisible = false
            )
        }
    }
    sealed class UiEvent {
        object SaveTaskList : UiEvent()
    }
}