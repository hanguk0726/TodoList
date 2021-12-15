package com.example.todolist.feature.todolist.presentation.todolist

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.feature.todolist.domain.model.InvalidTaskItemException
import com.example.todolist.feature.todolist.domain.model.TaskItem
import com.example.todolist.feature.todolist.domain.model.TaskList
import com.example.todolist.feature.todolist.domain.use_case.task_item.TaskItemUseCases
import com.example.todolist.feature.todolist.domain.use_case.task_list.TaskListUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    companion object{
       const val TASK_LIST_POSITION_KEY = "task_list_position_key"
    }

    private val Context.dataStore by preferencesDataStore("settings")

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
                        println("mylogger saved ::${taskItemContent.value.text}")
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
            is TodoListEvent.ChangeTaskList -> {
                _taskListId.value = mutableStateOf(event.changedTaskListId).value
            }
            is TodoListEvent.GetTaskItemsByTaskListId -> {
                getTaskItemsByTaskListId(event.taskListId)
            }
        }
    }

    private fun getTaskItemsByTaskListId(targetTaskListId : Long) {
        getTaskItemsJob?.cancel()
        println("mylogger called targetTaskListId ::$targetTaskListId")
        getTaskItemsJob = taskItemUseCases.getTaskItemsByTaskListId(
            targetTaskListId
        ).onEach { taskItems ->
            _taskItemsState.value = taskItemsState.value.copy(
                    taskItems = taskItems
                )
            println("mylogger called size ::${taskItems.size}")
            if(taskItems.isNotEmpty()){

            println("mylogger chekc id ::${taskItems.first().taskListId}")
            }
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
                    _eventFlow.emit(UiEvent.InitialTaskListIndex(readLastSelectedTaskListIndex()))
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

    suspend fun saveLastSelectedTaskListIndex(position: Int) {
        val dataStoreKey = intPreferencesKey(TASK_LIST_POSITION_KEY)
        appContext.dataStore.edit { settings ->
            settings[dataStoreKey] = position
        }
    }

    suspend fun readLastSelectedTaskListIndex(): Int {
        val dataStoreKey = intPreferencesKey(TASK_LIST_POSITION_KEY)
        return appContext.dataStore.data.map { preferences ->
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