package com.example.todolist.feature.todolist.presentation.editTaskItem

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.use_case.task_item.TaskItemUseCases
import com.example.todolist.feature.todolist.domain.use_case.task_list.TaskListUseCases
import com.example.todolist.feature.todolist.presentation.todolist.TodoListTextFieldState
import com.example.todolist.feature.todolist.presentation.util.TaskListsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class EditTaskItemViewModel @Inject constructor(
    private val taskItemUseCases: TaskItemUseCases,
    private val taskListUseCases: TaskListUseCases,
) : ViewModel() {

    private val _taskItemTitle = mutableStateOf(
        TodoListTextFieldState(
            hint = "제목 입력"
        )
    )
    val taskItemTitle: State<TodoListTextFieldState> = _taskItemTitle

    private val _taskItemDetail = mutableStateOf(
        TodoListTextFieldState(
            hint = "세부 정보 추가"
        )
    )
    val taskItemDetail: State<TodoListTextFieldState> = _taskItemDetail

    private val _taskListsState = mutableStateOf(TaskListsState())
    val taskListsState: State<TaskListsState> = _taskListsState

    private val _taskItemState = mutableStateOf<TaskItem?>(null)
    val taskItemState: State<TaskItem?> = _taskItemState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var lastDeletedTaskItem: TaskItem? = null
    private var getTaskListsJob: Job? = null

    val currentTaskListOfTaskItem: TaskList?
        get() {
            val taskItem = _taskItemState.value
            return taskItem?.let {
                _taskListsState.value.taskLists.filter { el ->
                    el.id == taskItem.taskListId
                }
            }?.first()
        }

    init {
        getTaskLists()
    }

    @DelicateCoroutinesApi
    fun onEvent(event: EditTaskItemEvent) {
        when (event) {
            is EditTaskItemEvent.EnterTaskItemTitle -> {
                enterTaskItemTitle(event.value)
            }
            is EditTaskItemEvent.EnterTaskItemDetail -> {
                enterTaskItemDetail(event.value)
            }
            is EditTaskItemEvent.SaveTaskItem -> {
                saveTaskItem()
            }
            is EditTaskItemEvent.DeleteTaskItem -> {
                deleteTaskItem()
            }
            is EditTaskItemEvent.ToggleAndSaveTaskItemCompletionState -> {
                toggleAndSaveTaskItemCompletionState()
            }
            is EditTaskItemEvent.ChangeTaskListOfTaskItem -> {
                changeTaskListOfTaskItem(event.targetTaskListId)
            }
        }
    }

    fun loadTaskItemValues(_taskItemId: Long) {
        viewModelScope.launch {
            try {
                val targetTaskItem = taskItemUseCases.getTaskItemById(_taskItemId)
                _taskItemState.value = targetTaskItem
                _taskItemTitle.value = taskItemTitle.value.copy(
                    text = targetTaskItem!!.title,
                )
                _taskItemTitle.value = taskItemTitle.value.copy(
                    isHintVisible = taskItemTitle.value.text.isBlank()
                )
                _taskItemDetail.value = taskItemDetail.value.copy(
                    text = targetTaskItem.detail,
                )
                _taskItemDetail.value = taskItemDetail.value.copy(
                    isHintVisible = taskItemDetail.value.text.isBlank()
                )
            } catch (e: InvalidTaskItemException) {
                Log.e("EditTaskItemViewModel", e.message ?: "Couldn't get the taskItem")
            }
        }
    }

    private fun getTaskLists() {
        getTaskListsJob?.cancel()
        getTaskListsJob = taskListUseCases.getTaskLists()
            .onEach { result ->
                _taskListsState.value = taskListsState.value.copy(
                    taskLists = result
                )
            }.launchIn(viewModelScope)
    }
    private fun enterTaskItemDetail(value: String) {
        _taskItemDetail.value = taskItemDetail.value.copy(
            text = value
        )
        _taskItemDetail.value = taskItemDetail.value.copy(
            isHintVisible = taskItemDetail.value.text.isBlank()
        )
    }
    private fun enterTaskItemTitle(value: String) {
        _taskItemTitle.value = taskItemTitle.value.copy(
            text = value
        )
        _taskItemTitle.value = taskItemTitle.value.copy(
            isHintVisible = taskItemTitle.value.text.isBlank()
        )
    }

    private fun saveTaskItem() {
        viewModelScope.launch {
            try {
                val taskItem = _taskItemState.value
                taskItemUseCases.updateTaskItem(
                    TaskItem(
                        title = taskItemTitle.value.text,
                        detail = taskItemDetail.value.text,
                        isCompleted = taskItem!!.isCompleted,
                        taskListId = taskItem.taskListId,
                        createdTimestamp = taskItem.createdTimestamp,
                        id = taskItem.id
                    )
                )
                _eventFlow.emit(UiEvent.SaveTaskItem)
            } catch (e: InvalidTaskItemException) {
                Log.e(
                    "EditTaskItemViewModel",
                    e.message ?: "Couldn't save the taskItem"
                )
            }
        }
    }

    @DelicateCoroutinesApi
    private fun deleteTaskItem() {
        GlobalScope.launch {
            _eventFlow.emit(UiEvent.DeleteTaskItem)
        }
        viewModelScope.launch {
            try {
                taskItemUseCases.getTaskItemById(_taskItemState.value!!.id!!)
                    ?.also { _taskItem ->
                        taskItemUseCases.deleteTaskItem(_taskItem)
                        lastDeletedTaskItem = _taskItem

                    }
            } catch (e: InvalidTaskItemException) {
                Log.e(
                    "EditTaskItemViewModel",
                    e.message ?: "Couldn't save the taskItem"
                )
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            try {
                delay(500L)
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        message = "할 일 1개가 삭제됨",
                        actionLabel = "실행취소",
                        action = {
                            CoroutineScope(Dispatchers.Default).launch {
                                taskItemUseCases.addTaskItem(lastDeletedTaskItem!!)
                            }
                        }
                    ))
            } catch (e: Exception) {
                Log.e(
                    "EditTaskItemViewModel",
                    e.message ?: "Couldn't show snackbar for deleting taskItem"
                )
            }
        }
    }

    private fun toggleAndSaveTaskItemCompletionState() {
        viewModelScope.launch {
            try {
                _taskItemState.value = taskItemState.value!!.copy(
                    isCompleted = !_taskItemState.value!!.isCompleted
                )
                taskItemUseCases.updateTaskItem(
                    TaskItem(
                        title = taskItemTitle.value.text,
                        detail = taskItemDetail.value.text,
                        isCompleted = _taskItemState.value!!.isCompleted,
                        taskListId = _taskItemState.value!!.taskListId,
                        createdTimestamp = _taskItemState.value!!.createdTimestamp,
                        id = _taskItemState.value!!.id
                    )
                )
                _eventFlow.emit(UiEvent.SaveTaskItem)
            } catch (e: InvalidTaskItemException) {
                Log.e(
                    "EditTaskItemViewModel",
                    e.message ?: "Couldn't save the taskItem"
                )
            }
        }
    }

    private fun changeTaskListOfTaskItem(targetTaskListId: Long) {
        viewModelScope.launch {
            try {
                _taskItemState.value = taskItemState.value!!.copy(
                    taskListId = targetTaskListId
                )
                taskItemUseCases.updateTaskItem(taskItemState.value!!)
            } catch (e: InvalidTaskItemException) {
                Log.e(
                    "EditTaskItemViewModel",
                    e.message ?: "Couldn't update the taskItem"
                )
            }
        }
    }
    sealed class UiEvent {
        data class ShowSnackbar(
            val message: String,
            val actionLabel: String? = null,
            val action: () -> Unit
        ) : UiEvent()

        object SaveTaskItem : UiEvent()
        object DeleteTaskItem : UiEvent()
    }
}