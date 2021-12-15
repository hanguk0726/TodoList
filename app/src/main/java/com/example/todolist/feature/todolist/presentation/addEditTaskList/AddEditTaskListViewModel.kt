package com.example.todolist.feature.todolist.presentation.addEditTaskList

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
    private val taskListUseCases: TaskListUseCases,
) : ViewModel() {

    private val _taskListName = mutableStateOf(
        TodoListTextFieldState(
        hint = "새 목록 이름"
        ))

    val taskListName: State<TodoListTextFieldState> = _taskListName

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentTaskListId: Long? = null

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
                    } catch(e: InvalidTaskListException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save task list"
                            )
                        )
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String): UiEvent()
        object SaveTaskList: UiEvent()
    }
}