package com.example.todolist.feature.todolist.presentation.todolist

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

//savedStateHandle

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val taskItemUseCases: TaskItemUseCases,
    private val taskListUseCases: TaskListUseCases,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        const val TASK_LIST_POSITION_KEY = "task_list_position_key"
    }


    private var selectedTaskListId: Long? = null


    private val _taskItemContent = mutableStateOf(
        TodoListTextFieldState(
            hint = "새 할 일"
        )
    )
    val taskItemContent: State<TodoListTextFieldState> = _taskItemContent



    private val _lastSelectedTaskListPositionLoaded = mutableStateOf(false)
    val lastSelectedTaskListPositionLoaded = _lastSelectedTaskListPositionLoaded

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getTaskListsJob: Job? = null

    private val _taskListsState = mutableStateOf(TaskListsState())
    val taskListsState: State<TaskListsState> = _taskListsState

    private val taskItemManagerPool = HashMap<Long, TaskItemManager>()


    init {
        getTaskLists()
    }


    fun getTaskItems(targetTaskListId: Long):List<TaskItem>  {
        println("mylogger  contain key :: ${taskItemManagerPool.containsKey(targetTaskListId)} // for id ${targetTaskListId}")
        return if(taskItemManagerPool.containsKey(targetTaskListId)){
            taskItemManagerPool[targetTaskListId]!!.taskItemsState.value.taskItems
        } else {
            emptyList()
        }
    }

    fun onEvent(event: TodoListEvent) {
        when (event) {
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
                                taskListId = selectedTaskListId!!
                            )
                        )
                        _taskItemContent.value = taskItemContent.value.copy(text = "")
                        _eventFlow.emit(UiEvent.SaveTaskItem)
                    } catch (e: InvalidTaskItemException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save taskItem"
                            )
                        )
                    }
                }
            }
            is TodoListEvent.SelectTaskList -> {
                selectedTaskListId = event.selectedTaskListId
                viewModelScope.launch {
                    saveLastSelectedTaskListId(event.selectedTaskListId.toInt())
                }
            }
            is TodoListEvent.GetTaskItemsByTaskListId -> {
                getTaskItemsByTaskListId(event.taskListId)
            }
            is TodoListEvent.LoadLastSelectedTaskListPosition -> {
                viewModelScope.launch {
                    val lastSelectedTaskListId = readLastSelectedTaskListId()
                    if(lastSelectedTaskListId != -1){
                        val lastSelectedTaskList =  taskListsState.value.taskLists
                            .find{ el -> el.id!!.toInt() == lastSelectedTaskListId}
                        selectedTaskListId = lastSelectedTaskList!!.id
                        val index = taskListsState.value.taskLists.indexOf(lastSelectedTaskList)
                        println("mylogger lastSelectedTaskListId ${lastSelectedTaskListId}// index ${index}")
                        _eventFlow.emit(UiEvent.ScrollToLastSelectedTaskListPosition(index))
                    }
                }
            }
            is TodoListEvent.LastTaskListPositionHasSelected -> {
                _lastSelectedTaskListPositionLoaded.value = true
            }
        }
    }
// 안쓰이는 elements 정리 로직 필요
    private fun getTaskItemsByTaskListId(targetTaskListId: Long) {
        var T = if(taskItemManagerPool.containsKey(targetTaskListId)){
            taskItemManagerPool[targetTaskListId]!!
        } else {
            TaskItemManager()
        }
        T.getTaskItemsJob?.cancel()
        T.getTaskItemsJob = taskItemUseCases.getTaskItemsByTaskListId(
            targetTaskListId
        ).onEach { taskItems ->
            T._taskItemsState.value = T.taskItemsState.value.copy(
                taskItems = taskItems
            )
            taskItemManagerPool[targetTaskListId] = T
            println("mylogger size ${taskItems.size} // id $targetTaskListId")
        }.launchIn(viewModelScope)
    }

    private fun getTaskLists() {
        getTaskListsJob?.cancel()
        getTaskListsJob = taskListUseCases.getTaskLists()
            .onEach { taskLists ->
                if (taskLists.isEmpty()) {
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
            }
            .launchIn(viewModelScope)
    }

    private suspend fun initializeFirstTaskList(taskList: TaskList): Long =
        withContext(viewModelScope.coroutineContext) {
            return@withContext try {
                taskListUseCases.addTaskList(taskList)
            } catch (e: Exception) {
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        message = e.message ?: "Couldn't create a task list"
                    )
                )
                -1
            }
        }

    private suspend fun saveLastSelectedTaskListId(id: Int) {
        val dataStoreKey = intPreferencesKey(TASK_LIST_POSITION_KEY)
        dataStore.edit { settings ->
            settings[dataStoreKey] = id
        }
    }

    private suspend fun readLastSelectedTaskListId(): Int {
        val dataStoreKey = intPreferencesKey(TASK_LIST_POSITION_KEY)
        return dataStore.data.map { preferences ->
            preferences[dataStoreKey] ?: -1
        }.first()
    }

    data class TaskItemManager(
        val _taskItemsState: MutableState<TaskItemsState> = mutableStateOf(TaskItemsState()),
        val taskItemsState: State<TaskItemsState> = _taskItemsState,
        var getTaskItemsJob: Job? = null
    )

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class ScrollToLastSelectedTaskListPosition(val index: Int) : UiEvent()
        object SaveTaskList : UiEvent()
        object SaveTaskItem : UiEvent()
        object CompleteTaskItem : UiEvent()
        object RestoreTaskItemFromCompletion : UiEvent()
        object DeleteTaskItem : UiEvent()
        object DeleteTaskList : UiEvent()
    }
}