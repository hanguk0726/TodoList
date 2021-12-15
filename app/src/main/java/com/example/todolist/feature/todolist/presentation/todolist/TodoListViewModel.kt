package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.di.AppModule
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.use_case.task_item.TaskItemUseCases
import com.example.todolist.feature.todolist.domain.use_case.task_list.TaskListUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

//savedStateHandle

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val taskItemUseCases: TaskItemUseCases,
    private val taskListUseCases: TaskListUseCases,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object{
       const val TASK_LIST_POSITION_KEY = "task_list_position_key"
    }

    private var initTaskListIndex = true

    private val _taskListId = mutableStateOf<Long?>(null)
    val taskListId: State<Long?> = _taskListId


    private val _taskItemContent = mutableStateOf(TodoListTextFieldState(
        hint = "새 할 일"
    ))
    val taskItemContent: State<TodoListTextFieldState> = _taskItemContent


    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getTaskListJob: Job? = null
    private var getTaskItemsJob: Job? = null

    private val _taskListsState = mutableStateOf(TaskListsState())
    val taskListsState: State<TaskListsState> = _taskListsState

    private val _taskItemsState = mutableStateOf(TaskItemState())
    val taskItemsState: State<TaskItemState> = _taskItemsState

    init {
        getTaskLists()
    }

    fun onEvent(event: TodoListEvent) {
        when(event) {
            is TodoListEvent.EnterTaskItemContent -> {
                _taskItemContent.value = taskItemContent.value.copy(
                    text = event.value,
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
                viewModelScope.launch {
                    try {
                        taskItemUseCases.addTaskItem(
                            TaskItem(
                                content = taskItemContent.value.text,
                                taskListId = taskListId.value!!
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveTaskItem)
                    } catch(e: InvalidTaskItemException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save taskItem"
                            )
                        )
                    }
                }
            }

            is TodoListEvent.GetTaskItemsByTaskListId -> {
                _taskListId.value = mutableStateOf(event.taskListId).value
            }
        }
    }
    // pager index를 3개를 holding하는 data class의 state를 만들어 관리해야할 듯 하다.
    private fun getTaskItemsByTaskListId(targetTaskListId : Long) {
        getTaskItemsJob?.cancel()
        getTaskItemsJob = taskItemUseCases.getTaskItemsByTaskListId(
            targetTaskListId
        ).onEach { taskItems ->
            _taskItemsState.value = taskItemsState.value.copy(
                    taskItems = taskItems
                )
            }
            .launchIn(viewModelScope)
    }

    private fun getTaskLists() {
        getTaskListJob?.cancel()
        getTaskListJob = taskListUseCases.getTaskLists()
            .onEach { taskLists ->
                if(taskLists.isEmpty()){
                    val initialTaskList = TaskList(
                        name = "할 일 목록",
                        createdTimestamp = System.currentTimeMillis(),
                        id = null
                    )
                    val taskListId = initializeFirstTaskList(initialTaskList)
                    _taskListsState.value = taskListsState.value.copy(
                        taskLists = listOf(initialTaskList.copy(id = taskListId))
                    )
                } else {
                    _taskListsState.value = taskListsState.value.copy(
                        taskLists = taskLists
                    )
                }
                if(initTaskListIndex){
                    _eventFlow.emit(UiEvent.InitialTaskListIndex(readLastTaskListIndex()))
                    initTaskListIndex = false
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun initializeFirstTaskList(taskList: TaskList): Long =
        withContext(viewModelScope.coroutineContext) {
            return@withContext try {
                taskListUseCases.addTaskList(taskList)
            } catch(e: Exception) {
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        message = e.message ?: "Couldn't create a task list"
                    )
                )
                -1
            }
    }

    suspend fun saveLastTaskListIndex(position: Int) {
        val dataStoreKey = intPreferencesKey(TASK_LIST_POSITION_KEY)
        dataStore.edit { settings ->
            settings[dataStoreKey] = position
        }
    }

    suspend fun readLastTaskListIndex(): Int {
        val dataStoreKey = intPreferencesKey(TASK_LIST_POSITION_KEY)
        return dataStore.data.map { preferences ->
            preferences[dataStoreKey] ?: 0
        }.first()
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String): UiEvent()
        data class InitialTaskListIndex(val index: Int): UiEvent()
        object SaveTaskList: UiEvent()
        object SaveTaskItem: UiEvent()
        object CompleteTaskItem: UiEvent()
        object RestoreTaskItemFromCompletion: UiEvent()
        object DeleteTaskItem: UiEvent()
        object DeleteTaskList: UiEvent()
    }
}