package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.use_case.task_item.TaskItemUseCases
import com.example.todolist.feature.todolist.domain.use_case.task_list.TaskListUseCases
import com.example.todolist.feature.todolist.domain.util.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

//savedStateHandle

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val taskItemUseCases: TaskItemUseCases,
    private val taskListUseCases: TaskListUseCases,
) : ViewModel() {


    private val _taskItemContent = mutableStateOf(TodoListTextFieldState(
        hint = "새 할 일"
    ))
    val taskItemContent: State<TodoListTextFieldState> = _taskItemContent

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getTaskListJob: Job? = null

    private val _taskListsState = mutableStateOf(TaskListsState())
    val taskListsState: State<TaskListsState> = _taskListsState



    init {
        getTaskLists()
        if(taskListsState.value.taskLists.isEmpty()){
            initializeFirstTaskList("할 일 목록")
        }
    }

    fun onEvent(event: TodoListEvent) {
        when(event) {
            is TodoListEvent.EnterTaskItemContent -> {
                _taskItemContent.value = taskItemContent.value.copy(
                    text = event.value
                )
                _taskItemContent.value = taskItemContent.value.copy(
                    isHintVisible = taskItemContent.value.text.isBlank()
                )
            }

            is TodoListEvent.CompleteTaskItem -> {

            }

            is TodoListEvent.DeleteTaskItem -> {

            }

            is TodoListEvent.DeleteTaskList -> {

            }

            is TodoListEvent.RestoreTaskItemFromCompletion -> {

            }

            is TodoListEvent.SaveTaskItem -> {

            }

        }
    }

    private fun getTaskLists() {
        getTaskListJob?.cancel()
        getTaskListJob = taskListUseCases.getTaskLists()
            .onEach {  taskLists ->
                _taskListsState.value = taskListsState.value.copy(
                    taskLists = taskLists
                )
            }
            .launchIn(viewModelScope)
    }

    private fun initializeFirstTaskList(name: String) {
        viewModelScope.launch {
            try {
                taskListUseCases.addTaskList(
                    TaskList(
                        name = name,
                        lastModificationTimestamp = System.currentTimeMillis(),
                        id = null
                    )
                )
            } catch(e: Exception) {
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        message = e.message ?: "Couldn't create a task list"
                    )
                )
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String): UiEvent()
        object SaveTaskList: UiEvent()
        object SaveTaskItem: UiEvent()
        object CompleteTaskItem: UiEvent()
        object RestoreTaskItemFromCompletion: UiEvent()
        object DeleteTaskItem: UiEvent()
        object DeleteTaskList: UiEvent()
    }
}