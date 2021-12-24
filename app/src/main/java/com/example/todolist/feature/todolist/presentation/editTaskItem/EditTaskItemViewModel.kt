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
import com.example.todolist.feature.todolist.presentation.util.TaskItemState
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

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentTaskItemId: Long? = null

    private var _currentTaskItemCompletionState: Boolean? = null

    val currentTaskItemCompletionState: Boolean?
        get() = _currentTaskItemCompletionState

    private var currentTaskItemTaskListId: Long? = null
    private var currentTaskItemTimeStamp: Long? = null

    private var lastDeletedTaskItem: TaskItem? = null
    private var getTaskListsJob: Job? = null

    val currentTaskItemTaskList: TaskList?
        get() {
            return currentTaskItemTaskListId?.let {
                _taskListsState.value.taskLists.filter { el ->
                    el.id == currentTaskItemTaskListId!!
                }
            }?.first()
        }

    private val _taskItemState = mutableStateOf(TaskItemState())
    val taskItemState: State<TaskItemState> = _taskItemState

    init {
        getTaskLists()
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
                GlobalScope.launch {
                    _eventFlow.emit(UiEvent.DeleteTaskItem)
                }
                viewModelScope.launch {
                    try {
                        taskItemUseCases.getTaskItemById(currentTaskItemId!!)?.also { _taskItem ->
                            taskItemUseCases.deleteTaskItem(_taskItem)
                            lastDeletedTaskItem = _taskItem

                        }
                    } catch (e: InvalidTaskItemException) {
                        Log.e(
                            "EditTaskItemViewModel",
                            "${e.message ?: "Couldn't save the taskItem"}"
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
                            "${e.message ?: "Couldn't show snackbar for deleting taskItem"}"
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

            is EditTaskItemEvent.ChangeTaskListOfTaskItem -> {

            }
        }
    }

    suspend fun loadTaskItemValues(_taskItemId: Long): TaskItem? =
        withContext(viewModelScope.coroutineContext) {
            return@withContext try {
                currentTaskItemId = _taskItemId
                val targetTaskItem = taskItemUseCases.getTaskItemById(_taskItemId)
                _taskItemState.value = taskItemState.value.copy(
                    taskItem = targetTaskItem
                )
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

                currentTaskItemTaskListId = targetTaskItem.taskListId

                currentTaskItemTimeStamp = targetTaskItem.timestamp
                targetTaskItem
            } catch (e: InvalidTaskItemException) {
                Log.e("EditTaskItemViewModel", "${e.message ?: "Couldn't get the taskItem"}")
                null
            }
        }

    private fun getTaskLists() {
        getTaskListsJob?.cancel()
        getTaskListsJob = taskListUseCases.getTaskLists()
            .onEach { taskLists ->
                _taskListsState.value = taskListsState.value.copy(
                    taskLists = taskLists
                )
            }
            .launchIn(viewModelScope)
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