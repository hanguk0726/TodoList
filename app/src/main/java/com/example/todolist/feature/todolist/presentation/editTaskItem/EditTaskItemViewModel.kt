package com.example.todolist.feature.todolist.presentation.editTaskItem

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.use_case.task_item.TaskItemUseCases
import com.example.todolist.feature.todolist.presentation.todolist.TodoListTextFieldState
import com.example.todolist.feature.todolist.presentation.todolist.TodoListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditTaskItemViewModel @Inject constructor(
    private val taskItemUseCases: TaskItemUseCases,
    savedStateHandle: SavedStateHandle
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

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentTaskItemId: Long? = null

    private var _currentTaskItemCompletionState: Boolean? = null

    val currentTaskItemCompletionState: Boolean?
        get() = _currentTaskItemCompletionState

    private var currentTaskItemTaskListId: Long? = null
    private var currentTaskItemTimeStamp: Long? = null

    init {
        savedStateHandle.get<Long>("taskItemId")?.let { _taskItemId ->
            if (_taskItemId != -1L) {
                viewModelScope.launch {
                    taskItemUseCases.getTaskItemById(_taskItemId)?.also { _taskItem ->
                        currentTaskItemId = _taskItemId
                        _taskItemTitle.value = taskItemTitle.value.copy(
                            text = _taskItem.title,
                        )
                        _taskItemTitle.value = taskItemTitle.value.copy(
                            isHintVisible = taskItemTitle.value.text.isBlank()
                        )
                        _taskItemDetail.value = taskItemDetail.value.copy(
                            text = _taskItem.detail,
                        )
                        _taskItemDetail.value = taskItemDetail.value.copy(
                            isHintVisible = taskItemDetail.value.text.isBlank()
                        )
                        _currentTaskItemCompletionState = _taskItem.isCompleted

                        currentTaskItemTaskListId = _taskItem.taskListId

                        currentTaskItemTimeStamp = _taskItem.timestamp
                    }
                }
            }
        }
    }

    fun onEvent(event: EditTaskItemEvent) {
        when (event) {
            is EditTaskItemEvent.EnterTaskItemTitle -> {
                _taskItemTitle.value = taskItemTitle.value.copy(
                    text = event.value
                )
                _taskItemTitle.value = taskItemTitle.value.copy(
                    isHintVisible = taskItemTitle.value.text.isBlank()
                )
            }
            is EditTaskItemEvent.EnterTaskItemDetail -> {
                _taskItemDetail.value = taskItemDetail.value.copy(
                    text = event.value
                )
                _taskItemDetail.value = taskItemDetail.value.copy(
                    isHintVisible = taskItemDetail.value.text.isBlank()
                )
            }
            is EditTaskItemEvent.SaveTaskItem -> {
                viewModelScope.launch {
                    try {
                        taskItemUseCases.updateTaskItem(
                            TaskItem(
                                title = taskItemTitle.value.text,
                                detail = taskItemDetail.value.text,
                                isCompleted = _currentTaskItemCompletionState!!,
                                taskListId = currentTaskItemTaskListId!!,
                                timestamp = currentTaskItemTimeStamp!!,
                                id = currentTaskItemId
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveTaskItem)
                    } catch (e: InvalidTaskItemException) {
                        Log.e(
                            "EditTaskItemViewModel",
                            "${e.message ?: "Couldn't save the taskItem"}"
                        )
                    }
                }
            }
            is EditTaskItemEvent.DeleteTaskItem -> {
                viewModelScope.launch {
                    try {
                        taskItemUseCases.getTaskItemById(currentTaskItemId!!)?.also { _taskItem ->
                            taskItemUseCases.deleteTaskItem(_taskItem)
                            _eventFlow.emit(UiEvent.DeleteTaskItem)

                            _eventFlow.emit(
                                UiEvent.ShowSnackbar(
                                message = "할 일 1개가 삭제됨",
                                actionLabel = "실행취소",
                                action = {
                                    viewModelScope.launch {
                                        taskItemUseCases.addTaskItem(_taskItem)
                                    }
                                }
                            ))
                        }
                    } catch (e: InvalidTaskItemException) {
                        Log.e(
                            "EditTaskItemViewModel",
                            "${e.message ?: "Couldn't save the taskItem"}"
                        )
                    }
                }
            }
            is EditTaskItemEvent.ToggleAndSaveTaskItemCompletionState -> {
                viewModelScope.launch {
                    try {
                        taskItemUseCases.updateTaskItem(
                            TaskItem(
                                title = taskItemTitle.value.text,
                                detail = taskItemDetail.value.text,
                                isCompleted = !_currentTaskItemCompletionState!!,
                                taskListId = currentTaskItemTaskListId!!,
                                timestamp = currentTaskItemTimeStamp!!,
                                id = currentTaskItemId
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveTaskItem)
                    } catch (e: InvalidTaskItemException) {
                        Log.e(
                            "EditTaskItemViewModel",
                            "${e.message ?: "Couldn't save the taskItem"}"
                        )
                    }
                }
            }
        }
    }

    fun loadTaskItemValues() {
        currentTaskItemId?.let{
            viewModelScope.launch {
                val targetTaskItem = taskItemUseCases.getTaskItemById(currentTaskItemId!!)
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
                _currentTaskItemCompletionState = targetTaskItem.isCompleted
            }
        }

    }

    sealed class UiEvent {
        data class ShowSnackbar(
            val message: String,
            val actionLabel: String? = null,
            val action: () -> Unit
        ): UiEvent()
        object SaveTaskItem : UiEvent()
        object DeleteTaskItem : UiEvent()
    }
}